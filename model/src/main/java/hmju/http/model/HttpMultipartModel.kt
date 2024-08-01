package hmju.http.model

import okhttp3.MultipartBody
import okio.Buffer

/**
 * Description : HTTP Multipart Body Model
 *
 * Created by juhongmin on 2024. 7. 29.
 */
data class HttpMultipartModel(
    val mimeType: String,
    val bytes: ByteArray? = null
) : ChildModel {

    constructor(
        part: MultipartBody.Part
    ) : this(
        mimeType = part.body.contentType().toString(),
        bytes = try {
            val buffer = Buffer()
            part.body.writeTo(buffer)
            buffer.readByteArray()
        } catch (ex: Exception) {
            null
        }
    )

    override fun equals(other: Any?): Boolean {
        return if (other is HttpMultipartModel) {
            mimeType == other.mimeType
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return mimeType.hashCode()
    }
}