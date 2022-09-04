package com.http.tracking.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.http.tracking.R
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.ui.TrackingBottomSheetDialog
import com.http.tracking.ui.diffutil.TrackingDetailDiffUtil
import com.http.tracking.ui.viewholder.*

/**
 * Description : HttpTracking 공통 어댑터
 *
 * Created by juhongmin on 2022/09/04
 */
internal class TrackingAdapter : RecyclerView.Adapter<BaseTrackingViewHolder<*>>() {

    private val dataList = mutableListOf<BaseTrackingUiModel>()

    private var dialog: TrackingBottomSheetDialog? = null

    fun submitList(newList: List<BaseTrackingUiModel>?) {
        if (newList == null) return
        val diffResult = DiffUtil.calculateDiff(
            TrackingDetailDiffUtil(
                dataList,
                newList
            )
        )
        dataList.clear()
        dataList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setBottomSheetDialog(dialog: TrackingBottomSheetDialog) {
        this.dialog = dialog
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseTrackingViewHolder<*> {
        return when (viewType) {
            R.layout.vh_tracking_header -> TrackingHeaderViewHolder(parent)
            R.layout.vh_tracking_path -> TrackingPathViewHolder(parent)
            R.layout.vh_tracking_query -> TrackingQueryViewHolder(parent)
            R.layout.vh_tracking_body -> TrackingBodyViewHolder(parent)
            R.layout.vh_tracking_title -> TrackingTitleViewHolder(parent)
            R.layout.vh_child_tracking -> TrackingListViewHolder(parent, dialog)
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun onBindViewHolder(holder: BaseTrackingViewHolder<*>, pos: Int) {
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