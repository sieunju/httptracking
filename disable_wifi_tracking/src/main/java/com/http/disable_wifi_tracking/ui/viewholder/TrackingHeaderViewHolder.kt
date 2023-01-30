package com.http.disable_wifi_tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.http.disable_wifi_tracking.R
import com.http.disable_wifi_tracking.models.BaseTrackingUiModel
import com.http.disable_wifi_tracking.models.TrackingHeaderUiModel

internal class TrackingHeaderViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(parent, R.layout.vh_tracking_header) {

    private val tvContents: AppCompatTextView by lazy { itemView.findViewById(R.id.tvContents) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingHeaderUiModel) {
            tvContents.text = model.contents
        }
    }
}
