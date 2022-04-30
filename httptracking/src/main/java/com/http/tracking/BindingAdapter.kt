package com.http.tracking

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter

internal object BindingAdapter {

    @JvmStatic
    @BindingAdapter("android:text")
    fun setText(
        tv: AppCompatTextView,
        newTxt: String?
    ) {
        if (!newTxt.isNullOrEmpty()) {
            val htmlText = HtmlCompat.fromHtml(newTxt, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tv.text = newTxt
        } else {
            tv.visibility = View.GONE
        }
    }
}
