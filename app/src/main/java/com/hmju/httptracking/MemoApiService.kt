package com.hmju.httptracking

import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * Description :
 *
 * Created by juhongmin on 2022/12/04
 */
interface MemoApiService {
    @GET("api/android")
    fun fetchAndroid(@QueryMap queryMap: Map<String, String>): Single<String>
}