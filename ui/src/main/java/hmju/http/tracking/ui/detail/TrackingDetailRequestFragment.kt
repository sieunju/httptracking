package hmju.http.tracking.ui.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import hmju.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingBodyUiModel
import hmju.http.tracking.models.TrackingHeaderUiModel
import hmju.http.tracking.models.TrackingMultipartBodyUiModel
import hmju.http.tracking.models.TrackingPathUiModel
import hmju.http.tracking.models.TrackingQueryUiModel
import hmju.http.tracking.models.TrackingTitleUiModel
import hmju.http.tracking.ui.TrackingBottomSheetDialog
import hmju.http.tracking.ui.adapter.TrackingAdapter
import hmju.http.tracking_interceptor.model.TrackingModel
import hmju.http.tracking_interceptor.model.TrackingRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder

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
            val data = dialogFragment.getTempDetailData()
            if (data != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val uiList = withContext(Dispatchers.IO) { parseUiModel(data) }
                    adapter.submitList(uiList)
                }
            }
        }
    }

    private fun parseUiModel(
        data: TrackingModel
    ): List<BaseTrackingUiModel> {
        return when (data) {
            is TrackingModel.Default -> getDefaultUiModels(data)
            is TrackingModel.TimeOut -> getTimeOutUiModels(data)
            is TrackingModel.Error -> getErrorUiModels(data)
        }
    }

    /**
     * 일반적인 모델 UiModel로 변환해서 리턴하는 함수
     *
     * @param data [TrackingModel.Default]
     */
    private fun getDefaultUiModels(
        data: TrackingModel.Default
    ): List<BaseTrackingUiModel> {
        val list = mutableListOf<BaseTrackingUiModel>()
        // FullURL
        list.add(TrackingPathUiModel(data.fullUrl))
        // Path
        list.add(TrackingTitleUiModel("[path]"))
        list.add(TrackingPathUiModel(data.path))

        // Headers
        val headerMap = data.request.headerMap
        if (headerMap.isNotEmpty()) {
            list.add(TrackingTitleUiModel("[header]"))
            list.addAll(headerMap.map { TrackingHeaderUiModel(it) })
        }

        // QueryParams
        val queryParams = data.request.queryParams?.split("&")
        if (!queryParams.isNullOrEmpty()) {
            list.add(TrackingTitleUiModel("[query]"))
            val str = StringBuilder()
            queryParams.forEach {
                val query = splitQuery(it) ?: return@forEach
                str.append(query.first)
                str.append("=")
                str.append(query.second)
                str.append("\n")
            }
            list.add(TrackingQueryUiModel(str.toString()))
        }

        // Body
        val request = data.request
        if (request is TrackingRequest.Default) {
            val body = request.body
            try {
                val js = JsonParser.parseString(body)
                list.add(TrackingBodyUiModel(gson.toJson(js)))
            } catch (ex: Exception) {
                // ignore
            }
        } else if (request is TrackingRequest.MultiPart) {
            request.binaryList.forEach { list.add(TrackingMultipartBodyUiModel(it)) }
        }
        return list
    }

    private fun getTimeOutUiModels(
        data: TrackingModel.TimeOut
    ): List<BaseTrackingUiModel> {
        val list = mutableListOf<BaseTrackingUiModel>()
        list.add(TrackingTitleUiModel("[timeout]"))
        // FullURL
        list.add(TrackingPathUiModel(data.fullUrl))
        // Path
        list.add(TrackingTitleUiModel("[path]"))
        list.add(TrackingPathUiModel(data.path))

        // Headers
        val headerMap = data.request.headerMap
        if (headerMap.isNotEmpty()) {
            list.add(TrackingTitleUiModel("[header]"))
            list.addAll(headerMap.map { TrackingHeaderUiModel(it) })
        }

        // QueryParams
        val queryParams = data.request.queryParams?.split("&")
        if (!queryParams.isNullOrEmpty()) {
            list.add(TrackingTitleUiModel("[query]"))
            val str = StringBuilder()
            queryParams.forEach {
                val query = splitQuery(it) ?: return@forEach
                str.append(query.first)
                str.append("=")
                str.append(query.second)
                str.append("\n")
            }
            list.add(TrackingQueryUiModel(str.toString()))
        }

        // Body
        val request = data.request
        if (request is TrackingRequest.Default) {
            val body = request.body
            try {
                val js = JsonParser.parseString(body)
                TrackingBodyUiModel(gson.toJson(js))
            } catch (ex: Exception) {
                // ignore
            }
        } else if (request is TrackingRequest.MultiPart) {
            request.binaryList.forEach { list.add(TrackingMultipartBodyUiModel(it)) }
        }
        return list
    }

    private fun getErrorUiModels(
        data: TrackingModel.Error
    ): List<BaseTrackingUiModel> {
        val list = mutableListOf<BaseTrackingUiModel>()
        list.add(TrackingTitleUiModel("[Error]"))
        list.add(TrackingPathUiModel(data.msg))
        // FullURL
        list.add(TrackingPathUiModel(data.fullUrl))
        // Path
        list.add(TrackingTitleUiModel("[path]"))
        list.add(TrackingPathUiModel(data.path))

        // Headers
        val headerMap = data.request.headerMap
        if (headerMap.isNotEmpty()) {
            list.add(TrackingTitleUiModel("[header]"))
            list.addAll(headerMap.map { TrackingHeaderUiModel(it) })
        }

        // QueryParams
        val queryParams = data.request.queryParams?.split("&")
        if (!queryParams.isNullOrEmpty()) {
            list.add(TrackingTitleUiModel("[query]"))
            val str = StringBuilder()
            queryParams.forEach {
                val query = splitQuery(it) ?: return@forEach
                str.append(query.first)
                str.append("=")
                str.append(query.second)
                str.append("\n")
            }
            list.add(TrackingQueryUiModel(str.toString()))
        }

        // Body
        val request = data.request
        if (request is TrackingRequest.Default) {
            val body = request.body
            try {
                val js = JsonParser.parseString(body)
                TrackingBodyUiModel(gson.toJson(js))
            } catch (ex: Exception) {
                // ignore
            }
        } else if (request is TrackingRequest.MultiPart) {
            request.binaryList.forEach { list.add(TrackingMultipartBodyUiModel(it)) }
        }
        return list
    }

    /**
     * Split HTTP Query
     *
     * @param txt {Key=Value}
     */
    private fun splitQuery(txt: String): Pair<String, String>? {
        val idx = txt.indexOf("=")
        return if (idx != -1) {
            var key = txt.substring(0, idx)
            key = try {
                URLDecoder.decode(key, Charsets.UTF_8.name())
            } catch (ex: UnsupportedOperationException) {
                key
            } catch (ex: IllegalArgumentException) {
                key
            }
            var value = txt.substring(idx.plus(1))
            value = try {
                URLDecoder.decode(value, Charsets.UTF_8.name())
            } catch (ex: UnsupportedOperationException) {
                value
            } catch (ex: IllegalArgumentException) {
                value
            }
            key to value
        } else {
            null
        }
    }
}
