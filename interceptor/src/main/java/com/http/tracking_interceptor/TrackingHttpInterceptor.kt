package com.http.tracking_interceptor

import com.http.tracking_interceptor.model.*
import okhttp3.*
import okio.Buffer
import okio.GzipSource
import java.io.IOException
import java.nio.charset.Charset

/**
 * Description : Http 정보 추적하는 Interceptor
 *
 * Created by juhongmin on 2022/03/29
 */
class TrackingHttpInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 릴리즈 Skip
        if (TrackingDataManager.getInstance().isRelease()) {
            return chain.proceed(request)
        }
        val tracking = try {
            TrackingHttpEntity(
                headerMap = toHeaderMap(request.headers),
                path = request.url.encodedPath,
                req = toReqEntity(request)
            ).apply {
                scheme = request.url.scheme
                baseUrl = request.url.host
                method = request.method
            }
        } catch (ex: Exception) {
            null
        }

        val response = try {
            chain.proceed(request)
        } catch (ex: Exception) {
            tracking?.error = ex
            throw ex
        }
        tracking?.runCatching {
            responseTimeMs = response.receivedResponseAtMillis
            res = toResEntity(request, response)
            takenTimeMs = response.receivedResponseAtMillis - response.sentRequestAtMillis
            code = response.code
        }
        TrackingDataManager.getInstance().addTracking(tracking)
        return response
    }

    /**
     * Request Header to Map
     */
    private fun toHeaderMap(headers: Headers): Map<String, String> {
        val map = mutableMapOf<String, String>()
        headers.forEach { pair ->
            map[pair.first] = pair.second
        }
        return map
    }

    /**
     * Converter TrackingRequestEntity 변환 함수
     * @param req OkHttp Request
     */
    private fun toReqEntity(req: Request): BaseTrackingRequestEntity {
        return if (req.body is MultipartBody) {
            toMultipartReqEntity(req)
        } else {
            toDefaultReqEntity(req)
        }
    }

    /**
     * Request Entity Default Type
     * @param req OkHttp Request
     */
    private fun toDefaultReqEntity(req: Request): TrackingRequestEntity {
        return TrackingRequestEntity(
            _fullUrl = req.url.toString(),
            _mediaType = req.body?.contentType(),
            body = toReqBodyStr(req.body)
        )
    }

    /**
     * Converter Multipart Request Entity
     * @param req OkHttp Request
     */
    private fun toMultipartReqEntity(req: Request): TrackingRequestMultipartEntity {
        val parts = mutableListOf<Part>()
        (req.body as MultipartBody).parts.forEach { part ->
            parts.add(Part(part.body.contentType(), toReqBodyBytes(part.body)))
        }
        return TrackingRequestMultipartEntity(
            _fullUrl = req.url.toString(),
            _mediaType = req.body?.contentType(),
            binaryList = parts
        )
    }

    /**
     * Converter TrackingResponseEntity 변환 함수
     */
    private fun toResEntity(req: Request, res: Response): TrackingResponseEntity {
        val bodyString = toResBodyStr(req.headers, res.body)
        return TrackingResponseEntity(
            body = bodyString
        )
    }

    /**
     * Request Body to String
     */
    private fun toReqBodyStr(body: RequestBody?): String? {
        if (body == null) return null
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readString(Charsets.UTF_8)
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Request Body to ByteArray
     */
    private fun toReqBodyBytes(body: RequestBody?): ByteArray? {
        if (body == null) return null
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readByteArray()
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Response Body to String
     */
    private fun toResBodyStr(headers: Headers, body: ResponseBody?): String? {
        if (body == null) return null
        return try {
            val contentLength = body.contentLength()
            val source = body.source()
            source.request(Long.MAX_VALUE)
            var buffer = source.buffer
            if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }
            if (contentLength != 0L) {
                buffer.clone().readString(Charset.defaultCharset())
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
    }
}
