package com.http.tracking.util

import android.app.Application
import android.content.Context
import android.net.*

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

        private const val PREF_KEY_PORT_NUM = "http_tracking_port_num"
    }

    private var application: Application? = null

    private val networkRequest: NetworkRequest by lazy {
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    // [s] Variable
    private var wifiAddress: String? = null
    private var isWifiEnabled = false
    // [e] Variable

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
            isWifiEnabled = true
        }

        override fun onLost(network: Network) {
            isWifiEnabled = false
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            val ipV4Address = linkProperties.linkAddresses.firstOrNull { linkAddress ->
                linkAddress.address.hostAddress?.contains(".") ?: false
            }?.address?.hostAddress
            wifiAddress = ipV4Address
        }
    }

    /**
     * Get Wifi Address 172.30.1.23
     */
    fun getWifiAddress(): String? {
        return wifiAddress
    }

    /**
     * Wifi Share HTTP Port Settings
     */
    fun setPort(port: Int) {
        val context = application?.applicationContext ?: return
        val pref = context.getSharedPreferences("http_tracking_preference", Context.MODE_PRIVATE)
        pref.edit()
            .putInt(PREF_KEY_PORT_NUM, port)
            .apply()
    }

    /**
     * Wifi Share HTTP Port Getting
     */
    fun getPort(): Int {
        val context = application?.applicationContext ?: return 50050

        val pref = context.getSharedPreferences("http_tracking_preference", Context.MODE_PRIVATE)
        return pref.getInt(PREF_KEY_PORT_NUM, 50050)
    }

    fun isWifiEnable() = isWifiEnabled
}
