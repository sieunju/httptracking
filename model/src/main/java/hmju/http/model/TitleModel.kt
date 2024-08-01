package hmju.http.model

/**
 * Description :
 *
 * Created by juhongmin on 2024. 7. 29.
 */
data class TitleModel(
    val hexCode: String = "#222222", // ex.) #222222
    val text: CharSequence
) : ChildModel
