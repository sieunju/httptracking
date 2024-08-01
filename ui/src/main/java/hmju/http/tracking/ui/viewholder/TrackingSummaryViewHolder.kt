package hmju.http.tracking.ui.viewholder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingSummaryUiModel
import hmju.http.tracking.ui.TrackingBottomSheetDialog

internal class TrackingSummaryViewHolder(
    parent: ViewGroup,
    private val fragment: Fragment
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_tracking_summary
) {

    private val llContents: LinearLayoutCompat by lazy { itemView.findViewById(R.id.llContents) }
    private val llBgStatus: LinearLayoutCompat by lazy { itemView.findViewById(R.id.llBgStatus) }
    private val tvTitle1: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle1) }
    private val tvTitle2: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle2) }
    private val tvTitle3: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle3) }
    private val tvContents1: AppCompatTextView by lazy { itemView.findViewById(R.id.tvContents1) }
    private val tvContents2: AppCompatTextView by lazy { itemView.findViewById(R.id.tvContents2) }
    private val tvContents3: AppCompatTextView by lazy { itemView.findViewById(R.id.tvContents3) }
    private var model: TrackingSummaryUiModel? = null

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
        if (model !is TrackingSummaryUiModel) return
        this.model = model
        val summary = model.item.getSummaryModel()
        llBgStatus.changeBgColor(summary.colorHexCode)
        summary.titleList.getOrNull(0)?.let {
            tvTitle1.changeText(it)
            tvTitle1.changeVisible(true)
        } ?: run { tvTitle1.changeVisible(false) }
        summary.titleList.getOrNull(1)?.let {
            tvTitle2.changeText(it)
            tvTitle2.changeVisible(true)
        } ?: run { tvTitle2.changeVisible(false) }
        summary.titleList.getOrNull(2)?.let {
            tvTitle3.changeText(it)
            tvTitle3.changeVisible(true)
        } ?: run { tvTitle3.changeVisible(false) }
        summary.contentsList.getOrNull(0)?.let {
            tvContents1.changeText(it)
            tvContents1.changeVisible(true)
        } ?: run { tvContents1.changeVisible(false) }
        summary.contentsList.getOrNull(1)?.let {
            tvContents2.changeText(it)
            tvContents2.changeVisible(true)
        } ?: run { tvContents2.changeVisible(false) }
        summary.contentsList.getOrNull(2)?.let {
            tvContents3.changeText(it)
            tvContents3.changeVisible(true)
        } ?: run { tvContents3.changeVisible(false) }
    }
}
