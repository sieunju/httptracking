package com.http.tracking.models

import com.http.tracking.R

internal data class TrackingPathUiModel(
    val path: String? = null
) : BaseTrackingUiModel(R.layout.vh_tracking_path)
