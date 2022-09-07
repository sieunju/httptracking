package com.http.tracking_interceptor

import com.http.tracking_interceptor.model.BaseTrackingEntity
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Description : Interceptor 로 트레킹한 데이터들을 가지고 있는 클래스
 *
 * Created by juhongmin on 2022/09/02
 */
class TrackingDataManager private constructor() {
    companion object {
        @Volatile
        private var instance: TrackingDataManager? = null

        private var trackingCnt: Long = 0L

        @JvmStatic
        fun getInstance(): TrackingDataManager {
            return instance ?: synchronized(this) {
                instance ?: TrackingDataManager().also {
                    instance = it
                }
            }
        }
    }

    interface Listener {
        fun onNotificationTrackingEntity()
    }

    // [s] Variable
    private var isDebug = false
    private var logMaxSize = 1000
    private var listener: Listener? = null
    // [e] Variable

    // Tracking List CopyOnWriteArrayList 고민해봐야함..
    private val httpTrackingList: ConcurrentLinkedDeque<BaseTrackingEntity> by lazy { ConcurrentLinkedDeque() }

    fun isDebug() = isDebug

    fun isRelease() = !isDebug

    /**
     * Set Build Type
     * @param isDebug 디버그 모드 true, 릴리즈 모드 false
     */
    fun setBuildType(isDebug: Boolean) {
        this.isDebug = isDebug
    }

    /**
     * 로그를 저장할 사이즈
     * @param size 사이즈
     */
    fun setLogMaxSize(size: Int) {
        this.logMaxSize = size
    }

    /**
     * Http 통신 트레킹 추가 함수
     *
     */
    fun addTracking(entity: BaseTrackingEntity?) {
        if (entity == null) return

        // UID 초기화 처리
        if (trackingCnt > Long.MAX_VALUE.minus(10)) {
            trackingCnt = 0
        }

        entity.uid = trackingCnt
        httpTrackingList.push(entity)
        trackingCnt++

        // 맥스 사이즈면 맨 마지막 삭제
        if (logMaxSize < httpTrackingList.size) {
            httpTrackingList.pop()
        }
        this.listener?.onNotificationTrackingEntity()
    }

    fun getTrackingList(): List<BaseTrackingEntity> {
        return httpTrackingList.toList()
    }

    fun setListener(l: Listener) {
        this.listener = l
    }

    fun clear() {
        httpTrackingList.clear()
    }
}
