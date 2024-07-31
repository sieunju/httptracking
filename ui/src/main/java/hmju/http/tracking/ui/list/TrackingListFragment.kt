package hmju.http.tracking.ui.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import hmju.http.tracking.R
import hmju.http.tracking.models.TrackingSummaryUiModel
import hmju.http.tracking.ui.adapter.TrackingAdapter
import hmju.http.tracking_interceptor.TrackingDataManager
import hmju.http.tracking_interceptor.model.TrackingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Description : HTTP Tracking List Fragment
 *
 * Created by juhongmin on 2023/01/06
 */
internal class TrackingListFragment : Fragment(
    R.layout.f_tracking_list
) {

    private lateinit var rvContents: RecyclerView
    private lateinit var etKeyword: AppCompatEditText

    // private val currentKeyword: MutableStateFlow<String> by lazy { MutableStateFlow("") }
    private var currentKeyword: CharSequence? = null

    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etKeyword = view.findViewById(R.id.etKeyword)
        rvContents = view.findViewById(R.id.rvContents)
        val abl: AppBarLayout = view.findViewById(R.id.abl)
        abl.setExpanded(false)
        abl.elevation = 0F
        abl.outlineProvider = null
        rvContents.layoutManager = LinearLayoutManager(view.context)
        rvContents.adapter = adapter

        initSearchKeyword()
        searchTrackingList(currentKeyword.toString())

        TrackingDataManager.getInstance().setListener(object : TrackingDataManager.Listener {
            override fun onUpdateTrackingData() {
                searchTrackingList(currentKeyword.toString())
            }
        })
    }

    private fun setTrackingData(newList: List<TrackingModel>) {
        lifecycleScope.launch {
            val uiList = withContext(Dispatchers.IO) {newList.map { TrackingSummaryUiModel(it) }}
            adapter.submitList(uiList)
        }
    }

    /**
     * Flow 를 활용한 EditText Change Observer 함수
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun EditText.textChangeObserver(): Flow<CharSequence?> {
        return callbackFlow {
            val listener = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    trySend(s)
                }

                override fun afterTextChanged(s: Editable?) {}
            }
            addTextChangedListener(listener)
            awaitClose { removeTextChangedListener(listener) }
        }.onStart {
            // 콜백이 시작 될때 Event 방출
            emit(text)
        }
    }

    @OptIn(FlowPreview::class)
    private fun initSearchKeyword() {
        etKeyword.textChangeObserver()
            .debounce(100)
            .onEach { searchTrackingList(it.toString()) }
            .launchIn(lifecycleScope)
    }

    /**
     * 검색하고자 하는 Tracking List
     * @param keyword 키워드
     */
    private fun searchTrackingList(
        keyword: String
    ) {
        currentKeyword = keyword
        val trackingList = TrackingDataManager.getInstance().getTrackingList()

        if (keyword.isEmpty() || keyword == "null") {
            setTrackingData(trackingList)
        } else {
//            val filterList = trackingList.filter { it.getPath().contains(keyword) }
            // setTrackingData(filterList)
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
