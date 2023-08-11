package hmju.http.tracking_interceptor.model

import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.GzipSource
import java.nio.charset.Charset

/**
 * Description : HttpTracking Request Data Model
 *
 * Created by juhongmin on 2023/08/11
 */
sealed class HttpTrackingResponse {

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    internal class Default(
        res: Response
    ) : HttpTrackingResponse() {

        val headerMap: Map<String, String>
        val body: String?

        init {
            headerMap = res.headers.toMap()
            body = getResBody(res.headers, res.body)
        }

        override fun equals(other: Any?): Boolean {
            return if (other is Default) {
                headerMap == other.headerMap &&
                        body == other.body
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            var result = headerMap.hashCode()
            result = 31 * result + (body?.hashCode() ?: 0)
            return result
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
}
