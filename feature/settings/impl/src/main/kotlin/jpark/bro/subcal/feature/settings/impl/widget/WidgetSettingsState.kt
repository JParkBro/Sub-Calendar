package jpark.bro.subcal.feature.settings.impl.widget

import jpark.bro.subcal.core.model.WidgetConfig

/** 위젯 설정 (product-spec 6장, widget/CLAUDE.md "설정"): 놓인 위젯 목록 + 위젯 추가 */
internal data class WidgetSettingsState(
    val isLoading: Boolean = true,
    val widgets: List<WidgetConfig> = emptyList(),
    /** 런처가 앱에서 위젯 추가 요청을 받을 때만 "위젯 추가" 버튼 */
    val canAddWidget: Boolean = false,
    val isSizeSheetOpen: Boolean = false,
)
