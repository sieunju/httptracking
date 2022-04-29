package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhTrackingTitleBinding

internal class TrackingTitleViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder<VhTrackingTitleBinding>(
    parent,
    R.layout.vh_tracking_title
) {
    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }
}
