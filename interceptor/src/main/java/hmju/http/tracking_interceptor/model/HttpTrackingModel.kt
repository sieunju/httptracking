package hmju.http.tracking_interceptor.model

import hmju.http.tracking_interceptor.Extensions.toDate
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.Response
import java.net.SocketTimeoutException

/**
 * Description : Tracking 에 데이터 모델
 *
 * Created by juhongmin on 2023/08/09
 */
sealed class HttpTrackingModel(
    open var uid: Long = -1
) {

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    class Default(
        req: Request,
        res: Response
    ) : HttpTrackingModel(-1) {

        val isSuccess: Boolean
        val method: String
        val code: Int
        val host: String
        val path: String
        val fullUrl : String
        val sentTimeMs: Long
        val receivedTimeMs: Long
        val request: HttpTrackingRequest
        val response: HttpTrackingResponse
        val takeTimeMs: Long
        val timeDate: String

        init {
            isSuccess = res.code in 200..299
            method = req.method
            code = res.code
            host = req.url.host
            path = req.url.encodedPath
            fullUrl = req.url.toString()
            sentTimeMs = res.sentRequestAtMillis
            receivedTimeMs = res.receivedResponseAtMillis
            request = getRequest(req)
            response = HttpTrackingResponse.Default(res)
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
         * @see [HttpTrackingRequest.MultiPart]
         * @see [HttpTrackingRequest.Default]
         */
        private fun getRequest(
            req: Request
        ): HttpTrackingRequest {
            return if (req.body is MultipartBody) {
                HttpTrackingRequest.MultiPart(req)
            } else {
                HttpTrackingRequest.Default(req)
            }
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
