package hmju.http.tracking_interceptor.model

/**
 * Description : BaseTrackingModel
 *
 * Created by juhongmin on 2024. 7. 29.
 */
sealed class TrackingModelV2(
    open var uid: Long = -1
)
