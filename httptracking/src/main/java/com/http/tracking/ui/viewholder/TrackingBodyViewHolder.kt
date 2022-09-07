package com.http.tracking.ui.viewholder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.content.MimeTypeFilter
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhTrackingBodyBinding
import com.http.tracking.models.TrackingBodyUiModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.MultipartReader
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.BufferedSource
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset

internal class TrackingBodyViewHolder(parent: ViewGroup) :
    BaseTrackingViewHolder<VhTrackingBodyBinding>(
        parent,
        R.layout.vh_tracking_body
    ) {
    init {
        itemView.setOnLongClickListener {
            binding.model?.runCatching {
                simpleLongClickCopy(body)
            }
            return@setOnLongClickListener false
        }
    }

    override fun onBindView(model: Any) {
        binding.setVariable(BR.model, model)
    }

    private fun strToBitmap(str: String?): Bitmap? {
        if (str == null) return null
        return try {
            val byte = Base64.decode(str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(byte, 0, byte.size)
        } catch (ex: Exception) {
            null
        }
    }
}
