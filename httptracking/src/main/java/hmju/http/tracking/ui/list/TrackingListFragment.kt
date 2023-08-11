package hmju.http.tracking.ui.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingListDefaultUiModel
import hmju.http.tracking.models.TrackingListErrorUiModel
import hmju.http.tracking.models.TrackingListTimeOutUiModel
import hmju.http.tracking.ui.adapter.TrackingAdapter
import hmju.http.tracking_interceptor.TrackingDataManager
import hmju.http.tracking_interceptor.model.TrackingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Description : HTTP Tracking List Fragment
 *
 * Created by juhongmin on 2023/01/06
 */
internal class TrackingListFragment : Fragment(R.layout.f_tracking_list) {

    private lateinit var rvContents: RecyclerView
    private lateinit var etKeyword: AppCompatEditText

    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etKeyword = view.findViewById(R.id.etKeyword)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = LinearLayoutManager(view.context)
        rvContents.adapter = adapter
        handleScrollListener()
        setTrackingData(TrackingDataManager.getInstance().getTrackingList())

        TrackingDataManager.getInstance().setListener(object : TrackingDataManager.Listener {
            override fun onUpdateTrackingData() {
                setTrackingData(TrackingDataManager.getInstance().getTrackingList())
            }
        })
    }

    private fun setTrackingData(newList: List<TrackingModel>) {
        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            val uiList = withContext(Dispatchers.IO) { newList.map { toUiModel(it) } }
            adapter.submitList(uiList)
        }
    }

    private fun toUiModel(model: TrackingModel): BaseTrackingUiModel {
        return when (model) {
            is TrackingModel.Default -> TrackingListDefaultUiModel(model)
            is TrackingModel.TimeOut -> TrackingListTimeOutUiModel(model)
            is TrackingModel.Error -> TrackingListErrorUiModel(model)
        }
    }

    private fun handleScrollListener() {
        rvContents.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!rv.canScrollVertically(-1)) {
                    // 최상단 입니다.
                    etKeyword.changeVisible(View.VISIBLE)
                } else {
                    etKeyword.changeVisible(View.GONE)
                }
            }
        })
    }

    private fun View.changeVisible(visible: Int) {
        if (visibility != visible) {
            visibility = visible
        }
    }

    companion object {
        fun newInstance(): TrackingListFragment = TrackingListFragment()
    }
}
