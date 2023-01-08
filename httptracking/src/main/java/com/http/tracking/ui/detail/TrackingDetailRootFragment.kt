package com.http.tracking.ui.detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.http.tracking.Extensions
import com.http.tracking.R
import com.http.tracking.ui.TrackingBottomSheetDialog
import com.http.tracking.util.WifiManager
import com.http.tracking_interceptor.model.TrackingHttpEntity
import com.http.tracking_interceptor.model.TrackingRequestEntity
import com.http.tracking_interceptor.model.TrackingRequestMultipartEntity
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import timber.log.Timber
import java.io.IOException
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/**
 * Description : HTTP Tracking Detail Root Router Fragment
 *
 * Created by juhongmin on 2023/01/06
 */
internal class TrackingDetailRootFragment : Fragment(R.layout.f_tracking_detail) {

    private lateinit var viewPager: ViewPager2
    private lateinit var ivShare: AppCompatImageView
    private lateinit var etPort: AppCompatEditText

    private var server: HttpServer? = null
    private var port: Int = 5000

    private val adapter: PagerAdapter by lazy { PagerAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        handleEditText()

        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) {
                if (pos == 0) {
                    setHeaderTitle("Request Detail")
                } else {
                    setHeaderTitle("Response Detail")
                }
            }
        })

        ivShare.setOnClickListener { handleShare() }
    }

    override fun onDestroyView() {
        handleHttpServerStop()
        super.onDestroyView()
    }

    private fun initView(view: View) {
        viewPager = view.findViewById(R.id.vp)
        ivShare = view.findViewById(R.id.ivShare)
        etPort = view.findViewById(R.id.etPort)
    }

    private fun handleEditText() {
        etPort.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null) return

                port = s.toString().toInt()
                Timber.d("onTextChanged $port")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun handleShare() {
        try {
            val wifiAddress = WifiManager.getInstance().getWifiAddress()
            if (server == null && !wifiAddress.isNullOrEmpty()) {
                server = HttpServer.create(InetSocketAddress(wifiAddress, port), 0)
                server?.executor = Executors.newCachedThreadPool()
                server?.createContext("/tracking", HttpTrackingRouter())
                server?.start()
            }
            Timber.d("ServerOn ${server?.address}")
        } catch (ex: IOException) {

        }
    }

    private fun handleHttpServerStop() {
        if (server != null) {
            server?.stop(0)
            Timber.d("Server Stop Success")
        }
    }

    private fun setHeaderTitle(txt: String) {
        if (parentFragment is TrackingBottomSheetDialog) {
            (parentFragment as TrackingBottomSheetDialog).setHeaderTitle(txt)
        }
    }

    inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(pos: Int): Fragment {
            return when (pos) {
                0 -> TrackingDetailRequestFragment.newInstance()
                else -> TrackingDetailResponseFragment.newInstance()
            }
        }
    }

    inner class HttpTrackingRouter : HttpHandler {
        override fun handle(exchange: HttpExchange?) {
            if (exchange == null) return
            val resBody = exchange.responseBody
            try {
                val sb = StringBuilder()
                sb.append("<!DOCTYPE html>")
                sb.append("<html>")
                sb.append("    <head>")
                sb.append("         <meta charset=\"utf-8\"")
                sb.append("         <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"")
                sb.append("         <title>Android Http Tracking Log</title>")
                sb.append("    </head>")
                sb.append(" <body>")
                sb.append("<br>")
                sb.append(getHttpTrackingJson())
                sb.append("<br>")
                sb.append(" </body>")
                sb.append("</html>")

                val response = sb.toString()

                // Set Response Headers
                val headers = exchange.responseHeaders
                headers.add("Content-Type", "text/html;charset=UTF-8")
                headers.add("Content-Length",response.length.toString())

                // Send Response Headers
                exchange.sendResponseHeaders(200, response.length.toLong())
                resBody.write(response.encodeToByteArray())
                resBody.close()
            } catch (ex: Exception) {
                Timber.d("Router Error $ex")
                resBody.close()
            } finally {
                exchange.close()
            }
        }
    }

    /**
     * Http Tracking Data
     */
    private fun getDetailData(): TrackingHttpEntity? {
        return if (parentFragment is TrackingBottomSheetDialog) {
            (parentFragment as TrackingBottomSheetDialog).getTempDetailData()
        } else {
            null
        }
    }

    private fun getHttpTrackingJson(): String {
        val str = StringBuilder()
        val data = getDetailData()
        if (data != null) {
            val req = data.req
            // Request Setting
            if (req != null) {
                // Full Url
                str.append("[URL] : ")
                str.append(req.fullUrl)
                // Header Setting
                str.append("[Headers]")
                str.append("<br>")
                data.headerMap.forEach { entry ->
                    str.append(entry.key)
                    str.append(" : ")
                    str.append(entry.value)
                    str.append("<br>")
                }

                // TrackingRequestMultipartEntity Skip
                if (req is TrackingRequestEntity) {
                    str.append(req.body)
                    str.append("<br>")
                }
            }
            val res = data.res
            if (res != null) {
                str.append("[Body]")
                str.append("<br>")
                str.append(Extensions.toJsonBody(res.body))
            }
        }
        return str.toString()
    }

    companion object {
        fun newInstance(): TrackingDetailRootFragment = TrackingDetailRootFragment()
    }
}
