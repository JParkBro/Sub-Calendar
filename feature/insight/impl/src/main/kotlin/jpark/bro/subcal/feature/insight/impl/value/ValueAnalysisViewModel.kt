package jpark.bro.subcal.feature.insight.impl.value

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.data.repository.UsageRepository
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class ValueAnalysisViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    usageRepository: UsageRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<ValueAnalysisState, ValueAnalysisAction, ValueAnalysisEvent>(ValueAnalysisState()) {
    init {
        combine(
            subscriptionRepository.observeRecords(),
            usageRepository.observeUsageLogs(),
            settingsRepository.observeAppSettings(),
        ) { records, usageLogs, settings ->
            val built = buildValueAnalysisState(records, usageLogs, settings, LocalDate.now(clock))
            setState { built.copy(isWorking = isWorking) }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: ValueAnalysisAction) {
        when (action) {
            ValueAnalysisAction.BackClicked -> {
                sendEvent(ValueAnalysisEvent.NavigateBack)
            }

            is ValueAnalysisAction.SubscriptionClicked -> {
                sendEvent(ValueAnalysisEvent.OpenDetail(action.subscriptionId))
            }

            is ValueAnalysisAction.AddToConsiderClicked -> {
                addToConsider(action.subscriptionId)
            }
        }
    }

    private fun addToConsider(subscriptionId: String) {
        if (currentState.isWorking) return
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            subscriptionRepository
                .setConsiderCancel(subscriptionId, considerCancel = true)
                .onSuccess { sendEvent(ValueAnalysisEvent.AddedToConsider) }
                .onFailure { sendEvent(ValueAnalysisEvent.ActionFailed) }
            setState { copy(isWorking = false) }
        }
    }
}
