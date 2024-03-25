package hmju.http.tracking.ui.diffutil

import androidx.recyclerview.widget.DiffUtil
import hmju.http.tracking.models.BaseTrackingUiModel

/**
 * Description : HttpTracking Diff Util Class
 *
 * Created by juhongmin on 2022/09/04
 */
internal class TrackingDetailDiffUtil<out T : BaseTrackingUiModel>(
    private val oldList: List<T>,
    private val newList: List<T>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        val oldItem = oldList[oldPosition]
        val newItem = newList[newPosition]
        return if (oldItem.getClassName() == newItem.getClassName()) {
            oldItem.areItemsTheSame(newItem)
        } else {
            false
        }
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        val oldItem = oldList[oldPosition]
        val newItem = newList[newPosition]
        return if (oldItem.getClassName() == newItem.getClassName()) {
            oldItem.areContentsTheSame(newItem)
        } else {
            false
        }
    }
}