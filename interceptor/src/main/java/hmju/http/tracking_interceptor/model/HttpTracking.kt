package hmju.http.tracking_interceptor.model

/**
 * Description :
 *
 * Created by juhongmin on 2023/08/09
 */
sealed class HttpTracking {
    data class Request(
        val headerMap: Map<String, String> = mapOf(),
        val queryParams: String? = null,
        val body: String? = null
    ) : HttpTracking()

    data class RequestMultiPart(
        val headerMap: Map<String, String> = mapOf(),
        val queryParams: String? = null,
        val binaryList: List<Part>
    ) : HttpTracking()

    data class Response(
        val headerMap: Map<String, String> = mapOf(),
        val body: String? = null
    ) : HttpTracking()
}
