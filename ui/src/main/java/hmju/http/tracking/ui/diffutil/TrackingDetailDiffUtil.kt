package hmju.http.tracking.ui.diffutil

import androidx.recyclerview.widget.DiffUtil
import hmju.http.tracking.models.BaseTrackingUiModel

/**
 * Description : HttpTracking Diff Util Class
 *
 * Created by juhongmin on 2022/09/04
 */
internal class TrackingDetailDiffUtil : DiffUtil.ItemCallback<BaseTrackingUiModel>() {

    override fun areItemsTheSame(
        oldItem: BaseTrackingUiModel,
        newItem: BaseTrackingUiModel
    ): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(
        oldItem: BaseTrackingUiModel,
        newItem: BaseTrackingUiModel
    ): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }
}