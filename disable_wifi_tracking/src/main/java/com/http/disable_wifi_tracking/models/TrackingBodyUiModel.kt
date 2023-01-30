package com.http.disable_wifi_tracking.models

import com.http.disable_wifi_tracking.R

internal data class TrackingBodyUiModel(
    val body: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_body) {

    override fun getClassName() = "TrackingBodyUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingBodyUiModel) {
            body == diffItem.body
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingBodyUiModel) {
            body == diffItem.body
        } else {
            false
        }
    }
}