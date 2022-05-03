package com.http.tracking

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.http.tracking.models.*
import com.http.tracking.ui.TrackingBottomSheetDialog
import com.http.tracking.ui.viewholder.*
import java.net.URLDecoder
import java.text.SimpleDateFormat

internal object Extensions {

    @SuppressLint("SimpleDateFormat")
    private val simpleDate = SimpleDateFormat("HH:mm:ss")

    fun Long.toDate(): String {
        return simpleDate.format(this)
    }

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

        /**
         * newItem 에 데이터 클래스를 oldItem 의 데이터 형을 비교하여 둘다 같은 데이터 형인경우
         * 같은 형태로 형변환 하여 비교 처리하는 함수
         * @return true 같은 데이터 형이고 같은 데이터, false 다른 데이터
         */
        private inline fun <reified R> compareInstance(
            old: R,
            new: T,
            function: (R, R) -> Boolean
        ): Boolean {
            return if (new is R) {
                function(old, new)
            } else {
                false
            }
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            return when (val oldItem = oldList[oldPosition]) {
                is TrackingHeaderUiModel ->
                    compareInstance<TrackingHeaderUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.key == new.key }
                is TrackingPathUiModel ->
                    compareInstance<TrackingPathUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.path == new.path }
                is TrackingQueryUiModel ->
                    compareInstance<TrackingQueryUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.key == new.key }
                is TrackingBodyUiModel ->
                    compareInstance<TrackingBodyUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.body == new.body }
                is TrackingTitleUiModel ->
                    compareInstance<TrackingTitleUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.title == new.title }
                is TrackingListUiModel ->
                    compareInstance<TrackingListUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.item.uid == new.item.uid }
                else -> false
            }
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            return when (val oldItem = oldList[oldPosition]) {
                is TrackingHeaderUiModel ->
                    compareInstance<TrackingHeaderUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.key == new.key && old.value == new.value }
                is TrackingPathUiModel ->
                    compareInstance<TrackingPathUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.path == new.path }
                is TrackingQueryUiModel ->
                    compareInstance<TrackingQueryUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.key == new.key && old.value == new.value }
                is TrackingBodyUiModel ->
                    compareInstance<TrackingBodyUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.body == new.body }
                is TrackingTitleUiModel ->
                    compareInstance<TrackingTitleUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.title == new.title }
                is TrackingListUiModel ->
                    compareInstance<TrackingListUiModel>(
                        oldItem,
                        newList[newPosition]
                    ) { old, new -> old.item == new.item }
                else -> false
            }
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
