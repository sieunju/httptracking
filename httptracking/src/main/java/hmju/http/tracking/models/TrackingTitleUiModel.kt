package hmju.http.tracking.models

import com.http.tracking.R

internal data class TrackingTitleUiModel(
    val title: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_title) {

    override fun getClassName() = "TrackingTitleUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingTitleUiModel) {
            title == diffItem.title
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingTitleUiModel) {
            title == diffItem.title
        } else {
            false
        }
    }
}
