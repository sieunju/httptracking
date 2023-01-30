package com.http.disable_wifi_tracking

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt


/**
 * Description : 쉐이크 감지 클래스
 *
 * Created by juhongmin on 2022/03/30
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
internal class ShakeDetector : SensorEventListener {

    companion object {
        const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        const val SHAKE_SLOP_TIME_MS = 500
        const val SHAKE_COUNT_RESET_TIME_MS = 1000
    }

    private var shakeTimeStamp: Long = 0L
    private var shakeCnt: Int = 0

    interface OnShakeListener {
        fun onShowDialog()
    }

    private var listener: OnShakeListener? = null

    fun setListener(listener: OnShakeListener): ShakeDetector {
        this.listener = listener
        return this
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // ignore
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()

            if (shakeTimeStamp + SHAKE_SLOP_TIME_MS > now) {
                return
            }

            if (shakeTimeStamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                shakeCnt = 0
            }
            // 업데이트한다
            shakeTimeStamp = now
            shakeCnt++
            // 흔들렸을 때 행동을 설정한다
            if (shakeCnt > 0) {
                listener?.onShowDialog()
            }
        }
    }
}
