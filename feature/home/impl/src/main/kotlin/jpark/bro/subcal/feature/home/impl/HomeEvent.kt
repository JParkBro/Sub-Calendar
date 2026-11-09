package jpark.bro.subcal.feature.home.impl

import jpark.bro.subcal.core.model.Category

internal sealed interface HomeEvent {
    data class OpenDetail(
        val subscriptionId: String,
    ) : HomeEvent

    /** 캘린더 탭으로 */
    data object OpenCalendar : HomeEvent

    /** 목록 탭을 카테고리로 걸러서 */
    data class OpenList(
        val category: Category,
    ) : HomeEvent

    data object OpenServicePicker : HomeEvent

    /** 빠른 등록: [presetId]가 null이면 직접 입력 */
    data class OpenEdit(
        val presetId: String?,
    ) : HomeEvent
}
