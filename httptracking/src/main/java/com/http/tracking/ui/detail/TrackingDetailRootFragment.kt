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
import com.http.tracking.R
import com.http.tracking.TrackingManagerBuilder
import com.http.tracking.ui.TrackingBottomSheetDialog
import com.http.tracking.util.WifiManager
import com.http.tracking.util.WifiShareManager
import com.http.tracking_interceptor.model.TrackingHttpEntity
import java.io.IOException

/**
 * Description : HTTP Tracking Detail Root Router Fragment
 *
 * Created by juhongmin on 2023/01/06
 */
internal class TrackingDetailRootFragment : Fragment(R.layout.f_tracking_detail),
    WifiShareManager.Listener {

    private lateinit var viewPager: ViewPager2
    private lateinit var ivShare: AppCompatImageView
    private lateinit var etPort: AppCompatEditText
    private lateinit var tvWifiShareStatus: AppCompatTextView

    private val wifiShareManager: WifiShareManager by lazy { WifiShareManager().setListener(this) }

    private val adapter: PagerAdapter by lazy { PagerAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        handleEditText()

        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) {
                if (pos == 0) {
                    setHeaderTitle(getDetailTitle("#222222", "#999999"))
                } else {
                    setHeaderTitle(getDetailTitle("#999999", "#222222"))
                }
            }
        })

        ivShare.setOnClickListener { handleShare() }
    }

    /**
     * getHeader Title
     */
    private fun getDetailTitle(reqColor: String, resColor: String): Spannable {
        val sb = SpannableStringBuilder()
        sb.append(
            "Request", ForegroundColorSpan(
                Color.parseColor(reqColor)
            ), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sb.append(" | ")
        sb.append(
            "Response", ForegroundColorSpan(
                Color.parseColor(resColor)
            ), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return sb
    }

    override fun onDestroyView() {
        stopWifiShare()
        super.onDestroyView()
    }

    override fun onServerStart(address: String) {
        view?.post {
            setWifiStatusText(address)
        }
    }

    private fun initView(view: View) {
        viewPager = view.findViewById(R.id.vp)
        ivShare = view.findViewById(R.id.ivShare)
        etPort = view.findViewById(R.id.etPort)
        tvWifiShareStatus = view.findViewById(R.id.tvWifiShareStatus)
        if (arguments?.getBoolean(TrackingBottomSheetDialog.WIFI_SHARE_KEY) == true) {
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
        etPort.setText(WifiManager.getInstance().getPort().toString(), TextView.BufferType.EDITABLE)
        etPort.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) return

                // Port Max Length 1024 < port <= 65535
                val prevNum = s.toString().toInt()
                if (prevNum > MAX_PORT) {
                    etPort.setText(MAX_PORT.toString(), TextView.BufferType.EDITABLE)
                } else {
                    WifiManager.getInstance().setPort(prevNum)
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
                wifiShareManager.start(wifiAddress, WifiManager.getInstance().getPort())
                wifiShareManager.setLogData(getDetailData())
            } catch (ex: IOException) {
                setWifiStatusText(TXT_SERVER_OFF)
            }
        } else {
            setWifiStatusText(TXT_WIFI_DISABLE)
        }
    }

    /**
     * Wifi 공유하기 기능 처리 함수
     */
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
        wifiShareManager.stop()
        setWifiStatusText(TXT_SERVER_OFF)
    }

    /**
     * set Header Title
     * @param txt Header Title
     */
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

    companion object {
        fun newInstance(bundle: Bundle?): Fragment {
            return TrackingDetailRootFragment().apply {
                arguments = bundle
            }
        }

        const val TXT_SERVER_OFF = "Wifi Share Off Port Range 1025-65535"
        const val TXT_WIFI_DISABLE = "The Wifi is Off"
        const val MIN_PORT = 1025 // System Port
        const val MAX_PORT = 65535
    }
}
