package com.hmju.httptracking

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import hmju.http.tracking_interceptor.TrackingHttpInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class HttpTestProvider(
    private val activity: AppCompatActivity
) {

    enum class Method {
        GET,
        POST
    }

    private val httpClient: OkHttpClient by lazy { createOkHttpClient() }

    private val contentResolver: ContentResolver by lazy { activity.contentResolver }

    private val trackingHttpInterceptor: TrackingHttpInterceptor by lazy { TrackingHttpInterceptor() }

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(trackingHttpInterceptor)
            .build()
    }

    fun onInit(): HttpTestProvider {
        galleryLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = result.data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                handleUpload(uri.toString())
            }
        }
        return this
    }

    fun startFileUpload() {
        Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            galleryLauncher.launch(this)
        }
    }

    fun startHttpTest() {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            repeat(300) {
                handleRandomApi()
                delay(10)
            }
        }
    }

    private fun requestGet(
        path: String,
        headerMap: Map<String, String> = mapOf(),
        queryMap: Map<String, String> = mapOf()
    ): Request {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(BuildConfig.TEST_HOST)
            .encodedPath(path)
        queryMap.forEach { httpUrl.addQueryParameter(it.key, it.value) }
        return reqApi(httpUrl.build(), headerMap, Method.GET, null)
    }

    private fun requestPost(
        path: String,
        headerMap: Map<String, String> = mapOf(),
        body: RequestBody? = null
    ): Request {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(BuildConfig.TEST_HOST)
            .encodedPath(path)
            .build()
        return reqApi(httpUrl, headerMap, Method.POST, body)
    }

    private fun reqApi(
        httpUrl: HttpUrl,
        headerMap: Map<String, String> = mapOf(),
        method: Method = Method.GET,
        reqBody: RequestBody? = null
    ): Request {
        val headers = Headers.Builder()
        headerMap.forEach { headers.add(it.key, it.value) }
        headers.add("Authorization", BuildConfig.TEST_AUTH)
        return Request.Builder()
            .url(httpUrl)
            .headers(headers.build())
            .method(method.name, reqBody)
            .build()
    }

    private fun handleUpload(contentsUri: String?) {
        if (contentsUri == null) return
        val bitmap = uriToBitmap(contentsUri)
        if (bitmap != null) {
            val list = mutableListOf<ByteArray>()
            list.add(bitmap)
            list.add(bitmap)
            list.add(bitmap)
            flow {
                val api = requestPost(
                    path = "/api/v1/uploads",
                    body = bitmapToMultiPart(list)
                )
                emit(httpClient.newCall(api).execute())
            }.flowOn(Dispatchers.IO).launchIn(activity.lifecycleScope)
        }
    }

    private fun bitmapToMultiPart(bitmapList: List<ByteArray>): RequestBody {
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        bitmapList.forEach {
            val body = it.toRequestBody(
                contentType = "image/jpg".toMediaType()
            )
            multipartBody.addFormDataPart(
                name = "files",
                filename = "${System.currentTimeMillis()}.jpg",
                body = body
            )
        }
        return multipartBody.build()
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

    private fun handleRandomApi() {
        val ran = Random.nextInt(0, 20)
        val api = if (ran < 3) {
            val queryMap = mapOf(
                "pageNo" to "1",
                "pageSize" to "${Random.nextInt()}",
                "hi" to "helllloqweqweqweqweqweqwe"
            )
            requestGet(
                path = "/api/v1/memo/aos",
                queryMap = queryMap,
                headerMap = mapOf(
                    "X-FOO" to "DDDD",
                    "DDD" to "dfff"
                )
            )
        } else if (ran < 5) {
            requestGet(
                path = "/api/v1/til/goods",
                queryMap = mapOf(
                    "pageNo" to Random.nextInt(1, 11).toString(),
                    "pageSize" to "25"
                )
            )
        } else if (ran < 10) {
            val json = JSONObject()
            json.put("id", "efefefefef")
            requestPost(
                path = "/api/til/goods/like",
                body = json.toString().toRequestBody()
            )
        } else {
            requestGet(
                path = "/api/v1/til/jsend/list/meta"
            )
        }
        flow {
            emit(httpClient.newCall(api).execute())
        }.flowOn(Dispatchers.IO)
            .catch { Timber.d("ERROR $it") }
            .onEach { Timber.d("SUCC $it") }
            .launchIn(activity.lifecycleScope)
    }
}