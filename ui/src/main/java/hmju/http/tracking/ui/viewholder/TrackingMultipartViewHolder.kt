package hmju.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingMultipartUiModel

internal class TrackingMultipartViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(
    parent, R.layout.vh_tracking_multipart
) {

    private val ivThumb: AppCompatImageView by lazy { itemView.findViewById(R.id.ivThumb) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model !is TrackingMultipartUiModel) return
        if (model.item.mimeType.startsWith("image")) {
            ivThumb.changeVisible(true)
            ivThumb.setImageBitmap(model.bitmap)
        } else {
            ivThumb.changeVisible(false)
        }
    }
}
