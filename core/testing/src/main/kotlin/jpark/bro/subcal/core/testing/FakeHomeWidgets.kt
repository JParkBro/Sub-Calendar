package jpark.bro.subcal.core.testing

import jpark.bro.subcal.core.data.repository.HomeWidgets
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 놓인 위젯 목록과 추가 요청 결과를 정해 둔다 */
class FakeHomeWidgets(
    placed: List<WidgetConfig> = emptyList(),
    var pinSupported: Boolean = true,
    var pinAccepted: Boolean = true,
) : HomeWidgets {
    val placed = MutableStateFlow(placed)

    /** 마지막으로 추가를 요청한 크기 */
    var requestedSize: WidgetSize? = null
        private set

    /** 마지막으로 설정 화면을 연 위젯 id */
    var openedWidgetId: Int? = null
        private set

    override fun observePlacedWidgets(): Flow<List<WidgetConfig>> = placed

    override fun isPinSupported(): Boolean = pinSupported

    override fun requestPin(size: WidgetSize): Boolean {
        requestedSize = size
        return pinSupported && pinAccepted
    }

    override fun openConfigure(widgetId: Int) {
        openedWidgetId = widgetId
    }
}
