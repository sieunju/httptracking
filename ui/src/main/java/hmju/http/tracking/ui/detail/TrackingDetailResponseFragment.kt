package hmju.http.tracking.ui.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hmju.tracking.model.ChildModel
import hmju.tracking.model.ContentsModel
import hmju.tracking.model.HttpBodyModel
import hmju.tracking.model.HttpMultipartModel
import hmju.tracking.model.TitleModel
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingBodyUiModel
import hmju.http.tracking.models.TrackingContentsUiModel
import hmju.http.tracking.models.TrackingMultipartUiModel
import hmju.http.tracking.models.TrackingTitleUiModel
import hmju.http.tracking.ui.TrackingBottomSheetDialog
import hmju.http.tracking.ui.adapter.TrackingAdapter

/**
 * Description : 상세 > Response Fragment
 *
 * Created by juhongmin on 2022/04/02
 */
internal class TrackingDetailResponseFragment : Fragment(
    R.layout.f_tracking_detail_response
) {

    companion object {
        fun newInstance(): TrackingDetailResponseFragment = TrackingDetailResponseFragment()
    }

    private lateinit var rvContents: RecyclerView
    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = LinearLayoutManager(view.context)
        rvContents.adapter = adapter
        handleRequestDetailEntity()
    }

    /**
     * Request Detail 처리
     */
    private fun handleRequestDetailEntity() {
        val dialogFragment = parentFragment?.parentFragment
        if (dialogFragment !is TrackingBottomSheetDialog) return
        val data = dialogFragment.getTempDetailData()
        if (data != null) {
            adapter.submitList(data.getResModels().mapNotNull { it.toUi() })
        }
    }

    private fun ChildModel.toUi(): BaseTrackingUiModel? {
        return when (this) {
            is TitleModel -> TrackingTitleUiModel(this)
            is ContentsModel -> TrackingContentsUiModel(this)
            is HttpBodyModel -> TrackingBodyUiModel(this)
            is HttpMultipartModel -> TrackingMultipartUiModel(this)
            else -> null
        }
    }
}
