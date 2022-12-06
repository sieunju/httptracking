package com.http.tracking.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.http.tracking.Extensions
import com.http.tracking.R
import com.http.tracking.databinding.FTrackingDetailRequestBinding
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingPathUiModel
import com.http.tracking.models.TrackingTitleUiModel
import com.http.tracking.ui.adapter.TrackingAdapter
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
internal class TrackingDetailRequestFragment : Fragment() {

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

    private lateinit var binding: FTrackingDetailRequestBinding
    private val adapter: TrackingAdapter by lazy { TrackingAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<FTrackingDetailRequestBinding>(
            inflater,
            R.layout.f_tracking_detail_request,
            container,
            false
        ).run {
            lifecycleOwner = this@TrackingDetailRequestFragment.viewLifecycleOwner
            binding = this
            return@run root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvContents.adapter = adapter
    }

    /**
     * Request Detail 처리
     * @param entity 트레킹 데이터 모델
     */
    fun performDetailEntity(entity: TrackingHttpEntity) {
        lifecycleScope.launch(Dispatchers.Main) {
            val uiList = flowOf(entity)
                .map { parseUiModel(it) }
                .flowOn(Dispatchers.IO)
                .singleOrNull() ?: return@launch
            adapter.submitList(uiList)
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
