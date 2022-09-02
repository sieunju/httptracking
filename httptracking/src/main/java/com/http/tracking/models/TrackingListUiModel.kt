package com.http.tracking.models

import com.http.tracking.R
import com.http.tracking_interceptor.model.TrackingHttpEntity

internal data class TrackingListUiModel(
    val item: TrackingHttpEntity
) : BaseTrackingUiModel(R.layout.vh_child_tracking) {

    override fun getClassName() = "TrackingListUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingListUiModel) {
            item.uid == diffItem.item.uid
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingListUiModel) {
            item == diffItem.item
        } else {
            false
        }
    }
}
