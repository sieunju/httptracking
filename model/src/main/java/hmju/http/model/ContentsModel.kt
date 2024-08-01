package hmju.http.model

/**
 * Description :
 *
 * Created by juhongmin on 2024. 7. 29.
 */
data class ContentsModel(
    val hexCode: String = "#B6B6B6", // ex.) #222222
    val text: CharSequence
) : ChildModel
