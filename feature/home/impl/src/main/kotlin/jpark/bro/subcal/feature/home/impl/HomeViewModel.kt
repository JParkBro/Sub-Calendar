package jpark.bro.subcal.feature.home.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import java.time.Clock
import java.time.LocalDate

internal class HomeViewModel(
    subscriptionRepository: SubscriptionRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    init {
        combine(subscriptionRepository.observeRecords(), settingsRepository.observeAppSettings()) { records, settings ->
            val state = buildHomeState(records, settings, LocalDate.now(clock))
            setState { state }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.UpcomingClicked -> sendEvent(HomeEvent.OpenDetail(action.subscriptionId))
            HomeAction.SeeAllClicked -> sendEvent(HomeEvent.OpenCalendar)
            is HomeAction.CategoryClicked -> sendEvent(HomeEvent.OpenList(action.category))
            HomeAction.AddClicked -> sendEvent(HomeEvent.OpenServicePicker)
            is HomeAction.QuickAddClicked -> sendEvent(HomeEvent.OpenEdit(action.quickAdd.presetId))
        }
    }
}
