package com.http.tracking

import com.http.tracking.entity.TrackingHttpEntity
import com.http.tracking.entity.TrackingRequestEntity
import com.http.tracking.entity.TrackingResponseEntity
import okhttp3.*
import okio.Buffer
import okio.GzipSource
import okio.IOException
import timber.log.Timber
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
        if (TrackingManager.getInstance().isRelease()) {
            return chain.proceed(request)
        }
        val tracking = try {
            Timber.d("TrackingURL ${request.url}")
            TrackingHttpEntity(
                headerMap = toHeaderMap(request.headers),
                path = request.url.encodedPath,
                req = TrackingRequestEntity(
                    fullUrl = request.url.toString(),
                    body = toReqBodyStr(request.body)
                )
            ).apply {
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
        tracking?.let {
            it.responseTimeMs = response.receivedResponseAtMillis
            it.res = TrackingResponseEntity(toResBodyString(request.headers, response.body))
            it.takenTimeMs = response.receivedResponseAtMillis - response.sentRequestAtMillis
            it.code = response.code
        }
        TrackingManager.getInstance().addTracking(tracking)
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
     * Response Body to String
     */
    private fun toResBodyString(headers: Headers, body: ResponseBody?): String? {
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
