package jpark.bro.subcal.feature.home.impl

import jpark.bro.subcal.core.model.Category

internal sealed interface HomeAction {
    data class UpcomingClicked(
        val subscriptionId: String,
    ) : HomeAction

    /** 다가오는 결제의 "전체보기" */
    data object SeeAllClicked : HomeAction

    data class CategoryClicked(
        val category: Category,
    ) : HomeAction

    /** FAB */
    data object AddClicked : HomeAction

    data class QuickAddClicked(
        val quickAdd: QuickAdd,
    ) : HomeAction
}
