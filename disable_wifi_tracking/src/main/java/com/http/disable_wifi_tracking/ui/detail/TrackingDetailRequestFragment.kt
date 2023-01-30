package com.http.disable_wifi_tracking.ui.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.http.disable_wifi_tracking.Extensions
import com.http.disable_wifi_tracking.R
import com.http.disable_wifi_tracking.models.BaseTrackingUiModel
import com.http.disable_wifi_tracking.models.TrackingPathUiModel
import com.http.disable_wifi_tracking.models.TrackingTitleUiModel
import com.http.disable_wifi_tracking.ui.TrackingBottomSheetDialog
import com.http.disable_wifi_tracking.ui.adapter.TrackingAdapter
import com.http.tracking_interceptor.model.TrackingHttpEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch

/**
 * Description : HTTP 요청 상세 Fragment
 *
 * Created by juhongmin on 2022/04/02
 */
internal class TrackingDetailRequestFragment : Fragment(R.layout.f_tracking_detail_request) {

    companion object {
        fun newInstance(): TrackingDetailRequestFragment = TrackingDetailRequestFragment()
    }

    // Gson
    private val gson: Gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .serializeNulls()
            .create()
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
        if (dialogFragment is TrackingBottomSheetDialog) {
            val detailEntity = dialogFragment.getTempDetailData()
            if (detailEntity != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val uiList = flowOf(detailEntity)
                        .map { parseUiModel(it) }
                        .flowOn(Dispatchers.IO)
                        .singleOrNull() ?: return@launch
                    adapter.submitList(uiList)
                }
            }
        }
    }

    /**
     * Request UiModel 변환 함수
     */
    private fun parseUiModel(entity: TrackingHttpEntity): List<BaseTrackingUiModel> {
        val uiList = mutableListOf<BaseTrackingUiModel>()
        // All Copy
        val req = entity.req
        if (req != null) {
            val fullCopy = StringBuilder()
            fullCopy.append(req.fullUrl)
            uiList.add(TrackingPathUiModel(fullCopy.toString()))
        }

        // host and path
        uiList.add(TrackingTitleUiModel("[path]"))
        uiList.add(TrackingPathUiModel(entity.path))

        // 헤더값 셋팅
        if (entity.headerMap.isNotEmpty()) {
            uiList.add(TrackingTitleUiModel("[header]"))
            uiList.addAll(Extensions.parseHeaderUiModel(entity.headerMap))
        }
        // 쿼리 파라미터 셋팅
        if (!entity.req?.fullUrl.isNullOrEmpty()) {
            val queryUiModelList = Extensions.parseQueryUiModel(entity.req?.fullUrl)
            if (queryUiModelList.isNotEmpty()) {
                uiList.add(TrackingTitleUiModel("[query]"))
                uiList.addAll(queryUiModelList)
            }
        }

        // 바디 값 셋팅
        val bodyModels = Extensions.toReqBodyUiModels(req)
        if (bodyModels.isNotEmpty()) {
            uiList.add(TrackingTitleUiModel("[body]"))
            uiList.addAll(bodyModels)
        }
        return uiList
    }
}
