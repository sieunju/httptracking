package com.http.tracking.models

import com.http.tracking.R

internal data class TrackingQueryUiModel(
    val key: String = "",
    val value: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_query)
