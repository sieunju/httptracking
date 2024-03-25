package hmju.http.tracking_interceptor

import hmju.http.tracking_interceptor.model.TrackingModel
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Description : Interceptor 로 트레킹한 데이터들을 가지고 있는 클래스
 *
 * Created by juhongmin on 2022/09/02
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
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
        fun onUpdateTrackingData()
    }

    // [s] Variable
    private var isDebug = false
    private var logMaxSize = 1000
    private var listener: Listener? = null
    // [e] Variable

    private val httpTrackingList: CopyOnWriteArrayList<TrackingModel> by lazy { CopyOnWriteArrayList() }

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
    fun add(model: TrackingModel?) {
        if (model == null) return

        // UID 초기화 처리
        if (trackingCnt > Long.MAX_VALUE.minus(10)) {
            trackingCnt = 0
        }

        model.uid = trackingCnt
        httpTrackingList.add(0, model)
        trackingCnt++

        // 맥스 사이즈면 맨 마지막 삭제
        if (logMaxSize < httpTrackingList.size) {
            runCatching {
                httpTrackingList.removeAt(httpTrackingList.size.minus(1))
            }
        }
        this.listener?.onUpdateTrackingData()
    }

    fun getTrackingList(): List<TrackingModel> {
        return httpTrackingList.toList()
    }

    fun setListener(l: Listener) {
        this.listener = l
    }

    fun clear() {
        httpTrackingList.clear()
    }

    fun refresh() {
        this.listener?.onUpdateTrackingData()
    }
}
