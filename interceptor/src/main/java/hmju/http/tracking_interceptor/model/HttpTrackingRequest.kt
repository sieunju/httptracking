package hmju.http.tracking_interceptor.model

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer

/**
 * Description : HttpTracking Request Data Model
 *
 * Created by juhongmin on 2023/08/09
 */
sealed class HttpTrackingRequest(
    open val headerMap: Map<String, String>,
    open val queryParams: String?
) {

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    class Default(
        req: okhttp3.Request
    ) : HttpTrackingRequest(
        req.headers.toMap(),
        req.url.query
    ) {

        val body: String?

        init {
            body = getReqBody(req.body)
        }

        override fun equals(other: Any?): Boolean {
            return if (other is Default) {
                headerMap == other.headerMap &&
                        queryParams == other.queryParams &&
                        body == other.body
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            var result = headerMap.hashCode()
            result = 31 * result + (queryParams?.hashCode() ?: 0)
            result = 31 * result + (body?.hashCode() ?: 0)
            return result
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
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    class MultiPart(
        req: okhttp3.Request
    ) : HttpTrackingRequest(
        req.headers.toMap(),
        req.url.query
    ) {

        val binaryList: List<Part>

        init {
            binaryList = (req.body as MultipartBody).parts.map { Part(it) }
        }

        override fun equals(other: Any?): Boolean {
            return if (other is MultiPart) {
                headerMap == other.headerMap &&
                        queryParams == other.queryParams &&
                        binaryList == other.binaryList
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            var result = headerMap.hashCode()
            result = 31 * result + (queryParams?.hashCode() ?: 0)
            result = 31 * result + binaryList.hashCode()
            return result
        }


        @Suppress("unused", "MemberVisibilityCanBePrivate")
        class Part(
            part: MultipartBody.Part
        ) {
            val type: MediaType?
            val bytes: ByteArray?

            init {
                type = part.body.contentType()
                bytes = getByte(part.body)
            }

            /**
             * MultipartBody to ByteArray
             */
            private fun getByte(body: RequestBody?): ByteArray? {
                if (body == null) return null
                return try {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    buffer.readByteArray()
                } catch (ex: Exception) {
                    null
                }
            }

            override fun equals(other: Any?): Boolean {
                return if (other is Part) {
                    type == other.type
                } else {
                    false
                }
            }

            override fun hashCode(): Int {
                return type?.hashCode() ?: 0
            }
        }
    }
}
