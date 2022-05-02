package com.hmju.httptracking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.http.tracking.TrackingHttpInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val apiService: TestApiService by lazy {
        createApiService(createOkHttpClient())
    }

    private val trackingHttpInterceptor: TrackingHttpInterceptor by lazy { TrackingHttpInterceptor() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Flowable.interval(1000, TimeUnit.MILLISECONDS)
            .onBackpressureBuffer()
            .subscribe({
                Timber.d("TICK $it")
                randomApi()
            }, {
            })
    }

    private fun randomApi() {
        val ran = Random.nextInt(0, 20)
        val api = if (ran < 3) {
            apiService.fetchTest()
        } else if (ran < 5) {
            apiService.fetchGoods(
                Random.nextInt(
                    1,
                    11
                ), 25
            )
        } else if (ran < 10) {
            apiService.addLike("efefefefef")
        } else {
            apiService.fetchJsendList()
        }
        api.subscribe({
            Timber.d("SUCC $it")
        }, {
            Timber.d("ERROR $it")
        })
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(trackingHttpInterceptor)
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createApiService(client: OkHttpClient): TestApiService {
        val json = Json {
            isLenient = true // Json 큰따옴표 느슨하게 체크.
            ignoreUnknownKeys = true // Field 값이 없는 경우 무시
            coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
        }
        return Retrofit.Builder().apply {
            baseUrl("https://til.qtzz.synology.me")
            client(client)
            addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        }.build().create(TestApiService::class.java)
    }
}