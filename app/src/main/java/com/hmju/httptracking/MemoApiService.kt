package com.hmju.httptracking

import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.QueryMap

/**
 * Description :
 *
 * Created by juhongmin on 2022/12/04
 */
interface MemoApiService {
    @Headers("X-FOO: DDDD", "DDD: dfff")
    @GET("api/android")
    fun fetchAndroid(@QueryMap queryMap: Map<String, String>): Single<String>
}