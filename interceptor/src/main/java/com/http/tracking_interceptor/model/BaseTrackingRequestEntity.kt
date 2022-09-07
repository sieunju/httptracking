package com.http.tracking_interceptor.model

import okhttp3.MediaType

/**
 * Description : Request Tracking Entity
 *
 * Created by juhongmin on 2022/09/07
 */
open class BaseTrackingRequestEntity(
    val fullUrl : String? = null,
    val mediaType : MediaType? = null
)
