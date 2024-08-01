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
import hmju.tracking.model.SummaryModel
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingSummaryUiModel
import hmju.http.tracking.ui.adapter.TrackingAdapter
import hmju.http.tracking_interceptor.TrackingDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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

    private var currentKeyword: CharSequence? = null
    private var debounceTime = System.currentTimeMillis()
    private var listJob: Job? = null

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
        rvContents.itemAnimator = null
        rvContents.adapter = adapter

        initSearchKeyword()
        initDebounceList()
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
            .onEach {
                currentKeyword = it
                handleSetList(it.toString())
            }
            .launchIn(lifecycleScope)
    }

    /**
     * 1초 단위로 리스트 셋팅하는 함수
     */
    private fun initDebounceList() {
        var prevTime = System.currentTimeMillis()
        val delay = 1000
        TrackingDataManager.getInstance().setListener {
            if (System.currentTimeMillis().minus(prevTime) > delay) {
                handleSetList(currentKeyword.toString())
                prevTime = System.currentTimeMillis()
            }
        }
    }

    /**
     * Set UiList
     * @param keyword 검색할 키워드
     */
    private fun handleSetList(
        keyword: String
    ) {
        debounceTime = System.currentTimeMillis()
        listJob?.cancel()
        listJob = lifecycleScope.launch(Dispatchers.Main) {
            val newList = withContext(Dispatchers.IO) {
                val originList = TrackingDataManager.getInstance().getTrackingList()
                val list = mutableListOf<BaseTrackingUiModel>()
                originList.forEach {
                    if (keyword.isEmpty() || keyword == "null") {
                        list.add(TrackingSummaryUiModel(it))
                    } else if (isFindKeyword(keyword, it.summaryModel)) {
                        list.add(TrackingSummaryUiModel(it))
                    }
                }
                return@withContext list
            }
            adapter.submitList(newList.toList())
        }
    }

    private fun isFindKeyword(
        keyword: String,
        model: SummaryModel
    ): Boolean {
        return model.titleList.find { it.contains(keyword, true) } != null ||
                model.contentsList.find { it.contains(keyword, true) } != null
    }

    companion object {
        fun newInstance(): TrackingListFragment = TrackingListFragment()
    }
}
