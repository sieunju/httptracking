package com.http.tracking_interceptor.model

/**
 * Description : HTTP Request Data Entity
 *
 * Created by juhongmin on 2022/03/30
 */
data class TrackingRequestEntity(
    val fullUrl : String? = null,
    val body : String? = null
)
