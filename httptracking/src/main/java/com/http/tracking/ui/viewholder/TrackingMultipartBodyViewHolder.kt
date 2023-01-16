package com.http.tracking.ui.viewholder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.http.tracking.R
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingMultipartBodyUiModel

internal class TrackingMultipartBodyViewHolder(
    parent: ViewGroup
) : BaseTrackingViewHolder(
    parent, R.layout.vh_tracking_multipart_body
) {

    private val ivThumb: AppCompatImageView by lazy { itemView.findViewById(R.id.ivThumb) }

    override fun onBindView(model: BaseTrackingUiModel) {
        if (model is TrackingMultipartBodyUiModel) {
            performUiModel(model)
        }
    }

    private fun performUiModel(model: TrackingMultipartBodyUiModel) {
        // Image Type 만 지원
        if (model.mediaType?.type == "image") {
            if (model.bitmap == null) {
                model.bitmap = strToBitmap(model.binary)
            }

            ivThumb.changeVisible(true)
            ivThumb.setImageBitmap(model.bitmap)

        } else {
            ivThumb.changeVisible(false)
        }
    }

    private fun strToBitmap(str: String?): Bitmap? {
        if (str == null) return null
        return try {
            val decode = Base64.decode(str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decode, 0, decode.size)
        } catch (ex: Exception) {
            null
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
}
