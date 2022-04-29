package com.http.tracking.models

import com.http.tracking.R
import com.http.tracking.entity.TrackingHttpEntity

internal data class TrackingListUiModel(
    val item: TrackingHttpEntity
) : BaseTrackingUiModel(R.layout.vh_child_tracking)
