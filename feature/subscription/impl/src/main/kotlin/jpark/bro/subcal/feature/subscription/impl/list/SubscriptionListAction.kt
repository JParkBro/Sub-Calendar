package jpark.bro.subcal.feature.subscription.impl.list

import jpark.bro.subcal.core.model.ListSort

internal sealed interface SubscriptionListAction {
    data class StatusFilterSelected(
        val filter: StatusFilter,
    ) : SubscriptionListAction

    /** 홈 · 통계에서 들어온 카테고리 필터 칩의 ✕ */
    data object CategoryFilterCleared : SubscriptionListAction

    data object SortClicked : SubscriptionListAction

    data class SortSelected(
        val sort: ListSort,
    ) : SubscriptionListAction

    data object SortSheetDismissed : SubscriptionListAction

    /** FAB */
    data object AddClicked : SubscriptionListAction

    data class RowClicked(
        val id: String,
    ) : SubscriptionListAction
}
