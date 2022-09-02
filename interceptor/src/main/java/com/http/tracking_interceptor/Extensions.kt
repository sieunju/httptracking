package com.http.tracking_interceptor

import java.text.SimpleDateFormat

internal object Extensions {

    @Suppress("SimpleDateFormat")
    private val simpleDate = SimpleDateFormat("HH:mm:ss")

    fun Long.toDate(): String {
        return simpleDate.format(this)
    }
}