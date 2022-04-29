package com.http.tracking.entity

/**
 * Description : HTTP 로그 추적 하는 데이터 모델 클래스
 *
 * Created by juhongmin on 2022/03/29
 */
internal data class TrackingHttpEntity(
    var uid: Long = 0,
    val headerMap: Map<String, String>,
    val path: String, // Path
    val req: TrackingRequestEntity,
    var res: TrackingResponseEntity? = null
) : BaseTrackingEntity() {
    override fun toString(): String {
        return "${super.toString()}\nHeaders=$headerMap\nPath=$path\nReq=$req\nRes=${res?.body}"
    }

    var uidTxt: String? = null
        get() {
            if (field == null) {
                field = "_${uid}"
            }
            return field
        }
}
