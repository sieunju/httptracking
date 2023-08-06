package hmju.http.tracking_interceptor.model

import okhttp3.MediaType

/**
 * Description : Multipart Request Data Entity
 *
 * Created by juhongmin on 2022/09/07
 */
data class TrackingRequestMultipartEntity(
    override val fullUrl: String? = null,
    override val mediaType: MediaType? = null,
    val binaryList: List<Part>
) : BaseTrackingRequestEntity(fullUrl, mediaType)
