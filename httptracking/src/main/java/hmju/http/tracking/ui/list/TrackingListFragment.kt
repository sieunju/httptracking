package hmju.http.tracking.ui.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
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
    private lateinit var etKeyword: AppCompatEditText
    private lateinit var appBarLayout: AppBarLayout
    private val currentKeyword: MutableStateFlow<String> by lazy { MutableStateFlow("") }

    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etKeyword = view.findViewById(R.id.etKeyword)
        rvContents = view.findViewById(R.id.rvContents)
        appBarLayout = view.findViewById(R.id.abl)
        appBarLayout.setExpanded(false)
        rvContents.layoutManager = LinearLayoutManager(view.context)
        rvContents.adapter = adapter

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

    private fun setSearchKeyword() {
        lifecycleScope.launchWhenResumed {
            currentKeyword.collectLatest {
                delay(100)
                searchTrackingList(it)
            }
        }
        etKeyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                currentKeyword.value = s.toString()
            }
        })
    }

    /**
     * 검색하고자 하는 Tracking List
     * @param keyword 키워드
     */
    private fun searchTrackingList(
        keyword: String
    ) {
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
