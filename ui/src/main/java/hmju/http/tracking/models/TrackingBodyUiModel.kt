package hmju.http.tracking.models

import hmju.http.tracking.R

internal data class TrackingBodyUiModel(
    val body: CharSequence
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