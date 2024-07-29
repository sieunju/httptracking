package hmju.http.tracking_interceptor.model.v2

import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.Response
import java.net.URLDecoder

/**
 * Description : BaseTrackingModel
 *
 * Created by juhongmin on 2024. 7. 29.
 */
@Suppress("MemberVisibilityCanBePrivate")
class TrackingModelV2 {
    var uid: Long = -1
    val reqModels: List<ChildModel>
    val resModels: List<ChildModel>
    val summaryModel: SummaryModel

    constructor(
        req: Request,
        res: Response
    ) {
        reqModels = try {
            getHttpRequestModels(req)
        } catch (ex: Exception) {
            listOf()
        }
        resModels = try {
            getHttpResponseModels(res)
        } catch (ex: Exception) {
            listOf()
        }
        summaryModel = SummaryModel(req, res)
    }

    constructor(
        req: Request,
        sendTimeMs: Long,
        err: Exception
    ) {
        reqModels = try {
            getHttpRequestModels(req)
        } catch (ex: Exception) {
            listOf()
        }
        resModels = mutableListOf(ContentsModel(text = err.message.toString()))
        summaryModel = SummaryModel(req, sendTimeMs, err)
    }

    /**
     * Getter HTTP Request UiModels
     * @param req HTTP Request
     */
    private fun getHttpRequestModels(
        req: Request
    ): List<ChildModel> {
        val list = mutableListOf<ChildModel>()
        // full url
        list.add(ContentsModel(text = req.url.toString()))
        // path
        list.add(TitleModel(hexCode = "#C62828", text = "[path]"))
        list.add(ContentsModel(text = req.url.encodedPath))
        val headerMap = req.headers.toMap()
        // headers
        if (headerMap.isNotEmpty()) {
            list.add(TitleModel(hexCode = "#C62828", text = "[header]"))
            list.addAll(headerMap.map { ContentsModel(text = it.key + " : " + it.value) })
        }
        // query
        val queryParams = req.url.query
        if (!queryParams.isNullOrEmpty()) {
            list.add(TitleModel(hexCode = "#C62828", text = "[query]"))
            queryParams.split("&").forEach {
                val query = splitQuery(it) ?: return@forEach
                val text = "${query.first} : ${query.second}"
                ContentsModel(
                    hexCode = "#222222",
                    text = text
                ).run { list.add(this) }
            }
        }

        // Body
        val body = req.body
        if (body is MultipartBody) {
            body.parts
                .map { HttpMultipartModel(it) }
                .run { list.addAll(this) }
        } else if (body != null) {
            list.add(HttpBodyModel(body))
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

    /**
     * Getter HTTP Response UiModels
     * @param res HTTP Response
     */
    private fun getHttpResponseModels(
        res: Response
    ): List<ChildModel> {
        val list = mutableListOf<ChildModel>()
        val headerMap = res.headers.toMap()
        // headers
        if (headerMap.isNotEmpty()) {
            list.add(TitleModel(hexCode = "#C62828", text = "[header]"))
            list.addAll(headerMap.map { ContentsModel(text = it.key + " : " + it.value) })
        }
        // Body
        val body = res.body
        if (body != null) {
            list.add(HttpBodyModel(res.headers, body))
        }
        return list
    }
}
