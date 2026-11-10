package jpark.bro.subcal.feature.trial.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.domain.trial.trialCancelDate
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class TrialViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<TrialState, TrialAction, TrialEvent>(TrialState()) {
    private val today: LocalDate get() = LocalDate.now(clock)

    /** 해지일을 정하기 위한 최근 구독 목록 */
    private var records: List<SubscriptionRecord> = emptyList()

    init {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
        ) { records, settings ->
            this.records = records
            val state = buildTrialState(records, settings.usdKrwRate, today)
            setState { state.copy(isWorking = isWorking) }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: TrialAction) {
        when (action) {
            TrialAction.BackClicked -> {
                sendEvent(TrialEvent.NavigateBack)
            }

            TrialAction.AddClicked -> {
                sendEvent(TrialEvent.OpenAddTrial)
            }

            is TrialAction.TrialClicked -> {
                sendEvent(TrialEvent.OpenDetail(action.subscriptionId))
            }

            is TrialAction.KeepClicked -> {
                write { subscriptionRepository.keepTrial(action.subscriptionId) }
            }

            is TrialAction.CancelledClicked -> {
                val record = records.firstOrNull { it.subscription.id == action.subscriptionId } ?: return
                write { subscriptionRepository.cancel(action.subscriptionId, record.trialCancelDate(today)) }
            }
        }
    }

    private fun write(block: suspend () -> Result<Unit>) {
        if (currentState.isWorking) return
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            block().onFailure { sendEvent(TrialEvent.ActionFailed) }
            setState { copy(isWorking = false) }
        }
    }
}
