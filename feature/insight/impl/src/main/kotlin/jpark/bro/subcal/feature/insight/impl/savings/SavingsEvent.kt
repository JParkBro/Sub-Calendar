package jpark.bro.subcal.feature.insight.impl.savings

internal sealed interface SavingsEvent {
    data object NavigateBack : SavingsEvent

    data class OpenDetail(
        val subscriptionId: String,
    ) : SavingsEvent

    data class OpenCancelPage(
        val url: String,
    ) : SavingsEvent
}
