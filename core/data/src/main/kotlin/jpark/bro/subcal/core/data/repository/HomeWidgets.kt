package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import kotlinx.coroutines.flow.Flow

/**
 * 홈 화면 위젯 다루기 (product-spec 7장, widget/CLAUDE.md). 기능 모듈은 widget 모듈을 직접 참조할 수 없어
 * 이 인터페이스로 쓰고, 구현은 widget 모듈에 있다.
 */
interface HomeWidgets {
    /** 지금 홈 화면에 놓인 위젯과 그 설정 (설정한 적 없으면 기본값) */
    fun observePlacedWidgets(): Flow<List<WidgetConfig>>

    /** 런처가 앱에서 위젯 추가 요청(requestPinAppWidget)을 받는지 */
    fun isPinSupported(): Boolean

    /** 위젯 추가를 요청한다. 요청하지 못하면 false */
    fun requestPin(size: WidgetSize): Boolean

    /** 위젯 하나의 설정 화면을 연다 */
    fun openConfigure(widgetId: Int)
}
