package com.hmju.httptracking

import io.reactivex.rxjava3.core.Single
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Description :
 *
 * Created by juhongmin on 2022/04/30
 */
interface TestApiService {
    @GET("/api/til/goods")
    fun fetchGoods(
        @Query("pageNo") pageNo: Int,
        @Query("pageSize") pageSize: Int
    ): Single<String>

    @GET("/api/til/test")
    fun fetchTest(): Single<String>

    @GET("/api/til/jsend/list/meta")
    fun fetchJsendList(): Single<String>

    @POST("/api/til/goods/like")
    fun addLike(
        @Body body : String
    ) : Single<String>
}