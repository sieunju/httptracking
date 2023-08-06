package hmju.http.tracking

import android.app.Activity
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import hmju.http.tracking.ui.TrackingBottomSheetDialog
import hmju.http.tracking.util.TrackingShakeDetector
import java.lang.ref.WeakReference

/**
 * Description : Http Tracking Manager
 *
 * Created by juhongmin on 2023/08/06
 */
class HttpTracking(
    app: Application,
    builder: Builder
) {

    private val isDebug: Boolean
    private val logMaxSize: Int
    private val isWifiShare: Boolean
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

    private val dialogDismissListener = object : TrackingBottomSheetDialog.DismissListener {
        override fun onDismiss() {
            dialog = null
        }
    }

    init {
        isDebug = builder.isDebug
        logMaxSize = builder.logMaxSize
        isWifiShare = builder.isWifiShare
        if (isDebug) {
            initTracking(app)
        }
    }

    /**
     * Tracking 초기화 함수
     * @param app Application
     */
    private fun initTracking(app: Application) {
        app.registerActivityLifecycleCallbacks(activityListener)
        val shakeListener = object : TrackingShakeDetector.OnShakeListener {
            override fun onShowDialog() {
                val act = activityListener.currentActivity?.get()
                if (act != null) {
                    try {
                        dismissDialog()
                        showDialog(act)
                    } catch (ex: Exception) {
                        // ignore
                    }
                }
            }
        }
        val sm = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val shakeDetector = TrackingShakeDetector().setListener(shakeListener)
        sm.registerListener(
            shakeDetector,
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun dismissDialog() {
        if (dialog != null) {
            dialog?.dismiss()
            dialog = null
        }
    }

    private fun showDialog(act: Activity) {
        val trackingDialog = TrackingBottomSheetDialog()
            .setWifiShare(isWifiShare)
            .setListener(dialogDismissListener)
        if (act is FragmentActivity) {
            trackingDialog.show(act.supportFragmentManager, "TrackingBottomSheetDialog")
        }
        dialog = trackingDialog
    }

    class Builder {
        var isDebug: Boolean = false
            private set
        var logMaxSize: Int = 500
            private set
        var isWifiShare: Boolean = false
            private set

        /**
         * BuildType 설정 하는 함수
         *
         * @param isDebug true: HTTP Log Tracking, false: HTTP Log Tracking Disable
         */
        fun setBuildType(isDebug: Boolean): Builder {
            this.isDebug = isDebug
            return this
        }

        /**
         * HTTP Log를 최대로 저장 하는 사이즈값 설정하는 함수
         * HTTP Log 가 최대치까지 도달할경우 가장 오래된순으로 삭제하면서 저장합니다.
         * AKA. 자료구조 큐 방식
         * @param size Max HTTP Log Size
         */
        fun setLogMaxSize(size: Int): Builder {
            this.logMaxSize = size
            return this
        }

        /**
         * HTTP Log 공유 하는 방식들 중 Wifi 방식 으로 PC 공유하는 기능에 대해 활성화 / 비 활성화 처리
         * 해당 함수를 호출하기전 추가한 라이브러리가 [com.github.sieunju.httptracking:ui] 인지 확인 해야합니다.
         * @param isEnable Wifi 공유 활성화 / 비 활성화
         */
        fun setWifiShare(isEnable: Boolean): Builder {
            this.isWifiShare = isEnable
            return this
        }

        /**
         * HTTP Tracking Build Start
         * @param application Android Application
         */
        fun build(application: Application): HttpTracking {
            return HttpTracking(application, this)
        }
    }
}
