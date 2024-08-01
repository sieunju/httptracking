package hmju.http.tracking.models

import hmju.tracking.model.TitleModel
import hmju.http.tracking.R

internal data class TrackingTitleUiModel(
    val item: TitleModel
) : BaseTrackingUiModel(R.layout.vh_tracking_title) {

    override fun getClassName() = "TrackingTitleUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingTitleUiModel) {
            item == diffItem.item
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingTitleUiModel) {
            item == diffItem.item
        } else {
            false
        }
    }
}
