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
sealed class TrackingModel(
    open var uid: Long = -1
) {

    @Suppress("MemberVisibilityCanBePrivate")
    class Default(
        req: Request,
        res: Response
    ) : TrackingModel(-1) {

        val isSuccess: Boolean
        val method: String
        val code: Int
        val host: String
        val path: String
        val fullUrl: String
        val sentTimeMs: Long
        val receivedTimeMs: Long
        val request: HttpTrackingRequest
        val response: TrackingResponse
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
            response = TrackingResponse.Default(res)
            takeTimeMs = (receivedTimeMs - sentTimeMs)
            val sentTime = res.sentRequestAtMillis.toDate()
            val receiveTime = res.receivedResponseAtMillis.toDate()
            timeDate = "$sentTime ~ $receiveTime"
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

    @Suppress("MemberVisibilityCanBePrivate")
    class TimeOut(
        req: Request,
        sendTimeMs: Long,
        err: SocketTimeoutException
    ) : TrackingModel(-1) {

        val method: String
        val host: String
        val path: String
        val fullUrl: String
        val msg: String
        val sendTimeText: String
        val request: HttpTrackingRequest

        init {
            method = req.method
            host = req.url.host
            path = req.url.encodedPath
            fullUrl = req.url.toString()
            msg = err.message ?: ""
            sendTimeText = sendTimeMs.toDate()
            request = getRequest(req)
        }

        override fun equals(other: Any?): Boolean {
            return if (other is TimeOut) {
                host == other.host &&
                        path == other.path &&
                        fullUrl == other.fullUrl &&
                        msg == other.msg &&
                        sendTimeText == other.sendTimeText &&
                        request == other.request
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            var result = method.hashCode()
            result = 31 * result + host.hashCode()
            result = 31 * result + path.hashCode()
            result = 31 * result + fullUrl.hashCode()
            result = 31 * result + msg.hashCode()
            result = 31 * result + sendTimeText.hashCode()
            result = 31 * result + request.hashCode()
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

    @Suppress("MemberVisibilityCanBePrivate")
    class Error(
        req: Request,
        sendTimeMs: Long,
        err: Exception
    ) : TrackingModel(-1) {

        val method: String
        val host: String
        val path: String
        val fullUrl: String
        val msg: String
        val sendTimeText: String
        val request: HttpTrackingRequest

        init {
            method = req.method
            host = req.url.host
            path = req.url.encodedPath
            fullUrl = req.url.toString()
            msg = err.message ?: ""
            sendTimeText = sendTimeMs.toDate()
            request = getRequest(req)
        }

        override fun equals(other: Any?): Boolean {
            return if (other is Error) {
                host == other.host &&
                        path == other.path &&
                        fullUrl == other.fullUrl &&
                        msg == other.msg &&
                        sendTimeText == other.sendTimeText &&
                        request == other.request
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            var result = method.hashCode()
            result = 31 * result + host.hashCode()
            result = 31 * result + path.hashCode()
            result = 31 * result + fullUrl.hashCode()
            result = 31 * result + msg.hashCode()
            result = 31 * result + sendTimeText.hashCode()
            result = 31 * result + request.hashCode()
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

    @JvmName("getCommonMethod")
    fun getMethod(): String {
        return when (this) {
            is Default -> this.method
            is TimeOut -> this.method
            is Error -> this.method
            else -> throw IllegalArgumentException()
        }
    }

    @JvmName("getCommonHost")
    fun getHost(): String {
        return when (this) {
            is Default -> this.host
            is TimeOut -> this.host
            is Error -> this.host
            else -> throw IllegalArgumentException()
        }
    }

    @JvmName("getCommonPath")
    fun getPath(): String {
        return when (this) {
            is Default -> this.path
            is TimeOut -> this.path
            is Error -> this.path
            else -> throw IllegalArgumentException()
        }
    }

    @JvmName("getCommonRequest")
    fun getRequest(): HttpTrackingRequest {
        return when (this) {
            is Default -> this.request
            is TimeOut -> this.request
            is Error -> this.request
            else -> throw IllegalArgumentException()
        }
    }
}
