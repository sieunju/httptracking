package com.http.tracking.models

import com.http.tracking.R

internal data class TrackingBodyUiModel(
    val body: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_body)