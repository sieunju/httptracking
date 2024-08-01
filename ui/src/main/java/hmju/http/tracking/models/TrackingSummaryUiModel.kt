package hmju.http.tracking.models

import hmju.tracking.model.TrackingModel
import hmju.http.tracking.R

internal data class TrackingSummaryUiModel(
    val item: TrackingModel
) : BaseTrackingUiModel(R.layout.vh_tracking_summary) {

    override fun getClassName(): String {
        return "TrackingSummaryUiModel"
    }

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingSummaryUiModel) {
            item.uid == diffItem.item.uid
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingSummaryUiModel) {
            item.uid == diffItem.item.uid
        } else {
            false
        }
    }
}
