package hmju.http.tracking.ui.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Description : HTTP Tracking List Fragment
 *
 * Created by juhongmin on 2023/01/06
 */
internal class TrackingListFragment : Fragment(R.layout.f_tracking_list) {

    private lateinit var rvContents: RecyclerView
    private lateinit var svKeyword: SearchView
    private val currentKeyword: MutableStateFlow<String> by lazy { MutableStateFlow("") }

    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        svKeyword = view.findViewById(R.id.svKeyword)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = LinearLayoutManager(view.context)
        rvContents.adapter = adapter
        handleScrollListener()
        setSearchKeyword()
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

            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
//                // 스크롤이 멈췄을때만 검색어 화면 노출 / 미노출 처리
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    if (!rv.canScrollVertically(-1)) {
//                        // 최상단 입니다.
//                        svKeyword.changeVisible(View.VISIBLE)
//                    } else {
//                        svKeyword.changeVisible(View.GONE)
//                    }
//                } else {
//                    svKeyword.changeVisible(View.GONE)
//                }
            }
        })
    }

    private fun setSearchKeyword() {
        lifecycleScope.launchWhenResumed {
            currentKeyword.collectLatest {
                delay(100)
                searchTrackingList(it)
            }
        }
        svKeyword.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentKeyword.value = newText ?: ""
                return false
            }
        })
    }

    private fun searchTrackingList(keyword: String) {
        val trackingList = TrackingDataManager.getInstance().getTrackingList()

        if (keyword.isEmpty()) {
            setTrackingData(trackingList)
        } else {
            val filterList = trackingList.filter { it.getPath().contains(keyword) }
            setTrackingData(filterList)
        }
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
