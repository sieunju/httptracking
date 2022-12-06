package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhTrackingQueryBinding

internal class TrackingQueryViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder<VhTrackingQueryBinding>(
    parent,
    R.layout.vh_tracking_query
) {
    init {
//        itemView.setOnLongClickListener {
//            binding.model?.runCatching {
//                simpleLongClickCopy(value)
//            }
//            return@setOnLongClickListener true
//        }
    }

    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }
}
