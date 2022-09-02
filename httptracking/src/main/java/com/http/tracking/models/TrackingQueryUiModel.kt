package com.http.tracking.models

import com.http.tracking.R

internal data class TrackingQueryUiModel(
    val key: String = "",
    val value: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_query) {

    override fun getClassName() = "TrackingQueryUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingQueryUiModel) {
            key == diffItem.key
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingQueryUiModel) {
            key == diffItem.key && value == diffItem.value
        } else {
            false
        }
    }
}
