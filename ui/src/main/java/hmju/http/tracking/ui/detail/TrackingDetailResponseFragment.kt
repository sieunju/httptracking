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
import hmju.http.tracking.models.TrackingTitleUiModel
import hmju.http.tracking.ui.TrackingBottomSheetDialog
import hmju.http.tracking.ui.adapter.TrackingAdapter
import hmju.http.tracking_interceptor.model.TrackingModel
import hmju.http.tracking_interceptor.model.TrackingResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Description : 상세 > Response Fragment
 *
 * Created by juhongmin on 2022/04/02
 */
internal class TrackingDetailResponseFragment : Fragment(R.layout.f_tracking_detail_response) {

    companion object {
        fun newInstance(): TrackingDetailResponseFragment = TrackingDetailResponseFragment()
    }

    private lateinit var rvContents: RecyclerView
    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    // Gson
    private val gson: Gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .serializeNulls()
            .create()
    }

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
        val list = mutableListOf<BaseTrackingUiModel>()
        if (data !is TrackingModel.Default) return list

        val res = data.response
        if (res is TrackingResponse.Default) {
            // Headers
            val headerMap = res.headerMap
            if (headerMap.isNotEmpty()) {
                list.add(TrackingTitleUiModel("[header]"))
                list.addAll(headerMap.map { TrackingHeaderUiModel(it) })
            }

            // Body
            try {
                val body = res.body
                val js = JsonParser.parseString(body)
                list.add(TrackingBodyUiModel(gson.toJson(js)))
            } catch (ex: Exception) {
                // ignore
            }
        }

        return list
    }
}
