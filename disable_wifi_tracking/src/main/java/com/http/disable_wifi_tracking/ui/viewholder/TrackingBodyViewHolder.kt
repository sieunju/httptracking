package com.http.disable_wifi_tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.http.disable_wifi_tracking.R
import com.http.disable_wifi_tracking.models.BaseTrackingUiModel
import com.http.disable_wifi_tracking.models.TrackingBodyUiModel

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
