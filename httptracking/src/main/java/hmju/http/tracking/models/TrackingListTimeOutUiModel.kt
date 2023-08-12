package hmju.http.tracking.models

import com.http.tracking.R
import hmju.http.tracking_interceptor.model.TrackingModel

internal data class TrackingListTimeOutUiModel(
    val item : TrackingModel.TimeOut
) : BaseTrackingUiModel(R.layout.vh_child_tracking_time_out) {
    override fun getClassName(): String {
        return "TrackingListTimeOutUiModel"
    }

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingListTimeOutUiModel) {
            item.uid == diffItem.item.uid
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingListTimeOutUiModel) {
            item.uid == diffItem.item.uid
        } else {
            false
        }
    }
}
