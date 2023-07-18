package hmju.http_tracking.core

import android.app.Application

/**
 * Description : Application 단에서 Tracking 에 대한 설정을 하기 위한
 * 인터페이스 클래스
 *
 * Created by juhongmin on 2023/07/18
 */
interface TrackingManager {

    /**
     * BuildType 설정 하는 함수
     *
     * @param isDebug true: HTTP Log Tracking, false: HTTP Log Tracking Disable
     */
    fun setBuildType(isDebug: Boolean): TrackingManager

    /**
     * HTTP Log를 최대로 저장 하는 사이즈값 설정하는 함수
     * HTTP Log 가 최대치까지 도달할경우 가장 오래된순으로 삭제하면서 저장합니다.
     * AKA. 자료구조 큐 방식
     * @param size Max HTTP Log Size
     */
    fun setLogMaxSize(size: Int): TrackingManager

    /**
     * HTTP Log 공유 하는 방식들 중 Wifi 방식 으로 PC 공유하는 기능에 대해 활성화 / 비 활성화 처리
     * 해당 함수를 호출하기전 추가한 라이브러리가 [com.github.sieunju.httptracking:ui] 인지 확인 해야합니다.
     * @param isEnable Wifi 공유 활성화 / 비 활성화
     */
    fun setWifiShare(isEnable: Boolean): TrackingManager

    /**
     * HTTP Tracking Build Start
     * @param application Android Application
     */
    fun build(application: Application)
}