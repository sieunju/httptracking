package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhChildTrackingBinding
import com.http.tracking.ui.TrackingBottomSheetDialog

internal class TrackingListViewHolder(
    parent: ViewGroup,
    dialog: TrackingBottomSheetDialog? = null
) : BaseTrackingViewHolder<VhChildTrackingBinding>(
    parent,
    R.layout.vh_child_tracking
) {
    init {
        binding.llContents.setOnClickListener {
            binding.model?.runCatching {
                dialog?.performDetail(item)
            }
        }
    }

    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }
}
