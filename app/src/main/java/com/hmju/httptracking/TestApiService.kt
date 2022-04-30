package com.hmju.httptracking

import io.reactivex.rxjava3.core.Single
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Description :
 *
 * Created by juhongmin on 2022/04/30
 */
interface TestApiService {
    @GET("/api/goods")
    fun fetchGoods(
        @Query("pageNo") pageNo: Int,
        @Query("pageSize") pageSize: Int
    ): Single<String>

    @GET("/api/test")
    fun fetchTest(): Single<String>
}