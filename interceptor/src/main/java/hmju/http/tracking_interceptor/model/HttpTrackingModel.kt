package hmju.http.tracking_interceptor.model

import hmju.http.tracking_interceptor.Extensions.toDate
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

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    class Default constructor(
        req: Request,
        res: Response
    ) : HttpTrackingModel(-1) {

        val isSuccess: Boolean
        val method: String
        val code: Int
        val host: String
        val path: String
        val sentTimeMs: Long
        val receivedTimeMs: Long
        val request: HttpTracking
        val response: HttpTracking
        val takeTimeMs: Long
        val timeDate: String

        init {
            isSuccess = res.code in 200..299
            method = req.method
            code = res.code
            host = req.url.host
            path = req.url.encodedPath
            sentTimeMs = res.sentRequestAtMillis
            receivedTimeMs = res.receivedResponseAtMillis
            request = getRequest(req)
            response = getResponse(res)
            takeTimeMs = (receivedTimeMs - sentTimeMs)
            val sentTime = res.sentRequestAtMillis.toDate()
            val receiveTime = res.receivedResponseAtMillis.toDate()
            timeDate = "$sentTime - $receiveTime"
        }

        override fun equals(other: Any?): Boolean {
            return if (other is Default) {
                uid == other.uid &&
                        code == other.code &&
                        host == other.host &&
                        path == other.path &&
                        sentTimeMs == other.sentTimeMs &&
                        receivedTimeMs == other.receivedTimeMs &&
                        request == other.request &&
                        response == other.response
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            var result = code
            result = 31 * result + host.hashCode()
            result = 31 * result + path.hashCode()
            result = 31 * result + sentTimeMs.hashCode()
            result = 31 * result + receivedTimeMs.hashCode()
            result = 31 * result + request.hashCode()
            result = 31 * result + response.hashCode()
            return result
        }

        /**
         * init Request Model
         *
         * @see [HttpTracking.RequestMultiPart]
         * @see [HttpTracking.Request]
         */
        private fun getRequest(
            req: Request
        ): HttpTracking {
            val body = req.body
            return if (body is MultipartBody) {
                HttpTracking.RequestMultiPart(
                    headerMap = req.headers.toMap(),
                    queryParams = req.url.query,
                    binaryList = body.parts.map { HttpTracking.RequestMultiPart.MultiPart(it) }
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

        private fun getResponse(
            res: Response
        ): HttpTracking {
            return HttpTracking.Response(
                headerMap = res.headers.toMap(),
                body = getResBody(res.headers, res.body)
            )
        }
    }

    data class TimeOut(
        val host: String,
        val path: String,
        val method: String,
        val sendTimeMs: Long,
        val msg: String,
        val sendTimeText: String,
        override var uid: Long
    ) : HttpTrackingModel(uid) {
        constructor(
            req: Request,
            sendTimeMs: Long,
            err: SocketTimeoutException
        ) : this(
            host = req.url.host,
            path = req.url.encodedPath,
            method = req.method,
            sendTimeMs = sendTimeMs,
            msg = err.message ?: "",
            sendTimeText = sendTimeMs.toDate(),
            uid = -1
        )
    }

    data class Error(
        val host: String,
        val path: String,
        val method: String,
        val sendTimeMs: Long,
        val msg: String,
        val sendTimeText: String,
        override var uid: Long
    ) : HttpTrackingModel(uid) {
        constructor(
            req: Request,
            sendTimeMs: Long,
            err: Exception
        ) : this(
            host = req.url.host,
            path = req.url.encodedPath,
            method = req.method,
            sendTimeMs = sendTimeMs,
            msg = err.message ?: "",
            sendTimeText = sendTimeMs.toDate(),
            uid = -1
        )
    }
}
