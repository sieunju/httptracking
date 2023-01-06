package com.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.http.tracking.R
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingQueryUiModel

internal class TrackingQueryViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_tracking_query
) {

    private val tvQuery: AppCompatTextView by lazy { itemView.findViewById(R.id.tvQuery) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingQueryUiModel) {
            tvQuery.text = model.query
        }
    }
}
