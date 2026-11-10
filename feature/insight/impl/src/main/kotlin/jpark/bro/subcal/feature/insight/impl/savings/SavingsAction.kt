package jpark.bro.subcal.feature.insight.impl.savings

import jpark.bro.subcal.feature.insight.api.SavingsTab

internal sealed interface SavingsAction {
    data object BackClicked : SavingsAction

    data class TabSelected(
        val tab: SavingsTab,
    ) : SavingsAction

    data class SubscriptionClicked(
        val subscriptionId: String,
    ) : SavingsAction

    /** "해지하러 가기": 해지 주소가 있으면 브라우저, 없으면 구독 상세 */
    data class CancelPageClicked(
        val subscriptionId: String,
    ) : SavingsAction
}
