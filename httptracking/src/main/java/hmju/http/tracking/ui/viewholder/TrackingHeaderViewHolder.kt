package hmju.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.http.tracking.R
import hmju.http.tracking.models.*

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
