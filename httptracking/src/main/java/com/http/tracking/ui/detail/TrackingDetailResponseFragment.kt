package com.http.tracking.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.http.tracking.Extensions
import com.http.tracking.R
import com.http.tracking.databinding.FTrackingDetailResponseBinding
import com.http.tracking.entity.TrackingHttpEntity
import com.http.tracking.models.BaseTrackingUiModel
import com.http.tracking.models.TrackingTitleUiModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

/**
 * Description : 상세 > Response Fragment
 *
 * Created by juhongmin on 2022/04/02
 */
internal class TrackingDetailResponseFragment : Fragment() {

    companion object {
        fun newInstance(): TrackingDetailResponseFragment = TrackingDetailResponseFragment()
    }

    private val disposable: CompositeDisposable by lazy { CompositeDisposable() }

    private lateinit var binding: FTrackingDetailResponseBinding
    private val adapter: Extensions.TrackingAdapter by lazy { Extensions.TrackingAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<FTrackingDetailResponseBinding>(
            inflater,
            R.layout.f_tracking_detail_response,
            container,
            false
        ).run {
            lifecycleOwner = this@TrackingDetailResponseFragment.viewLifecycleOwner
            binding = this
            return@run root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvContents.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
    }

    /**
     * Request Detail 처리
     * @param entity 트레킹 데이터 모델
     */
    fun performDetailEntity(entity: TrackingHttpEntity) {
        Single.just(entity)
            .map { parseUiModel(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("Request Data ${it.size}")
                adapter.submitList(it)
            }, {

            }).addTo(disposable)
    }

    private fun parseUiModel(entity: TrackingHttpEntity): List<BaseTrackingUiModel> {
        val uiList = mutableListOf<BaseTrackingUiModel>()
        entity.res?.body?.let { body ->
            uiList.add(TrackingTitleUiModel("[Body]"))
            uiList.add(Extensions.parseBodyUiModel(body))
        }
        return uiList
    }
}
