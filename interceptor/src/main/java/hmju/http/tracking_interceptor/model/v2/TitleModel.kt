package hmju.http.tracking_interceptor.model.v2

/**
 * Description :
 *
 * Created by juhongmin on 2024. 7. 29.
 */
data class TitleModel(
    val hexCode: String = "#222222", // ex.) #222222
    val text: CharSequence
) : ChildModel
