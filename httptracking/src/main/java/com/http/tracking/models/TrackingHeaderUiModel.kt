package com.http.tracking.models

import com.http.tracking.R

internal class TrackingHeaderUiModel(
    val key: String,
    val value: String
) : BaseTrackingUiModel(R.layout.vh_tracking_header) {

    override fun getClassName() = "TrackingHeaderUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingHeaderUiModel) {
            key == diffItem.key
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingHeaderUiModel) {
            key == diffItem.key && value == diffItem.value
        } else {
            false
        }
    }
}
