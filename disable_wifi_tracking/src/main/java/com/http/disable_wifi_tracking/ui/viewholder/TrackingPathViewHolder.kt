package com.http.disable_wifi_tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.http.disable_wifi_tracking.R
import com.http.disable_wifi_tracking.models.BaseTrackingUiModel
import com.http.disable_wifi_tracking.models.TrackingPathUiModel

/**
 * Description : Http Path ViewHolder
 *
 * Created by juhongmin on 2022/04/03
 */
internal class TrackingPathViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_tracking_path
) {

    private val tvPath: AppCompatTextView by lazy { itemView.findViewById(R.id.tvPath) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingPathUiModel) {
            tvPath.text = model.path
        }
    }
}
