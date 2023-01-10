package com.http.tracking.util

import android.text.InputFilter
import android.text.Spanned
import timber.log.Timber


/**
 * Description : EditText Min Value ~ Max Value
 *
 * Created by juhongmin on 2023/01/10
 */
class InputFilterMinMax(
    private val min: Int,
    private val max: Int
) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toInt()
            if (isInRange(min, max, input)) return null
        } catch (nfe: NumberFormatException) {
        }
        return "0"
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        Timber.d("isInRange Input $c")
        if (c.toString().length > 5) {
            return true
        }
        return if (b > a) c in a..b else c in b..a
    }
}
