package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhTrackingPathBinding

/**
 * Description : Http Path ViewHolder
 *
 * Created by juhongmin on 2022/04/03
 */
internal class TrackingPathViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder<VhTrackingPathBinding>(
    parent,
    R.layout.vh_tracking_path
) {
    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }
}
