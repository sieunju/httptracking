package hmju.http.tracking.models

import com.http.tracking.R
import hmju.http.tracking_interceptor.model.HttpTrackingModel

internal data class TrackingListErrorUiModel(
    val item: HttpTrackingModel.Error
) : BaseTrackingUiModel(R.layout.vh_child_tracking_error) {
    override fun getClassName(): String {
        return "TrackingListErrorUiModel"
    }

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingListErrorUiModel) {
            item.uid == diffItem.item.uid
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingListErrorUiModel) {
            item.uid == diffItem.item.uid
        } else {
            false
        }
    }
}
