package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhTrackingBodyBinding

internal class TrackingBodyViewHolder(parent: ViewGroup) :
    BaseTrackingViewHolder<VhTrackingBodyBinding>(
        parent,
        R.layout.vh_tracking_body
    ) {
    init {
        itemView.setOnLongClickListener {
            binding.model?.runCatching {
                simpleLongClickCopy(body)
            }
            return@setOnLongClickListener false
        }
    }

    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }
}
