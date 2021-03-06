package com.http.tracking.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.http.tracking.Extensions
import com.http.tracking.R
import com.http.tracking.databinding.FTrackingDetailRequestBinding
import com.http.tracking.entity.TrackingHttpEntity
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingPathUiModel
import com.http.tracking.models.TrackingTitleUiModel
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

    private lateinit var binding: FTrackingDetailRequestBinding
    private val adapter: Extensions.TrackingAdapter by lazy { Extensions.TrackingAdapter() }

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
                .singleOrNull()
            adapter.submitList(uiList)
        }
    }

    private fun parseUiModel(entity: TrackingHttpEntity): List<BaseTrackingUiModel> {
        val uiList = mutableListOf<BaseTrackingUiModel>()
        uiList.add(TrackingTitleUiModel("[Host]"))
        uiList.add(TrackingPathUiModel(entity.baseUrl))
        uiList.add(TrackingTitleUiModel("[Path]"))
        uiList.add(TrackingPathUiModel(entity.path))
        if (entity.headerMap.isNotEmpty()) {
            uiList.add(TrackingTitleUiModel("[Header]"))
            uiList.addAll(Extensions.parseHeaderUiModel(entity.headerMap))
        }
        if (!entity.req?.fullUrl.isNullOrEmpty()) {
            val queryUiModelList = Extensions.parseQueryUiModel(entity.req?.fullUrl)
            if (queryUiModelList.isNotEmpty()) {
                uiList.add(TrackingTitleUiModel("[Query]"))
                uiList.addAll(queryUiModelList)
            }
        }

        entity.req?.body?.let { body ->
            uiList.add(TrackingTitleUiModel("[Body]"))
            uiList.add(Extensions.parseBodyUiModel(body))
        }
        return uiList
    }
}
