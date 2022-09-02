package com.http.tracking.models

import com.http.tracking.R

internal data class TrackingPathUiModel(
    val path: String? = null
) : BaseTrackingUiModel(R.layout.vh_tracking_path) {

    override fun getClassName() = "TrackingPathUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingPathUiModel) {
            path == diffItem.path
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingPathUiModel) {
            path == diffItem.path
        } else {
            false
        }
    }
}
