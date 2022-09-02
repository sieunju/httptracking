package com.http.tracking

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingBodyUiModel
import com.http.tracking.models.TrackingHeaderUiModel
import com.http.tracking.models.TrackingQueryUiModel
import com.http.tracking.ui.TrackingBottomSheetDialog
import com.http.tracking.ui.viewholder.*
import java.net.URLDecoder

internal object Extensions {

    // Gson
    private val gson: Gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .serializeNulls()
            .create()
    }

    class TrackingDetailDiffUtil<out T : BaseTrackingUiModel>(
        private val oldList: List<T>,
        private val newList: List<T>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            val oldItem = oldList[oldPosition]
            val newItem = newList[newPosition]
            return oldItem.areItemsTheSame(newItem)
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            val oldItem = oldList[oldPosition]
            val newItem = newList[newPosition]
            return oldItem.areContentsTheSame(newItem)
        }
    }

    /**
     * Header UiModel 변환 처리함수
     */
    fun parseHeaderUiModel(map: Map<String, String>): List<BaseTrackingUiModel> {
        val uiList = mutableListOf<BaseTrackingUiModel>()
        map.forEach { entry ->
            uiList.add(
                TrackingHeaderUiModel(
                    key = entry.key,
                    value = entry.value
                )
            )
        }
        return uiList
    }

    /**
     * Request Query 값 UiModel 변환 처리 함수
     */
    fun parseQueryUiModel(fullUrl: String?): List<BaseTrackingUiModel> {
        if (fullUrl == null) return emptyList()
        val uiList = mutableListOf<BaseTrackingUiModel>()
        val startIdx = fullUrl.indexOf("?")
        if (startIdx != -1) {
            val pathOrQuery = fullUrl.substring(startIdx.plus(1))
            pathOrQuery.split("&").forEach { str ->
                str.runCatching {
                    val pair = splitQuery(this)
                    if (pair != null) {
                        uiList.add(TrackingQueryUiModel(key = pair.first, value = pair.second))
                    }
                }
            }
        }
        return uiList
    }

    fun parseBodyUiModel(body: String): BaseTrackingUiModel {
        return try {
            val je = JsonParser.parseString(body)
            TrackingBodyUiModel(gson.toJson(je))
        } catch (ex: Exception) {
            TrackingBodyUiModel(body)
        }
    }

    /**
     * Split HTTP Query
     * Key=Value
     * @param txt Full Url
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

    internal class TrackingAdapter : RecyclerView.Adapter<BaseTrackingViewHolder<*>>() {

        private val dataList = mutableListOf<BaseTrackingUiModel>()

        private var dialog: TrackingBottomSheetDialog? = null

        fun submitList(newList: List<BaseTrackingUiModel>?) {
            if (newList == null) return
            val diffResult = DiffUtil.calculateDiff(TrackingDetailDiffUtil(dataList, newList))
            dataList.clear()
            dataList.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }

        fun setBottomSheetDialog(dialog: TrackingBottomSheetDialog) {
            this.dialog = dialog
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseTrackingViewHolder<*> {
            return when (viewType) {
                R.layout.vh_tracking_header -> TrackingHeaderViewHolder(parent)
                R.layout.vh_tracking_path -> TrackingPathViewHolder(parent)
                R.layout.vh_tracking_query -> TrackingQueryViewHolder(parent)
                R.layout.vh_tracking_body -> TrackingBodyViewHolder(parent)
                R.layout.vh_tracking_title -> TrackingTitleViewHolder(parent)
                R.layout.vh_child_tracking -> TrackingListViewHolder(parent, dialog)
                else -> throw IllegalArgumentException("Invalid ViewType")
            }
        }

        override fun onBindViewHolder(holder: BaseTrackingViewHolder<*>, pos: Int) {
            runCatching {
                holder.onBindView(dataList[pos])
            }
        }

        override fun getItemViewType(pos: Int): Int {
            return if (dataList.size > pos) {
                dataList[pos].layoutId
            } else {
                return super.getItemViewType(pos)
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }
    }
}
