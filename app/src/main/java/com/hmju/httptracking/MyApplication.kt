package com.hmju.httptracking

import android.app.Application
import hmju.http.tracking.HttpTracking
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initTimber()
        HttpTracking.Builder()
            .setBuildType(true)
            .setWifiShare(true)
            .setLogMaxSize(50)
            .build(this)

    }

    private fun initTimber() {
        Timber.plant(object : Timber.DebugTree() {

            override fun createStackElementTag(element: StackTraceElement): String {
                return "Timber_${super.createStackElementTag(element)}"
                // return "Timber_${element.methodName.substringBeforeLast(".")}"
            }
        })
    }
}