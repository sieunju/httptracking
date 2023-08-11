package hmju.http.tracking.models

import android.graphics.Bitmap
import android.util.Base64
import com.http.tracking.R
import hmju.http.tracking_interceptor.model.TrackingRequest
import okhttp3.MediaType

internal data class TrackingMultipartBodyUiModel(
    val mediaType: MediaType? = null,
    val binary: String = ""
) : BaseTrackingUiModel(R.layout.vh_tracking_multipart_body) {

    var bitmap: Bitmap? = null

    override fun getClassName() = "TrackingMultipartBodyUiModel"

    override fun areItemsTheSame(diffItem: Any): Boolean {
        return false
    }

    override fun areContentsTheSame(diffItem: Any): Boolean {
        return false
    }

    constructor(
        part: TrackingRequest.MultiPart.Part
    ) : this(
        mediaType = part.type,
        binary = Base64.encodeToString(part.bytes, Base64.DEFAULT) ?: ""
    )
}
