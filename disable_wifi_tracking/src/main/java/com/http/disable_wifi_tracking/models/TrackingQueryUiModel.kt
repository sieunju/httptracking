package com.http.disable_wifi_tracking.models

import com.http.disable_wifi_tracking.R

internal data class TrackingQueryUiModel(
    val query: String
) : BaseTrackingUiModel(R.layout.vh_tracking_query) {

    override fun getClassName() = "TrackingQueryUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingQueryUiModel) {
            query == diffItem.query
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingQueryUiModel) {
            query == diffItem.query
        } else {
            false
        }
    }
}
