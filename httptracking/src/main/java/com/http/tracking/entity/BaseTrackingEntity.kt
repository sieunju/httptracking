package com.http.tracking.entity

import com.http.tracking.Extensions.toDate

/**
 * Description : Base Tracking Entity
 *
 * Created by juhongmin on 2022/03/30
 */
internal open class BaseTrackingEntity {
    var responseTimeMs: Long = 0L
    var takenTimeMs: Long = 0L // 걸린 시간
    var code: Int = 0
    var baseUrl: String = "" // domain url
    var method: String = "" // POST, GET, PUT
    var error: Exception? = null

    fun isSuccess(): Boolean {
        return code in 200..299
    }

    var resTimeDate: String? = null
        get() {
            if (field == null) {
                field = responseTimeMs.toDate()
            }
            return field
        }
    var codeTxt: String? = null
        get() {
            if (field == null) {
                field = "$code "
            }
            return field
        }
    var takenTimeTxt: String? = null
        get() {
            if (field == null) {
                field = "${takenTimeMs}MS"
            }
            return field
        }

    override fun toString(): String {
        return "Domain=${baseUrl} Method=$method\nResponseCode=${code}\nTime=${takenTimeMs}MS\nError=$error"
    }
}
