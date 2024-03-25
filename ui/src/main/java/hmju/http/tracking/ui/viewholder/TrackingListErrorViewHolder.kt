package hmju.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingListErrorUiModel
import hmju.http.tracking.ui.TrackingBottomSheetDialog

internal class TrackingListErrorViewHolder(
    parent: ViewGroup,
    private val fragment: Fragment
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_child_tracking_error
) {

    private val llContents: LinearLayoutCompat by lazy { itemView.findViewById(R.id.llContents) }
    private val tvMethod: AppCompatTextView by lazy { itemView.findViewById(R.id.tvMethod) }
    private val tvHost: AppCompatTextView by lazy { itemView.findViewById(R.id.tvHost) }
    private val tvPath: AppCompatTextView by lazy { itemView.findViewById(R.id.tvPath) }
    private val tvSendTime: AppCompatTextView by lazy { itemView.findViewById(R.id.tvSendTime) }

    private var model: TrackingListErrorUiModel? = null

    init {
        llContents.setOnClickListener {
            val item = model?.item ?: return@setOnClickListener
            runCatching {
                val parentFragment = fragment.parentFragment
                if (parentFragment is TrackingBottomSheetDialog) {
                    parentFragment.moveToDetailFragment(item)
                }
            }
        }
    }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model !is TrackingListErrorUiModel) return
        this.model = model
        val item = model.item
        tvMethod.text = item.method
        tvHost.text = item.host
        tvPath.text = item.path
        tvSendTime.text = item.sendTimeText
    }
}
