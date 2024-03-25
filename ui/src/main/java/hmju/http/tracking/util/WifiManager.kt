package hmju.http.tracking.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Description : Wifi Manager
 *
 * Created by juhongmin on 2023/08/06
 */
internal class WifiManager constructor(
    private val context: Context
) {

    private var address: String? = null
    private var isWifiEnabled = false

    private val PREF_KEY_PORT_NUM = "http_tracking_port_num"
    private val networkRequest: NetworkRequest by lazy {
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
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
            address = ipV4Address
        }
    }

    init {
        val cm: ConnectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.requestNetwork(networkRequest, networkCallback)
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     * Get Wifi Address 172.30.1.23
     */
    fun getWifiAddress(): String? {
        return address
    }

    /**
     * Wifi Share HTTP Port Settings
     */
    fun setPort(port: Int) {
        val pref = context.getSharedPreferences("http_tracking_preference", Context.MODE_PRIVATE)
        pref.edit()
            .putInt(PREF_KEY_PORT_NUM, port)
            .apply()
    }

    /**
     * Wifi Share HTTP Port Getting
     */
    fun getPort(): Int {
        val pref = context.getSharedPreferences("http_tracking_preference", Context.MODE_PRIVATE)
        return pref.getInt(PREF_KEY_PORT_NUM, 50050)
    }

    fun isWifiEnable() = isWifiEnabled
}
