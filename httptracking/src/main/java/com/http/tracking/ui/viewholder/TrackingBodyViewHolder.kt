package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.http.tracking.R
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingBodyUiModel

internal class TrackingBodyViewHolder(parent: ViewGroup) :
    BaseTrackingViewHolder(
        parent,
        R.layout.vh_tracking_body
    ) {

    private val tvBody: AppCompatTextView by lazy { itemView.findViewById(R.id.tvBody) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingBodyUiModel) {
            tvBody.text = model.body
        }
    }
}
