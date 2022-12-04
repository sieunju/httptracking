package com.http.tracking.models

import com.http.tracking.R

internal class TrackingAllCopyUiModel(
    val msg: String
): BaseTrackingUiModel(R.layout.vh_tracking_all_copy) {
    override fun getClassName() = "TrackingAllCopyUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingAllCopyUiModel) {
            msg == diffItem.msg
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingAllCopyUiModel) {
            msg == diffItem.msg
        } else {
            false
        }
    }
}