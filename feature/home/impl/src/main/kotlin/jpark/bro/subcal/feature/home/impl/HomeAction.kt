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

    /** 무료체험 카드 · "결제됐나요?" (product-spec 4-1, 2-7) */
    sealed interface Trial : HomeAction

    /** "무료체험 n건 종료 임박" → 무료체험 화면 */
    data object TrialWarningClicked : Trial

    /** "결제됐나요?" → 계속 쓸래요 */
    data class UnresolvedKeepClicked(
        val subscriptionId: String,
    ) : Trial

    /** "결제됐나요?" → 해지했어요 */
    data class UnresolvedCancelledClicked(
        val subscriptionId: String,
    ) : Trial
}
