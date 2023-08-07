package hmju.http.tracking_interceptor.model

import okhttp3.MediaType

/**
 * Description : HTTP Request Data Entity
 *
 * Created by juhongmin on 2022/03/30
 */
data class TrackingRequestEntity(
    private val _fullUrl: String? = null,
    private val _mediaType: MediaType? = null,
    val body: String? = null
) : BaseTrackingRequestEntity(_fullUrl, _mediaType)
