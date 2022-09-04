package com.hmju.httptracking

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.http.tracking_interceptor.TrackingHttpInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import timber.log.Timber
import java.io.File
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val compositeDisposable = CompositeDisposable()

    private val apiService: TestApiService by lazy {
        createApiService(createOkHttpClient())
    }

    private val uploadApiService: UploadApiService by lazy {
        createUploadApiService(createOkHttpClient())
    }

    private val trackingHttpInterceptor: TrackingHttpInterceptor by lazy { TrackingHttpInterceptor() }

    private val galleryCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Timber.d("GalleryCallback ${it.data?.dataString}")
            performUpload(it.data?.dataString)
        }

    private val MAX_IMAGE_WIDTH = 1080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            moveGallery()
        }

        lifecycleScope.async(Dispatchers.IO) {
            repeat(1000) {
                randomApi()
                delay(5000)
            }
        }
    }

    private fun moveGallery() {
        val galleryUri = Uri.parse("content://media/external/images/media")
        val intent = Intent(Intent.ACTION_VIEW, galleryUri).apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        galleryCallback.launch(intent)
    }

    private fun performUpload(contentsUri: String?) {
        if (contentsUri == null) return
        lifecycleScope.launch(Dispatchers.Default) {
            val buffer = uriToBytes(contentsUri)
            if (buffer != null) {
                uploadApiService.uploads(bitmapToMultiPart(buffer)).subscribe(
                    {
                        Timber.d("SUCC $it")
                    }, {
                        Timber.d("ERROR $it")
                    }).addTo(compositeDisposable)
            }
        }
    }

    private fun bitmapToMultiPart(vararg reqBuffers: ByteArray): List<MultipartBody.Part> {
        val fileList = mutableListOf<MultipartBody.Part>()
        reqBuffers.forEach { buffer ->
            val body = buffer.toRequestBody("image/*".toMediaType())
            fileList.add(
                MultipartBody.Part.createFormData(
                    name = "files",
                    filename = null,
                    body = body
                )
            )
        }
        return fileList
    }

    private fun uriToBytes(uri: String): ByteArray? =
        contentResolver.openInputStream(uri.toUri())?.buffered()?.use { it.readBytes() }

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

    @OptIn(ExperimentalSerializationApi::class)
    private fun createUploadApiService(client: OkHttpClient): UploadApiService {
        val json = Json {
            isLenient = true // Json 큰따옴표 느슨하게 체크.
            ignoreUnknownKeys = true // Field 값이 없는 경우 무시
            coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
        }
        return Retrofit.Builder().apply {
            baseUrl("https://cdn.qtzz.synology.me")
            client(client)
            addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        }.build().create(UploadApiService::class.java)
    }
}