package com.http.tracking.ui.viewholder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import androidx.core.content.MimeTypeFilter
import com.http.tracking.BR
import com.http.tracking.R
import com.http.tracking.databinding.VhTrackingBodyBinding
import com.http.tracking.models.TrackingBodyUiModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import timber.log.Timber
import java.io.ByteArrayOutputStream
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
        if (model is TrackingBodyUiModel) {
            binding.setVariable(BR.model, model)
            performBody(model)
        }
    }

    private fun performBody(bodyUiModel: TrackingBodyUiModel) {
        binding.runCatching {
            if (bodyUiModel.isImageType) {
                tvBody.changeVisible(false)
                ivBody.changeVisible(true)
                val bitmap = strToBitmap(bodyUiModel.body)
                ivBody.setImageBitmap(bitmap)
            } else {
                tvBody.changeVisible(true)
                ivBody.changeVisible(false)
                tvBody.text = bodyUiModel.body
            }
        }
    }

    private fun View.changeVisible(isVisible: Boolean) {
        if (isVisible) {
            if (visibility == View.GONE) {
                visibility = View.VISIBLE
            }
        } else {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            }
        }
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
