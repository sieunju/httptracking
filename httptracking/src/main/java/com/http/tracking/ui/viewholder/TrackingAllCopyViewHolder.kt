package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhTrackingAllCopyBinding

internal class TrackingAllCopyViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder<VhTrackingAllCopyBinding>(
    parent,
    R.layout.vh_tracking_all_copy
) {
    init {
        binding.llCopy.setOnClickListener {
            binding.model?.runCatching {
                simpleLongClickCopy(msg)
            }
        }
    }

    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }
}
