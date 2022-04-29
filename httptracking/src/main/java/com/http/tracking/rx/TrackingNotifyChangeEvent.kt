package com.http.tracking.rx

import io.reactivex.rxjava3.subjects.PublishSubject


/**
 * Description : 실시간으로 API 호출되는 경우 주기적으로 갱신 처리를 알려주는 RxBus 이벤트
 *
 * Created by juhongmin on 2022/04/01
 */
internal object TrackingNotifyChangeEvent {
    private val publisher = PublishSubject.create<Long>()

    fun publish(cnt: Long) {
        publisher.onNext(cnt)
    }

    fun listen(): PublishSubject<Long> = publisher
}