package jpark.bro.subcal.widget.config

import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.widget.WidgetDisplay

/** 위젯 하나의 설정 (product-spec 7장): 배경 + 미리보기 */
internal data class WidgetConfigState(
    val widgetId: Int,
    val size: WidgetSize,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val background: WidgetBackground = WidgetBackground.THEME,
    /** 미리보기에 보일 내용 (위젯과 같은 계산) */
    val display: WidgetDisplay? = null,
)
