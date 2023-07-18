package com.http.tracking

import android.app.Activity
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.http.tracking.ui.TrackingBottomSheetDialog
import com.http.tracking.util.WifiManager
import hmju.http_tracking.core.TrackingManager
import hmju.http_tracking.core.TrackingShakeDetector
import java.lang.ref.WeakReference

/**
 * Description : Tracking Manager Impl
 *
 * Created by juhongmin on 2023/07/18
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class TrackingManagerBuilder : TrackingManager {

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

    private val shakeListener = object : TrackingShakeDetector.OnShakeListener {
        override fun onShowDialog() {
            val act = activityListener.currentActivity?.get()
            if (act != null) {
                try {
                    if (dialog != null) {
                        dialog?.dismiss()
                        dialog = null
                    }
                    dialog = TrackingBottomSheetDialog()
                        .setWifiShare(isWifiShare)
                        .setListener(trackingBottomDialogDismissListener)
                        .also {
                            if (act is FragmentActivity) {
                                it.show(act.supportFragmentManager, "TrackingBottomSheetDialog")
                            }
                        }
                } catch (ex: Exception) {
                    // ignore
                }
            }
        }
    }

    private val trackingBottomDialogDismissListener =
        object : TrackingBottomSheetDialog.DismissListener {
            override fun onDismiss() {
                dialog = null
            }
        }

    private val shakeDetector: TrackingShakeDetector by lazy {
        TrackingShakeDetector().setListener(shakeListener)
    }

    private var dialog: TrackingBottomSheetDialog? = null
    private var isDebug: Boolean = false
    private var trackingLogMaxSize: Int = 500
    private var isWifiShare: Boolean = false

    fun isWifiShare(): Boolean {
        return isWifiShare
    }

    override fun setBuildType(isDebug: Boolean): TrackingManager {
        this.isDebug = isDebug
        return this
    }

    override fun setLogMaxSize(size: Int): TrackingManager {
        this.trackingLogMaxSize = size
        return this
    }

    override fun setWifiShare(isEnable: Boolean): TrackingManager {
        this.isWifiShare = isEnable
        return this
    }

    override fun build(application: Application) {
        if (isDebug) {
            application.registerActivityLifecycleCallbacks(activityListener)
            val sensorManager =
                application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.registerListener(
                shakeDetector,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI
            )

            // Wifi Share Enable
            if (isWifiShare) {
                WifiManager.getInstance().setApplication(application)
            }
        }
    }
}
