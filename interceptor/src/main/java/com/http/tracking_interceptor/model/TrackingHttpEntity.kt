package com.http.tracking_interceptor.model

/**
 * Description : HTTP 로그 추적 하는 데이터 모델 클래스
 *
 * Created by juhongmin on 2022/03/29
 */
data class TrackingHttpEntity(
    val headerMap: Map<String, String>,
    val path: String = "", // Path
    val req: BaseTrackingRequestEntity? = null,
    var res: TrackingResponseEntity? = null
) : BaseTrackingEntity() {
    override fun toString(): String {
        return "${super.toString()}\nHeaders=$headerMap\nPath=$path\nReq=$req\nRes=${res?.body}"
    }
}
