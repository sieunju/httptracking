package com.http.tracking

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingBodyUiModel
import com.http.tracking.models.TrackingHeaderUiModel
import com.http.tracking.models.TrackingQueryUiModel
import com.http.tracking_interceptor.model.TrackingRequestEntity
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

    fun parseBodyUiModel(req: TrackingRequestEntity): BaseTrackingUiModel {
        return try {
            if (req.mediaType?.type == "multipart") {
                TrackingBodyUiModel(
                    isImageType = true,
                    body = req.body ?: ""
                )
            } else {
                val je = JsonParser.parseString(req.body)
                TrackingBodyUiModel(
                    isImageType = false,
                    body = gson.toJson(je)
                )
            }
        } catch (ex: Exception) {
            TrackingBodyUiModel(
                body = req.body ?: ""
            )
        }
    }

    fun parseBodyUiModel(body: String): BaseTrackingUiModel {
        return try {
            val je = JsonParser.parseString(body)
            TrackingBodyUiModel(body = gson.toJson(je))
        } catch (ex: Exception) {
            TrackingBodyUiModel(body = body)
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
}
