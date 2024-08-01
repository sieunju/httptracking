package hmju.http.model

import hmju.http.model.Extensions.toDate
import okhttp3.Request
import okhttp3.Response
import java.net.SocketTimeoutException

/**
 * Description : List 에 표시하는 요약 데이터 모델
 *
 * Created by juhongmin on 2024. 7. 29.
 */
data class SummaryModel(
    val colorHexCode: String = "#222222",
    val titleList: List<String> = listOf(),
    val contentsList: List<String> = listOf(),
    val wifiSummary: String? = null
) {
    constructor(
        req: Request,
        res: Response
    ) : this(
        colorHexCode = if (res.isSuccessful) "#03A9F4" else "#C62828",
        titleList = listOf(
            req.method,
            res.code.toString(),
            "${res.receivedResponseAtMillis - res.sentRequestAtMillis}MS"
        ),
        contentsList = listOf(
            req.url.host,
            req.url.encodedPath,
            "${res.sentRequestAtMillis.toDate()} ~ ${res.receivedResponseAtMillis.toDate()}",
        ),
        wifiSummary = StringBuilder()
            .append("[${req.method}/${res.code}]")
            .append(" ${req.url.host}${req.url.encodedPath}")
            .toString()
    )

    constructor(
        req: Request,
        sendTimeMs: Long,
        err: Exception
    ) : this(
        colorHexCode = "#FF5722",
        titleList = listOf(
            if (err is SocketTimeoutException) "TIME_OUT" else "ERROR",
            req.method
        ),
        contentsList = listOf(
            req.url.host,
            req.url.encodedPath,
            sendTimeMs.toDate()
        ),
        wifiSummary = StringBuilder()
            .append("[${req.method}]")
            .append("[")
            .append(
                if (err is SocketTimeoutException) {
                    "TIME_OUT"
                } else {
                    "ERROR"
                }
            )
            .append("]")
            .append("${req.url.host}${req.url.encodedPath}")
            .toString()
    )
}
