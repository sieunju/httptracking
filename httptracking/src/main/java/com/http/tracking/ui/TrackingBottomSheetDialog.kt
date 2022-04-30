package com.http.tracking.ui

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.http.tracking.BR
import com.http.tracking.Extensions
import com.http.tracking.R
import com.http.tracking.TrackingManager
import com.http.tracking.databinding.DTrackingBottomSheetBinding
import com.http.tracking.entity.TrackingHttpEntity
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingListUiModel
import com.http.tracking.ui.detail.TrackingDetailRequestFragment
import com.http.tracking.ui.detail.TrackingDetailResponseFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import java.util.concurrent.TimeUnit

/**
 * Description : HTTP 로그 정보 보여주는 BottomSheetDialog
 *
 * Created by juhongmin on 2022/03/29
 */
internal class TrackingBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        var IS_SHOW = false
    }

    private val disposable: CompositeDisposable by lazy { CompositeDisposable() }
    val position: MutableLiveData<Int> by lazy { MutableLiveData<Int>().apply { value = 0 } }

    lateinit var binding: DTrackingBottomSheetBinding
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var trackingAdapter: Extensions.TrackingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupRatio(bottomSheetDialog)
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        if (dialog is BottomSheetDialog) {
            (dialog as BottomSheetDialog).runCatching {
                behavior.skipCollapsed = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<DTrackingBottomSheetBinding>(
            inflater,
            R.layout.d_tracking_bottom_sheet,
            container,
            false
        ).run {
            lifecycleOwner = this@TrackingBottomSheetDialog
            binding = this
            binding.setVariable(BR.dialog, this@TrackingBottomSheetDialog)
            return@run root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IS_SHOW = true
        pagerAdapter = PagerAdapter(this)
        trackingAdapter = Extensions.TrackingAdapter()
        trackingAdapter.setBottomSheetDialog(this)
        with(binding) {
            rvContents.adapter = trackingAdapter
            vp.adapter = pagerAdapter
            vp.offscreenPageLimit = 2
            vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(pos: Int) {
                    position.value = pos.plus(1)
                }
            })
        }

        updateTrackingData()

        dialog?.setOnDismissListener {
            dismiss()
        }
    }

    /**
     * 데이터 업데이트 처리 함수
     */
    fun updateTrackingData() {
        Single.just(TrackingManager.getInstance().getTrackingList())
            .map { list ->
                val uiList = mutableListOf<BaseTrackingUiModel>()
                list.map { uiList.add(TrackingListUiModel(it)) }
                return@map uiList
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                trackingAdapter.submitList(it)
            }, {

            }).addTo(disposable)
    }

    override fun dismiss() {
        IS_SHOW = false
        disposable.clear()
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        super.dismiss()
    }

    /**
     * 목록 화면으로 돌아가는 처리
     */
    fun onBack() {
        position.value = 0
    }

    fun onClear() {
        TrackingManager.getInstance().dataClear()
        updateTrackingData()
    }

    fun performDetail(entity: TrackingHttpEntity) {
        Single.just(entity)
            .delay(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                moveViewPager(0)
                childFragmentManager.runCatching {
                    val requestFragment = findFragmentByTag("f0")
                    val responseFragment = findFragmentByTag("f1")
                    if (requestFragment is TrackingDetailRequestFragment) {
                        requestFragment.performDetailEntity(it)
                    }
                    if (responseFragment is TrackingDetailResponseFragment) {
                        responseFragment.performDetailEntity(it)
                    }
                }
            }, {

            }).addTo(disposable)
    }

    private fun moveViewPager(pos: Int) {
        runCatching {
            position.value = 1
            binding.vp.post {
                binding.vp.setCurrentItem(pos, true)
            }
        }
    }

    /**
     * BottomSheet Device 비율에 맞게 높이값 조정 하는 함수
     */
    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetHeight()
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * Height 85%
     */
    private fun getBottomSheetHeight(): Int {
        return getDeviceHeight() * 70 / 100
    }

    @Suppress("DEPRECATION")
    private fun getDeviceHeight(): Int {
        val windowManager: WindowManager =
            requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    class PagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
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
}
