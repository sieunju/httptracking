package hmju.http.tracking_interceptor.model

import okhttp3.MediaType

/**
 * Description : Request Tracking Entity
 *
 * Created by juhongmin on 2022/09/07
 */
open class BaseTrackingRequestEntity(
    open val fullUrl: String? = null,
    open val mediaType: MediaType? = null
)
