package jpark.bro.subcal.feature.insight.impl.value

internal sealed interface ValueAnalysisEvent {
    data object NavigateBack : ValueAnalysisEvent

    data class OpenDetail(
        val subscriptionId: String,
    ) : ValueAnalysisEvent

    data object AddedToConsider : ValueAnalysisEvent

    data object ActionFailed : ValueAnalysisEvent
}
