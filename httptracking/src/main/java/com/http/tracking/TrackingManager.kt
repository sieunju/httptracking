package com.http.tracking

import android.app.Activity
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.http.tracking.entity.TrackingHttpEntity
import com.http.tracking.ui.TrackingBottomSheetDialog
import java.lang.ref.WeakReference

/**
 * Description : HTTP 트레킹 매니저
 *
 * Created by juhongmin on 2022/03/29
 */
class TrackingManager private constructor() {

    companion object {
        @Volatile
        private var instance: TrackingManager? = null

        private var trackingCnt: Long = 0L

        @JvmStatic
        fun getInstance(): TrackingManager {
            return instance ?: synchronized(this) {
                instance ?: TrackingManager().also {
                    instance = it
                }
            }
        }
    }

    // [s] Variable
    private var isDebug = false
    private var logMaxSize = 1000
    // [e] Variable

    // Tracking List
    private val httpTrackingList: MutableList<TrackingHttpEntity> by lazy { mutableListOf() }

    // Gson
//    private val gson: Gson by lazy {
//        GsonBuilder()
//            .disableHtmlEscaping()
//            .setPrettyPrinting()
//            .serializeNulls()
//            .create()
//    }

    private var dialog: TrackingBottomSheetDialog? = null

    private val activityListener = object : Application.ActivityLifecycleCallbacks {
        var currentActivity: WeakReference<FragmentActivity>? = null

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            if (activity is FragmentActivity) {
                currentActivity?.clear()
                currentActivity = WeakReference(activity)
            }
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

    private val shakeListener = object : ShakeDetector.OnShakeListener {
        override fun onShowDialog() {
            activityListener.currentActivity?.get()?.let { act ->
                if (dialog != null) {
                    dialog?.dismiss()
                    dialog = null
                }
                dialog = TrackingBottomSheetDialog()
                dialog?.show(act.supportFragmentManager, "TrackingBottomSheetDialog")
            }
        }
    }

    private val shakeDetector: ShakeDetector by lazy { ShakeDetector().setListener(shakeListener) }

    /**
     * Builder
     * @param context Application Context
     */
    fun build(context: Application) {
        if (isDebug()) {
            context.registerActivityLifecycleCallbacks(activityListener)
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.registerListener(
                shakeDetector,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    /**
     * Set Build Type
     * @param isDebug 디버그 모드 true, 릴리즈 모드 false
     */
    fun setBuildType(isDebug: Boolean): TrackingManager {
        this.isDebug = isDebug
        return this
    }

    /**
     * 로그를 저장할 사이즈
     * @param size 사이즈
     */
    fun setLogMaxSize(size: Int): TrackingManager {
        this.logMaxSize = size
        return this
    }

    fun isDebug() = isDebug

    fun isRelease() = !isDebug

    internal fun getTrackingList(): List<TrackingHttpEntity> {
        val tmpList = mutableListOf<TrackingHttpEntity>()
        httpTrackingList.forEach {
            tmpList.add(it)
        }
        return tmpList
    }

    internal fun addTracking(entity: TrackingHttpEntity?) {
        if (entity == null) return
        // UID 초기화 처리
        if (trackingCnt > Long.MAX_VALUE.minus(10)) {
            trackingCnt = 0
        }

        entity.uid = trackingCnt
        httpTrackingList.add(0, entity)
        trackingCnt++
        // 맥스 사이즈 맨 첫번째꺼 삭제
        if (logMaxSize < httpTrackingList.size) {
            httpTrackingList.removeFirst()
        }
        dialog?.updateTrackingData()
    }

    internal fun dataClear() {
        httpTrackingList.clear()
    }

//    internal fun getGson() = gson
}
