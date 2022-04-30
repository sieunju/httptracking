package com.hmju.httptracking

import androidx.multidex.MultiDexApplication
import com.http.tracking.TrackingManager

/**
 * Description :
 *
 * Created by juhongmin on 2022/04/30
 */
class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        TrackingManager.getInstance()
            .setBuild(false)
            .build(this)
    }
}