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

    private val tv: AppCompatTextView by lazy { itemView.findViewById(R.id.tv) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model !is TrackingTitleUiModel) return
        tv.changeColor(model.item.hexCode)
        tv.text = model.item.text
    }
}
