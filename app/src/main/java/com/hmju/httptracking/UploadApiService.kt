package com.hmju.httptracking

import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Description :
 *
 * Created by juhongmin on 2022/09/04
 */
interface UploadApiService {

    @Multipart
    @POST("/api/uploads")
    fun uploads(
        @Part files : List<MultipartBody.Part>
    ) : Single<String>
}
