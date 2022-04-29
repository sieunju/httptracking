package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhTrackingHeaderBinding

internal class TrackingHeaderViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder<VhTrackingHeaderBinding>(parent, R.layout.vh_tracking_header) {

    init {
        binding.tvContents.setOnLongClickListener {
            binding.model?.runCatching {
                simpleLongClickCopy(value)
            }
            return@setOnLongClickListener false
        }
    }

    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }
}
