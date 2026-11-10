package jpark.bro.subcal.core.model

/** 홈 화면 위젯 하나의 설정 (product-spec 7장). [widgetId]는 시스템이 주는 appWidgetId */
data class WidgetConfig(
    val widgetId: Int,
    val size: WidgetSize,
    val background: WidgetBackground = WidgetBackground.THEME,
)

enum class WidgetSize {
    /** 2×2: 가장 가까운 결제 1건 */
    SMALL,

    /** 4×2: 이번 달 구독료 + 다가오는 결제 2건 */
    MEDIUM,
}

enum class WidgetBackground {
    /** 기기 다크 모드를 따른다 */
    THEME,
    DARK,
    WHITE,
}
