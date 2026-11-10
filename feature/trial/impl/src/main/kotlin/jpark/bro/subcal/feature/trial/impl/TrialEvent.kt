package jpark.bro.subcal.feature.trial.impl

internal sealed interface TrialEvent {
    data object NavigateBack : TrialEvent

    data object OpenAddTrial : TrialEvent

    data class OpenDetail(
        val subscriptionId: String,
    ) : TrialEvent

    data object ActionFailed : TrialEvent
}
