package hmju.tracking.model

import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.Response
import okio.Buffer

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

    constructor(
        reqList: List<ChildModel>,
        resList: List<ChildModel>,
        summary: SummaryModel
    ) {
        _reqModels.addAll(reqList)
        _resModels.addAll(resList)
        _summary = summary
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
        val fullUrl = req.url.toString()
        list.add(ContentsModel(text = fullUrl))
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
        val url = req.url
        if (url.querySize > 0) {
            list.add(TitleModel(hexCode = "#C62828", text = "[query]"))
            for (idx in 0 until url.querySize) {
                ContentsModel(
                    hexCode = "#222222",
                    text = "${url.queryParameterName(idx)} : ${url.queryParameterValue(idx).orEmpty()}"
                ).run { list.add(this) }
            }
        }

        // Body (HTTP Method 와 상관없이 Body 타입 기준으로 처리)
        val body = req.body ?: return list
        if (body.isOneShot() || body.isDuplex()) {
            list.add(TitleModel(hexCode = "#C62828", text = "[body]"))
            list.add(ContentsModel(text = "${body.contentType()} (one-shot or duplex body)"))
            return list
        }
        when (body) {
            is FormBody -> {
                // @Field, @FieldMap
                list.add(TitleModel(hexCode = "#C62828", text = "[field]"))
                for (idx in 0 until body.size) {
                    ContentsModel(
                        hexCode = "#222222",
                        text = "${body.name(idx)} : ${body.value(idx)}"
                    ).run { list.add(this) }
                }
            }

            is MultipartBody -> {
                // @Part, @PartMap
                list.add(TitleModel(hexCode = "#C62828", text = "[multipart]"))
                body.parts.forEach { list.add(getMultipartModel(it)) }
            }

            else -> {
                // @Body
                list.add(TitleModel(hexCode = "#C62828", text = "[body]"))
                list.add(HttpBodyModel(body))
            }
        }

        return list
    }

    /**
     * Multipart Part 에서 이미지는 HttpMultipartModel, 그외는 Text 로 처리
     */
    private fun getMultipartModel(part: MultipartBody.Part): ChildModel {
        val disposition = part.headers?.get("Content-Disposition").orEmpty()
        val name = Regex("name=\"([^\"]*)\"").find(disposition)?.groupValues?.get(1) ?: ""
        val fileName = Regex("filename=\"([^\"]*)\"").find(disposition)?.groupValues?.get(1)
        val contentType = part.body.contentType()
        if (contentType?.type == "image") {
            return HttpMultipartModel(part)
        }
        val text = if (fileName != null) {
            "$fileName ($contentType, ${part.body.contentLength()} bytes)"
        } else {
            try {
                val buffer = Buffer()
                part.body.writeTo(buffer)
                buffer.readString(contentType?.charset() ?: Charsets.UTF_8)
            } catch (ex: Exception) {
                ""
            }
        }
        return ContentsModel(hexCode = "#222222", text = "$name : $text")
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
        // status
        list.add(TitleModel(hexCode = "#C62828", text = "[status]"))
        list.add(ContentsModel(text = "${res.protocol} ${res.code} ${res.message}"))
        // path
        list.add(TitleModel(hexCode = "#C62828", text = "[path]"))
        list.add(ContentsModel(text = res.request.url.encodedPath))
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
            list.add(TitleModel(hexCode = "#C62828", text = "[body]"))
            list.add(HttpBodyModel(res.headers, body))
        }
        return list
    }

    fun setReqModels(list: List<ChildModel>): TrackingModel {
        _reqModels.clear()
        _reqModels.addAll(list)
        return this
    }

    fun setResModels(list: List<ChildModel>): TrackingModel {
        _resModels.clear()
        _resModels.addAll(list)
        return this
    }

    fun setSummary(summary: SummaryModel): TrackingModel {
        this._summary = summary
        return this
    }
}
