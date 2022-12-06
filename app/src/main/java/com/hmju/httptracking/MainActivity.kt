package com.hmju.httptracking

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.http.tracking_interceptor.TrackingHttpInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.random.Random

internal class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    private val apiService: TestApiService by lazy {
        createApiService(BuildConfig.TIL_URL, createOkHttpClient())
    }

    private val uploadApiService: UploadApiService by lazy {
        createApiService(BuildConfig.CDN_URL, createOkHttpClient())
    }

    private val memoApiService: MemoApiService by lazy {
        createApiService(BuildConfig.MEMO_URL, createOkHttpClient())
    }

    private val trackingHttpInterceptor: TrackingHttpInterceptor by lazy { TrackingHttpInterceptor() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            moveGallery()
        }

        lifecycleScope.async(Dispatchers.IO) {
            repeat(30) {
                randomApi()
                delay(500)
            }
        }
    }

    private fun moveGallery() {
        val galleryUri = Uri.parse("content://media/external/images/media")
        val intent = Intent(Intent.ACTION_VIEW, galleryUri).apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        startActivityForResult(intent, 3000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3000) {
            performUpload(data?.dataString)
        }
    }

    private fun performUpload(contentsUri: String?) {
        if (contentsUri == null) return
        lifecycleScope.launch(Dispatchers.Default) {
            val bitmap = uriToBitmap(contentsUri)
            if (bitmap != null) {
                val list = mutableListOf<ByteArray>()
                list.add(bitmap)
                list.add(bitmap)
                list.add(bitmap)
                uploadApiService.uploads(bitmapToMultiPart(*list.toTypedArray())).subscribe(
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
            val body = buffer.toRequestBody(
                contentType = "image/jpg".toMediaType()
            )
            fileList.add(
                MultipartBody.Part.createFormData(
                    name = "files",
                    filename = "${System.currentTimeMillis()}.jpg",
                    body = body
                )
            )
        }
        return fileList
    }

    private fun uriToBitmap(path: String?): ByteArray? {
        if (path == null) return null
        val uri = Uri.parse(path)
        var bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        }

        // 이미지 회전 이슈 처리.
        val matrix = Matrix()
        contentResolver.openInputStream(uri)?.let {
            val exif = ExifInterface(it)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            setRotate(
                orientation = orientation,
                matrix = matrix
            )

            it.close()
        }

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // 이미지 리사이징 처리 필요에 따라 사용할지 말지 정의.
        if (1080 < bitmap.width) {
            // 비율에 맞게 높이값 계산
            val height = 1080 * bitmap.height / bitmap.width
            bitmap = Bitmap.createScaledBitmap(bitmap, 1080, height, true)
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * set Image Rotate Func.
     * @param orientation ExifInterface Orientation
     * @param matrix Image Matrix
     *
     */
    private fun setRotate(orientation: Int, matrix: Matrix): Boolean {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.postRotate(0F)
                true
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.postRotate(180f)
                true
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                matrix.postRotate(270f)
                true
            }
            else -> false
        }
    }

    private fun randomApi() {
        val ran = Random.nextInt(0, 20)
        val api = if (ran < 3) {
            val queryMap = mapOf<String, String>(
                "pageNo" to "1",
                "pageSize" to "${Random.nextInt()}",
                "hi" to "helllloqweqweqweqweqweqwe"
            )
            memoApiService.fetchAndroid(queryMap)
        } else if (ran < 5) {
            apiService.fetchGoods(
                Random.nextInt(
                    1,
                    11
                ), 25
            )
        } else if (ran < 10) {
            val json = JSONObject()
            json.put("id","efefefefef")
            apiService.addLike(json.toString())
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

    /**
     * ApiService 에 따라서 생성하는 함수
     */
    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> createApiService(url: String, client: OkHttpClient): T {
        val json = Json {
            isLenient = true // Json 큰따옴표 느슨하게 체크.
            ignoreUnknownKeys = true // Field 값이 없는 경우 무시
            coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
        }
        return Retrofit.Builder().apply {
            baseUrl(url)
            client(client)
            addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        }.build().create(T::class.java)
    }
}