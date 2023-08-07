package hmju.http.tracking.ui.viewholder

import android.graphics.Color
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import com.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingListUiModel
import hmju.http.tracking.ui.TrackingBottomSheetDialog
import hmju.http.tracking_interceptor.model.TrackingHttpEntity

internal class TrackingListViewHolder(
    parent: ViewGroup,
    private val fragment: Fragment
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_child_tracking
) {

    private val llContents: LinearLayoutCompat by lazy { itemView.findViewById(R.id.llContents) }
    private val llBgStatus: LinearLayoutCompat by lazy { itemView.findViewById(R.id.llBgStatus) }
    private val tvResponseDate: AppCompatTextView by lazy { itemView.findViewById(R.id.tvResponseDate) }
    private val tvResponseMs: AppCompatTextView by lazy { itemView.findViewById(R.id.tvResponseMs) }
    private val tvStatusCode: AppCompatTextView by lazy { itemView.findViewById(R.id.tvStatusCode) }
    private val tvBaseUrl: AppCompatTextView by lazy { itemView.findViewById(R.id.tvBaseUrl) }
    private val tvPath: AppCompatTextView by lazy { itemView.findViewById(R.id.tvPath) }
    private val tvMethod: AppCompatTextView by lazy { itemView.findViewById(R.id.tvMethod) }

    private var data: TrackingListUiModel? = null

    init {
        llContents.setOnClickListener {
            data?.runCatching {
                val parentFragment = fragment.parentFragment
                if (parentFragment is TrackingBottomSheetDialog) {
                    parentFragment.moveToDetailFragment(this.item)
                }
            }
        }
    }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingListUiModel) {
            data = model
            setStatusColor(model.item)
            setTime(model.item)
            setHttpInfo(model.item)
        }
    }

    private fun setStatusColor(item: TrackingHttpEntity) {
        if (item.isSuccess()) {
            llBgStatus.setBackgroundColor(Color.parseColor("#03A9F4"))
        } else {
            llBgStatus.setBackgroundColor(Color.parseColor("#C62828"))
        }

        tvStatusCode.text = item.codeTxt
    }

    private fun setTime(item: TrackingHttpEntity) {
        tvResponseDate.text = item.resTimeDate
        tvResponseMs.text = item.takenTimeTxt
    }

    private fun setHttpInfo(item: TrackingHttpEntity) {
        tvBaseUrl.text = item.baseUrl
        tvPath.text = item.path
        tvMethod.text = item.method
    }
}
