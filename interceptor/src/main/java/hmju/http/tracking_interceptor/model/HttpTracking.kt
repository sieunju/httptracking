package hmju.http.tracking_interceptor.model

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer

/**
 * Description : HttpTracking
 *
 * Created by juhongmin on 2023/08/09
 */
sealed class HttpTracking {
    internal data class Request(
        val headerMap: Map<String, String> = mapOf(),
        val queryParams: String? = null,
        val body: String? = null
    ) : HttpTracking()

    internal data class RequestMultiPart(
        val headerMap: Map<String, String> = mapOf(),
        val queryParams: String? = null,
        val binaryList: List<MultiPart>
    ) : HttpTracking() {

        class MultiPart(
            part: MultipartBody.Part
        ) {
            val type: MediaType?
            val byte: ByteArray?

            init {
                type = part.body.contentType()
                byte = getByte(part.body)
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
                return if (other is MultiPart) {
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

    internal data class Response(
        val headerMap: Map<String, String> = mapOf(),
        val body: String? = null
    ) : HttpTracking()
}
