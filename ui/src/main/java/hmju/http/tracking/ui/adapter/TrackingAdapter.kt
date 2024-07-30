package hmju.http.tracking.ui.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.ui.diffutil.TrackingDetailDiffUtil
import hmju.http.tracking.ui.viewholder.BaseTrackingViewHolder
import hmju.http.tracking.ui.viewholder.TrackingBodyViewHolder
import hmju.http.tracking.ui.viewholder.TrackingContentsViewHolder
import hmju.http.tracking.ui.viewholder.TrackingMultipartViewHolder
import hmju.http.tracking.ui.viewholder.TrackingSummaryViewHolder
import hmju.http.tracking.ui.viewholder.TrackingTitleViewHolder

/**
 * Description : HttpTracking 공통 어댑터
 *
 * Created by juhongmin on 2022/09/04
 */
internal class TrackingAdapter(
    private val fragment: Fragment
) : RecyclerView.Adapter<BaseTrackingViewHolder>() {

    private val dataList = mutableListOf<BaseTrackingUiModel>()

    fun submitList(
        newList: List<BaseTrackingUiModel>?
    ) {
        if (newList == null) return
        val diffResult = DiffUtil.calculateDiff(TrackingDetailDiffUtil(dataList, newList))
        dataList.clear()
        dataList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseTrackingViewHolder {
        return when (viewType) {
            R.layout.vh_tracking_title -> TrackingTitleViewHolder(parent)
            R.layout.vh_tracking_contents -> TrackingContentsViewHolder(parent)
            R.layout.vh_tracking_body -> TrackingBodyViewHolder(parent)
            R.layout.vh_tracking_multipart -> TrackingMultipartViewHolder(parent)
            R.layout.vh_tracking_summary -> TrackingSummaryViewHolder(parent, fragment)
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun onBindViewHolder(holder: BaseTrackingViewHolder, pos: Int) {
        runCatching {
            holder.onBindView(dataList[pos])
        }
    }

    override fun getItemViewType(pos: Int): Int {
        return if (dataList.size > pos) {
            dataList[pos].layoutId
        } else {
            return super.getItemViewType(pos)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}