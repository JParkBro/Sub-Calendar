package jpark.bro.subcal.feature.insight.impl.value

internal sealed interface ValueAnalysisAction {
    data object BackClicked : ValueAnalysisAction

    /** 목록 항목 → 구독 상세 */
    data class SubscriptionClicked(
        val subscriptionId: String,
    ) : ValueAnalysisAction

    /** "해지 고민함에 담을까요?" 카드의 담기 */
    data class AddToConsiderClicked(
        val subscriptionId: String,
    ) : ValueAnalysisAction
}
