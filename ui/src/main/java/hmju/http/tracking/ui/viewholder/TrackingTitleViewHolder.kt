package hmju.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingTitleUiModel

internal class TrackingTitleViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_tracking_title
) {

    private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingTitleUiModel) {
            tvTitle.text = model.title
        }
    }
}
