package com.http.tracking.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.http.tracking.Extensions
import com.http.tracking.R
import com.http.tracking.TrackingManager
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
    private lateinit var tvWifiShareStatus: AppCompatTextView

    private var server: HttpServer? = null
    private var port: Int = WifiManager.getInstance().getPort() // 초기값 셋팅

    private val adapter: PagerAdapter by lazy { PagerAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        handleEditText()

        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) {
                if (pos == 0) {
                    val sb = SpannableStringBuilder()
                    sb.append(
                        "Request", ForegroundColorSpan(
                            Color.parseColor("#222222")
                        ), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    sb.append(" | ")
                    sb.append(
                        "Response", ForegroundColorSpan(
                            Color.parseColor("#999999")
                        ), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setHeaderTitle(sb)
                } else {
                    val sb = SpannableStringBuilder()
                    sb.append(
                        "Request", ForegroundColorSpan(
                            Color.parseColor("#999999")
                        ), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    sb.append(" | ")
                    sb.append(
                        "Response", ForegroundColorSpan(
                            Color.parseColor("#222222")
                        ), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setHeaderTitle(sb)
                }
            }
        })

        ivShare.setOnClickListener { handleShare() }
    }

    override fun onDestroyView() {
        stopWifiShare()
        super.onDestroyView()
    }

    private fun initView(view: View) {
        viewPager = view.findViewById(R.id.vp)
        ivShare = view.findViewById(R.id.ivShare)
        etPort = view.findViewById(R.id.etPort)
        tvWifiShareStatus = view.findViewById(R.id.tvWifiShareStatus)
        if (TrackingManager.isWifiShare) {
            setWifiStatusText(TXT_SERVER_OFF)
            view.findViewById<LinearLayoutCompat>(R.id.llWifiShare).visibility = View.VISIBLE
        }
    }

    /**
     * Wifi Share Status set Text
     */
    private fun setWifiStatusText(txt: CharSequence) {
        tvWifiShareStatus.text = txt
        tvWifiShareStatus.isSelected = true
    }

    private fun handleEditText() {
        etPort.setText(port.toString(), TextView.BufferType.EDITABLE)
        etPort.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) return

                // Port Max Length 1024 < port <= 65535
                val prevNum = s.toString().toInt()
                if (prevNum > MAX_PORT) {
                    etPort.setText(MAX_PORT.toString(), TextView.BufferType.EDITABLE)
                } else {
                    port = prevNum
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Wifi Share Start 처리함수
     */
    private fun startWifiShare() {
        stopWifiShare()
        if (!WifiManager.getInstance().isWifiEnable()) {
            setWifiStatusText(TXT_WIFI_DISABLE)
            return
        }
        val wifiAddress = WifiManager.getInstance().getWifiAddress()
        if (!wifiAddress.isNullOrEmpty()) {
            try {
                server = HttpServer.create(InetSocketAddress(wifiAddress, port), 0)?.run {
                    executor = Executors.newCachedThreadPool()
                    createContext("/tracking", HttpTrackingRouter())
                    return@run this
                }
                server?.start()
                WifiManager.getInstance().setPort(port)
                val str = StringBuilder("http://")
                str.append(server?.address.toString().removePrefix("/"))
                str.append("/tracking")
                setWifiStatusText(str)
            } catch (ex: IOException) {
                setWifiStatusText(TXT_SERVER_OFF)
            }
        } else {
            setWifiStatusText(TXT_WIFI_DISABLE)
        }
    }

    private fun handleShare() {
        if (TrackingBottomSheetDialog.IS_SHOW_WIFI_SHARE_MSG) {
            startWifiShare()
        } else {
            AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setMessage("보안 문제로 공공장소에서 사용은 지양합니다.")
                .setPositiveButton("인지했습니다.") { _, _ ->
                    TrackingBottomSheetDialog.IS_SHOW_WIFI_SHARE_MSG = true
                    startWifiShare()
                }
                .show()
        }
    }

    private fun stopWifiShare() {
        if (server != null) {
            server?.stop(0)
            server = null
            setWifiStatusText(TXT_SERVER_OFF)
        }
    }

    private fun setHeaderTitle(txt: CharSequence) {
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

        private val defaultCharBufferSize = 8 * 1024 // Char Write Limit Size

        override fun handle(exchange: HttpExchange?) {
            if (exchange == null) return
            val resBody = exchange.responseBody
            try {
                val sb = StringBuilder()
                sb.append(
                    "<!DOCTYPE html>\n" +
                            "<html lang=\"en\">\n" +
                            "\n" +
                            "<head>\n" +
                            "    <meta charset=\"utf-8\">\n" +
                            "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n" +
                            "    <title>Http Tracking</title>\n" +
                            "</head>"
                )
                sb.append("\n")
                sb.append("<body>")
                sb.append("\n")
                sb.append(getHttpTrackingJson())
                sb.append("\n")
                sb.append("</body>")
                sb.append("\n")
                sb.append("</html>")

                val response = sb.toString()

                // Set Response Headers
                val headers = exchange.responseHeaders
                headers.add("Content-Type", "text/html;charset=UTF-8")
                exchange.sendResponseHeaders(200, response.encodeToByteArray().size.toLong())
                resBody.write(response.encodeToByteArray())

                // 2023. 01. 10 기존에 IOException too many bytes to write to stream 발생해서 버퍼를 잘라서 보냈는데
                // 계속 이슈가 발생해서 찾아보니 sendResponseHeaders Encode 된사이즈를 설정해줘야함 그래서 해당 로직은 사용 X
                /* var startIdx = 0
                Timber.d("RouterInfo StartIdx: $startIdx ResponseLength: ${response.length}")
                while (startIdx < response.length) {
                    val endIdx =
                        startIdx.plus(defaultCharBufferSize).coerceAtMost(response.length)
                    Timber.d("StartIdx $startIdx EndIdx $endIdx")
                    val subString = response.substring(startIdx, endIdx)
                    val subByte = subString.encodeToByteArray()
                    startIdx += defaultCharBufferSize
                    resBody.write(subByte)
                } */

                // Send Response Headers
                resBody.flush()
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
        // Android Http Tracking Log
        str.append("<h3>Android HTTP Tracking Log by.hmju</h3>")
        val data = getDetailData()
        if (data != null) {
            // Log Print Format
            // 1. {MethodType} {BaseUrl + Path}
            // 2. {Request Query Parameter}
            // 3. {Request Body}
            // 4. {Headers}
            // 5. Response Code
            // 6. Response Body
            // 7. Response Error
            val req = data.req
            // Request Setting
            if (req != null) {
                // 1. {MethodType} {BaseUrl + Path}
                str.append("<h4>[${data.method}] ${data.scheme}://${data.baseUrl}${data.path}</h4>")

                // 2. {Request Query Parameter or Request Body}
                val queryList = Extensions.toReqQueryList(req.fullUrl)
                if (queryList.isNotEmpty()) {
                    str.append("<h5>[Request Query Parameters]</h5>")
                    queryList.forEach {
                        str.append(it.plus("<br>"))
                    }
                }

                // 3. {Request Body}
                if (req is TrackingRequestEntity) {
                    if (!req.body.isNullOrEmpty()) {
                        str.append("<h5>[Request Body - Json]</h5>")
                        str.append("<pre>")
                        str.append(Extensions.toJsonBody(req.body).replace("\n", "<br>"))
                        str.append("</pre>")
                    }
                } else if (req is TrackingRequestMultipartEntity) {
                    str.append("<h5>[Request Body - MultipartType]</h5>")
                    req.binaryList.forEach {
                        str.append("MultiPart-MediaType: ")
                        str.append(it.type)
                        str.append(" Length: ")
                        str.append(it.bytes?.size)
                        str.append("<br>")
                    }
                }

                // 4. {Headers}
                if (data.headerMap.isNotEmpty()) {
                    str.append("<h5>[Headers]</h5>")
                    data.headerMap.forEach { entry ->
                        str.append(entry.key)
                        str.append(" : ")
                        str.append(entry.value)
                        str.append("<br>")
                    }
                }

                // 5. Response Code
                if (data.isSuccess()) {
                    str.append("<H4><font color=\"#03A9F4\">${data.code}</font></H4>")
                } else {
                    str.append("<H4><font color=\"#C62828\">${data.code}</font></H4>")
                }
            }

            val res = data.res

            if (res != null && !res.body.isNullOrEmpty()) {
                // 6. Response Body
                str.append("<h5>[Body]</h5>")
                str.append("<pre>")
                str.append(Extensions.toJsonBody(res.body).replace("\n", "<br>"))
                str.append("</pre>")
            }

            // 7. Response Error
            if (data.error != null) {
                str.append("<br>")
                str.append("<h5>[HTTP Error]</h5>")
                str.append(data.error)
                str.append("<br>")
            }
        }
        return str.toString()
    }

    companion object {
        fun newInstance(): TrackingDetailRootFragment = TrackingDetailRootFragment()
        const val TXT_SERVER_OFF = "Wifi Share Off Port Range 1025-65535"
        const val TXT_WIFI_DISABLE = "The Wifi is Off"
        const val MIN_PORT = 1025 // System Port
        const val MAX_PORT = 65535
    }
}
