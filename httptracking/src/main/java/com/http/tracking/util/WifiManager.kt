package com.http.tracking.util

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.os.Build
import timber.log.Timber

/**
 * Description : Wifi Manager
 *
 * Created by juhongmin on 2023/01/08
 */
internal class WifiManager private constructor() {

    companion object {
        @Volatile
        private var instance: WifiManager? = null

        @JvmStatic
        fun getInstance(): WifiManager {
            return instance ?: synchronized(this) {
                instance ?: WifiManager().also {
                    instance = it
                }
            }
        }
    }

    private var application: Application? = null

    private val networkRequest: NetworkRequest by lazy {
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    private var wifiAddress: String? = null

    /**
     * init WifiManager
     */
    fun setApplication(application: Application) {
        this.application = application
        startWifiTracking(application)
    }

    private fun startWifiTracking(application: Application) {
        val cm: ConnectivityManager by lazy {
            application.applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
        }
        cm.requestNetwork(networkRequest, networkCallback)
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiInfo = capabilities.transportInfo as WifiInfo
                val wifiIp = wifiInfo.ipAddress
                Timber.d("OriginAddress $wifiIp")
                val formattedIpAddress = String.format(
                    "%d.%d.%d.%d",
                    wifiIp and 0xff,
                    wifiIp shr 8 and 0xff,
                    wifiIp shr 16 and 0xff,
                    wifiIp shr 24 and 0xff
                )
                Timber.d("WifiInfo Address $formattedIpAddress")
            } else {

            }
        }
    }

    fun getWifiAddress(): String? {
        val context = application?.applicationContext ?: return null
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val linkAddresses = cm.getLinkProperties(cm.activeNetwork)?.linkAddresses
            Timber.d("LinkAddress $linkAddresses")
            val ipV4Address = linkAddresses?.firstOrNull { linkAddress ->
                linkAddress.address.hostAddress?.contains(".") ?: false
            }?.address?.hostAddress
            Timber.d("WifiAddress $ipV4Address")
            return ipV4Address
        } else {

            return null
        }
    }
}
