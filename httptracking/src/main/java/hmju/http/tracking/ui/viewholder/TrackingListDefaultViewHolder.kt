package hmju.http.tracking.ui.viewholder

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import com.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingListDefaultUiModel
import hmju.http.tracking.ui.TrackingBottomSheetDialog

internal class TrackingListDefaultViewHolder(
    parent: ViewGroup,
    private val fragment: Fragment
) : BaseTrackingViewHolder(
    parent,
    R.layout.vh_child_tracking
) {

    private val llContents: LinearLayoutCompat by lazy { itemView.findViewById(R.id.llContents) }
    private val llBgStatus: LinearLayoutCompat by lazy { itemView.findViewById(R.id.llBgStatus) }
    private val tvMethod: AppCompatTextView by lazy { itemView.findViewById(R.id.tvMethod) }
    private val tvStatusCode: AppCompatTextView by lazy { itemView.findViewById(R.id.tvStatusCode) }
    private val tvTakeTimeMs: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTakeTimeMs) }
    private val tvHost: AppCompatTextView by lazy { itemView.findViewById(R.id.tvHost) }
    private val tvPath: AppCompatTextView by lazy { itemView.findViewById(R.id.tvPath) }
    private val tvTimeDate: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTimeDate) }

    private var model: TrackingListDefaultUiModel? = null

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
        if (model !is TrackingListDefaultUiModel) return
        this.model = model
        val item = model.item
        if (item.isSuccess) {
            llBgStatus.changeBgColor(Color.parseColor("#03A9F4"))
        } else {
            llBgStatus.changeBgColor(Color.parseColor("#C62828"))
        }
        tvMethod.changeText(item.method)
        tvStatusCode.changeText("${item.code}")
        tvTakeTimeMs.changeText("${item.takeTimeMs}MS")
        tvHost.changeText(item.host)
        tvPath.changeText(item.path)
        tvTimeDate.changeText(item.timeDate)
    }

    private fun View.changeBgColor(@ColorInt color: Int) {
        val bg = background
        if (bg is ColorDrawable && bg.color != color) {
            bg.color = color
        } else {
            setBackgroundColor(color)
        }
    }

    private fun AppCompatTextView.changeText(str: CharSequence) {
        if (text != str) {
            text = str
        }
    }
}
