package com.http.tracking.ui.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.http.tracking.R
import com.http.tracking.ui.TrackingBottomSheetDialog

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
        viewPager = view.findViewById(R.id.vp)
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

    companion object {
        fun newInstance(): TrackingDetailRootFragment = TrackingDetailRootFragment()
    }
}
