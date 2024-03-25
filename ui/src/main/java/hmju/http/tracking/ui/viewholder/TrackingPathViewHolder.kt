package hmju.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingPathUiModel

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
