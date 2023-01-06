package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhChildTrackingBinding
import com.http.tracking.ui.TrackingBottomSheetDialog

internal class TrackingListViewHolder(
    parent: ViewGroup,
    private val fragment: Fragment
) : BaseTrackingViewHolder<VhChildTrackingBinding>(
    parent,
    R.layout.vh_child_tracking
) {
    init {
        binding.llContents.setOnClickListener {
            binding.model?.runCatching {
                val parentFragment = fragment.parentFragment
                if (parentFragment is TrackingBottomSheetDialog) {
                    parentFragment.moveToDetailFragment(this.item)
                }
            }
        }
    }

    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }
}
