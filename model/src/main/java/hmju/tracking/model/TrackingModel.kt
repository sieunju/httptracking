package hmju.tracking.model

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
open class TrackingModel {
    private val _reqModels: MutableList<ChildModel> by lazy { mutableListOf() }
    private val _resModels: MutableList<ChildModel> by lazy { mutableListOf() }
    private var _summary: SummaryModel? = null

    open var uid: Long = -1
    open fun getReqModels(): List<ChildModel> {
        return _reqModels
    }

    open fun getResModels(): List<ChildModel> {
        return _resModels
    }

    open fun getSummaryModel(): SummaryModel {
        return _summary!!
    }

    constructor(
        req: Request,
        res: Response
    ) {
        try {
            _reqModels.addAll(getHttpRequestModels(req))
        } catch (ex: Exception) {
            // ignore
        }
        try {
            _resModels.addAll(getHttpResponseModels(res))
        } catch (ex: Exception) {
            // ignore
        }
        _summary = SummaryModel(req, res)
    }

    constructor(
        req: Request,
        sendTimeMs: Long,
        err: Exception
    ) {
        try {
            _reqModels.addAll(getHttpRequestModels(req))
        } catch (ex: Exception) {
            // ignore
        }
        try {
            _resModels.add(ContentsModel(text = err.message.toString()))
        } catch (ex: Exception) {
            // ignore
        }
        _summary = SummaryModel(req, sendTimeMs, err)
    }

    constructor()

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
            headerMap.map {
                ContentsModel(
                    hexCode = "#222222",
                    text = it.key + " : " + it.value
                )
            }.run { list.addAll(this) }
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
            headerMap.map {
                ContentsModel(
                    hexCode = "#222222",
                    text = it.key + " : " + it.value
                )
            }.run { list.addAll(this) }
        }
        // Body
        val body = res.body
        if (body != null) {
            list.add(HttpBodyModel(res.headers, body))
        }
        return list
    }

    fun setReqModels(list: List<ChildModel>) {
        _reqModels.clear()
        _reqModels.addAll(list)
    }

    fun setResModels(list: List<ChildModel>) {
        _resModels.clear()
        _resModels.addAll(list)
    }

    fun setSummary(summary: SummaryModel) {
        this._summary = summary
    }
}
