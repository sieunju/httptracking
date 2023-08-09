package hmju.http.tracking_interceptor.model

import okhttp3.Headers
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.GzipSource
import java.net.SocketTimeoutException
import java.nio.charset.Charset

/**
 * Description : Tracking 에 데이터 모델
 *
 * Created by juhongmin on 2023/08/09
 */
sealed class HttpTrackingModel(
    open var uid: Long = -1
) {
    internal data class Default(
        val code: Int,
        val host: String,
        val path: String,
        val sentTimeMs: Long,
        val receivedTimeMs: Long,
        val request: HttpTracking,
        val response: HttpTracking,
        override var uid: Long
    ) : HttpTrackingModel(uid) {

        companion object {

            /**
             * init Request Model
             *
             * @see [HttpTracking.RequestMultiPart]
             * @see [HttpTracking.Request]
             */
            private fun getRequest(
                req: Request
            ): HttpTracking {
                return if (req.body is MultipartBody) {
                    val parts = mutableListOf<Part>()
                    (req.body as MultipartBody).parts.forEach {
                        parts.add(Part(it.body.contentType(), toReqBodyBytes(it.body)))
                    }
                    HttpTracking.RequestMultiPart(
                        headerMap = req.headers.toMap(),
                        queryParams = req.url.query,
                        binaryList = parts
                    )
                } else {
                    HttpTracking.Request(
                        headerMap = req.headers.toMap(),
                        queryParams = req.url.query,
                        body = getReqBody(req.body)
                    )
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
             * Response Body to JSON String
             */
            private fun getResBody(headers: Headers, body: ResponseBody?): String? {
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

            /**
             * Request Body to String
             */
            private fun getReqBody(body: RequestBody?): String? {
                if (body == null) return null
                return try {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    buffer.readString(Charsets.UTF_8)
                } catch (ex: Exception) {
                    null
                }
            }

            private fun getResponse(
                res: Response
            ): HttpTracking {
                return HttpTracking.Response(
                    headerMap = res.headers.toMap(),
                    body = getResBody(res.headers, res.body)
                )
            }
        }

        constructor(
            req: Request,
            res: Response
        ) : this(
            code = res.code,
            host = req.url.host,
            path = req.url.encodedPath,
            sentTimeMs = res.sentRequestAtMillis,
            receivedTimeMs = res.receivedResponseAtMillis,
            request = getRequest(req),
            response = getResponse(res),
            uid = -1
        )
    }

    internal data class TimeOut(
        val path: String,
        val sendTimeMs: Long,
        val msg: String,
        override var uid: Long
    ) : HttpTrackingModel(uid) {
        constructor(
            req: Request,
            sendTimeMs: Long,
            err: SocketTimeoutException
        ) : this(
            path = req.url.encodedPath,
            sendTimeMs = sendTimeMs,
            msg = err.message ?: "",
            uid = -1
        )
    }

    internal data class Error(
        val path: String,
        val sendTimeMs: Long,
        val msg: String,
        override var uid: Long
    ) : HttpTrackingModel(uid) {
        constructor(
            req: Request,
            sendTimeMs: Long,
            err: Exception
        ) : this(
            path = req.url.encodedPath,
            sendTimeMs = sendTimeMs,
            msg = err.message ?: "",
            uid = -1
        )
    }
}
