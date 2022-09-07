package com.http.tracking_interceptor.model

import okhttp3.MediaType

/**
 * Description : Multipart Request Data Entity
 *
 * Created by juhongmin on 2022/09/07
 */
data class TrackingRequestMultipartEntity(
    private val _fullUrl: String? = null,
    private val _mediaType: MediaType? = null,
    val binaryList: List<Part>
) : BaseTrackingRequestEntity(_fullUrl, _mediaType)
