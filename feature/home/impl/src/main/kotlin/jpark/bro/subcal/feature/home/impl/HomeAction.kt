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

    /** "결제일을 확인해 주세요" 카드: 누르면 목록, 닫으면 다시 보이지 않음 */
    data object BillingDateHintClicked : HomeAction

    data object BillingDateHintDismissed : HomeAction

    data class QuickAddClicked(
        val quickAdd: QuickAdd,
    ) : HomeAction
}
