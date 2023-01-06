package com.http.tracking

import android.app.Activity
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import com.http.tracking.ui.TrackingBottomSheetDialog
import com.http.tracking_interceptor.TrackingDataManager
import java.lang.ref.WeakReference

/**
 * Description : HTTP 트레킹 매니저
 *
 * Created by juhongmin on 2022/03/29
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
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
    // [e] Variable

    private var dialog: TrackingBottomSheetDialog? = null

    private val activityListener = object : Application.ActivityLifecycleCallbacks {
        var currentActivity: WeakReference<Activity>? = null

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            currentActivity?.clear()
            currentActivity = WeakReference(activity)
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
                try {
                    if (dialog != null) {
                        dialog?.dismiss()
                        dialog = null
                    }
                    dialog = TrackingBottomSheetDialog()
                        .setListener(object : TrackingBottomSheetDialog.DismissListener {
                            override fun onDismiss() {
                                dialog = null
                            }
                        }).also {
                            if (act is FragmentActivity) {
                                it.show(act.supportFragmentManager, "TrackingBottomSheetDialog")
                            }
                        }
                } catch (ex: Exception) {

                }
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
        TrackingDataManager.getInstance().setBuildType(isDebug)
        return this
    }

    /**
     * 로그를 저장할 사이즈
     * @param size 사이즈
     */
    fun setLogMaxSize(size: Int): TrackingManager {
        TrackingDataManager.getInstance().setLogMaxSize(size)
        return this
    }

    fun isDebug() = isDebug

    fun isRelease() = !isDebug
}
