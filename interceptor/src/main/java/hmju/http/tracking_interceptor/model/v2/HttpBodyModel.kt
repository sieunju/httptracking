package hmju.http.tracking_interceptor.model.v2

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.GzipSource
import java.nio.charset.Charset

/**
 * Description : HTTP Request or Response Body Model
 *
 * Created by juhongmin on 2024. 7. 29.
 */
data class HttpBodyModel(
    val json: String
) : ChildModel {
    companion object {
        // Gson
        private val gson: Gson by lazy {
            GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .serializeNulls()
                .create()
        }

        /**
         * Response Body to JSON String
         */
        private fun getResBody(headers: Headers, body: ResponseBody?): String? {
            if (body == null) return null
            return try {
                val contentLength = body.contentLength()
                val source = body.source()
                source.request(Long.MAX_VALUE)
                var buffer = source.buffer
                if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                    GzipSource(buffer.clone()).use { gzippedResponseBody ->
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    }
                }
                if (contentLength != 0L) {
                    buffer.clone().readString(Charset.defaultCharset())
                } else {
                    null
                }
            } catch (ex: Exception) {
                null
            }
        }
    }

    constructor(
        body: RequestBody
    ) : this(
        json = try {
            val buffer = Buffer()
            body.writeTo(buffer)
            val str = buffer.readString(Charsets.UTF_8)
            val js = JsonParser.parseString(str)
            gson.toJson(js)
        } catch (ex: Exception) {
            ""
        }
    )

    constructor(
        headers: Headers,
        body: ResponseBody
    ) : this(
        json = try {
            val str = getResBody(headers, body)
            val js = JsonParser.parseString(str)
            gson.toJson(js)
        } catch (ex: Exception) {
            ""
        }
    )
}
