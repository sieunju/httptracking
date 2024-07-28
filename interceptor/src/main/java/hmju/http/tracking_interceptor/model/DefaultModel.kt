package hmju.http.tracking_interceptor.model

/**
 * Description :
 *
 * Created by juhongmin on 2024. 7. 28.
 */
data class DefaultModel(
    val isSuccess: Boolean,
    val method: String,
    val code: Int,
    val host: String,
    val path: String,
): TrackingModel(0) {
}