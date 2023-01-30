package com.http.disable_wifi_tracking.models

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.http.disable_wifi_tracking.R

internal class TrackingHeaderUiModel(
    val key: String,
    val value: String
) : BaseTrackingUiModel(R.layout.vh_tracking_header) {

    override fun getClassName() = "TrackingHeaderUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingHeaderUiModel) {
            key == diffItem.key
        } else {
            false
        }
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return if (diffItem is TrackingHeaderUiModel) {
            key == diffItem.key && value == diffItem.value
        } else {
            false
        }
    }

    var contents: Spannable? = null
        get() {
            if (field == null) {
                val str = StringBuilder()
                str.append(key)
                str.append(":")
                str.append(value)
                val ssb = SpannableStringBuilder(str)
                ssb.setSpan(
                    ForegroundColorSpan(Color.parseColor("#03A9F4")),
                    0,
                    key.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                field = ssb
            }
            return field
        }
}
