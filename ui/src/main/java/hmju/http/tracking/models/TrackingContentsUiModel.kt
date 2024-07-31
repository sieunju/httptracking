package hmju.http.tracking.models

import hmju.http.tracking.R
import hmju.http.tracking_interceptor.model.ContentsModel

internal data class TrackingContentsUiModel(
    val item: ContentsModel
) : BaseTrackingUiModel(R.layout.vh_tracking_contents) {
    override fun getClassName() = "TrackingBodyUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingContentsUiModel) {
            item == diffItem.item
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingContentsUiModel) {
            item == diffItem.item
        } else {
            false
        }
    }
}