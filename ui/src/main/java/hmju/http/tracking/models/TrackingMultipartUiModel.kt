package hmju.http.tracking.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import hmju.http.tracking.R
import hmju.http.tracking_interceptor.model.HttpMultipartModel

internal data class TrackingMultipartUiModel(
    val item: HttpMultipartModel,
) : BaseTrackingUiModel(R.layout.vh_tracking_multipart) {

    val bitmap: Bitmap? by lazy {
        val str: String = Base64.encodeToString(
            item.bytes,
            Base64.DEFAULT
        ) ?: return@lazy null
        try {
            val decode = Base64.decode(str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decode, 0, decode.size)
        } catch (ex: Exception) {
            null
        }
    }

    override fun getClassName() = "TrackingMultipartUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return false
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return false
    }
}
