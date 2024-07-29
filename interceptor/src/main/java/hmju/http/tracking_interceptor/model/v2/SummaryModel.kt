package hmju.http.tracking_interceptor.model.v2

import hmju.http.tracking_interceptor.Extensions.toDate
import okhttp3.Request
import okhttp3.Response
import java.net.SocketTimeoutException

/**
 * Description : List 에 표시하는 요약 데이터 모델
 *
 * Created by juhongmin on 2024. 7. 29.
 */
data class SummaryModel(
    val colorHexCode: String = "#222",
    val titleList: List<String> = listOf(),
    val contentsList: List<String> = listOf()
) {
    // 03A9F4 파랑, C62828 빨강, FF5722 주항
    constructor(
        req: Request,
        res: Response
    ) : this(
        colorHexCode = if (res.isSuccessful) "#03A9F4" else "#C62828",
        titleList = listOf(
            req.method,
            res.code.toString(),
            "${res.receivedResponseAtMillis - res.sentRequestAtMillis}zMS"
        ),
        contentsList = listOf(
            req.url.host,
            req.url.encodedPath,
            "${res.sentRequestAtMillis.toDate()} ~ ${res.receivedResponseAtMillis.toDate()}",
        )
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
        )
    )
}
