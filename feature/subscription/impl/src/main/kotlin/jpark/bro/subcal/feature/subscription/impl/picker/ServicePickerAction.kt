package jpark.bro.subcal.feature.subscription.impl.picker

import jpark.bro.subcal.core.model.Category

internal sealed interface ServicePickerAction {
    data class QueryChanged(
        val query: String,
    ) : ServicePickerAction

    /** null이면 전체 */
    data class CategorySelected(
        val category: Category?,
    ) : ServicePickerAction

    data class ServiceClicked(
        val presetId: String,
    ) : ServicePickerAction

    /** 하단 "목록에 없어요" */
    data object ManualEntryClicked : ServicePickerAction

    data object CloseClicked : ServicePickerAction
}
