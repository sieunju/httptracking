package hmju.http.tracking.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.http.tracking.R
import hmju.http.tracking.models.BaseTrackingUiModel
import hmju.http.tracking.models.TrackingListDefaultUiModel
import hmju.http.tracking.models.TrackingListTimeOutUiModel
import hmju.http.tracking.ui.adapter.TrackingAdapter
import hmju.http.tracking_interceptor.TrackingDataManager
import hmju.http.tracking_interceptor.model.HttpTrackingModel
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

    private val adapter: TrackingAdapter by lazy { TrackingAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = LinearLayoutManager(view.context)
        rvContents.adapter = adapter

        setTrackingData(TrackingDataManager.getInstance().getTrackingListV2())

        TrackingDataManager.getInstance().setListener(object : TrackingDataManager.Listener {
            override fun onUpdateTrackingData() {
                setTrackingData(TrackingDataManager.getInstance().getTrackingListV2())
            }
        })
    }

    private fun setTrackingData(newList: List<HttpTrackingModel>) {
        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            val uiList = withContext(Dispatchers.IO) { newList.map { toUiModel(it) } }
            adapter.submitList(uiList)
        }
    }

    private fun toUiModel(model: HttpTrackingModel): BaseTrackingUiModel {
        return when (model) {
            is HttpTrackingModel.Default -> TrackingListDefaultUiModel(model)
            is HttpTrackingModel.TimeOut -> TrackingListTimeOutUiModel(model)
            is HttpTrackingModel.Error -> throw IllegalArgumentException("ㅇㅇㅇ")
        }
    }

    companion object {
        fun newInstance(): TrackingListFragment = TrackingListFragment()
    }
}
