package jpark.bro.subcal.feature.insight.impl.stats

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.ui.MviViewModel
import jpark.bro.subcal.feature.insight.api.SavingsTab
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import java.time.Clock
import java.time.LocalDate

internal class StatsViewModel(
    subscriptionRepository: SubscriptionRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<StatsState, StatsAction, StatsEvent>(StatsState()) {
    init {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
        ) { records, settings ->
            val state = buildStatsState(records, settings, LocalDate.now(clock))
            setState { state }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: StatsAction) {
        when (action) {
            is StatsAction.CategoryClicked -> sendEvent(StatsEvent.OpenList(action.category))
            StatsAction.ConsiderCardClicked -> sendEvent(StatsEvent.OpenSavings(SavingsTab.CONSIDER))
            StatsAction.SavedCardClicked -> sendEvent(StatsEvent.OpenSavings(SavingsTab.SAVED))
        }
    }
}
