package hmju.http.tracking_interceptor

import hmju.http.tracking_interceptor.model.v2.TrackingModelV2
import okhttp3.*
import java.io.IOException

/**
 * Description : Http 정보 추적하는 Interceptor
 *
 * Created by juhongmin on 2022/03/29
 */
class TrackingHttpInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 릴리즈 Skip
        if (TrackingDataManager.getInstance().isRelease()) {
            return chain.proceed(request)
        }

        val sendTimeMs = System.currentTimeMillis()
        val response = try {
            chain.proceed(request)
        } catch (ex: Exception) {
            TrackingDataManager
                .getInstance()
                .addV2(TrackingModelV2(request, sendTimeMs, ex))
            throw ex
        }
        TrackingDataManager
            .getInstance()
            .addV2(TrackingModelV2(request, response))
        return response
    }
}
