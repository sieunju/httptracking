package com.http.disable_wifi_tracking.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.http.disable_wifi_tracking.R
import com.http.disable_wifi_tracking.ui.TrackingBottomSheetDialog

/**
 * Description : HTTP Tracking Detail Root Router Fragment
 *
 * Created by juhongmin on 2023/01/06
 */
internal class TrackingDetailRootFragment : Fragment(R.layout.f_tracking_detail) {

    private lateinit var viewPager: ViewPager2

    private val adapter: PagerAdapter by lazy { PagerAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)

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

    private fun initView(view: View) {
        viewPager = view.findViewById(R.id.vp)
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

    companion object {
        fun newInstance(): TrackingDetailRootFragment = TrackingDetailRootFragment()
        const val TXT_SERVER_OFF = "Wifi Share Off Port Range 1025-65535"
        const val TXT_WIFI_DISABLE = "The Wifi is Off"
        const val MIN_PORT = 1025 // System Port
        const val MAX_PORT = 65535
    }
}
