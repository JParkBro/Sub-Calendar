package jpark.bro.subcal.feature.home.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class HomeViewModel(
    subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    init {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
            settingsRepository.observeBillingDateHintPending(),
        ) { records, settings, hint ->
            val state = buildHomeState(records, settings, LocalDate.now(clock), showBillingDateHint = hint)
            setState { state }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.UpcomingClicked -> {
                sendEvent(HomeEvent.OpenDetail(action.subscriptionId))
            }

            HomeAction.SeeAllClicked -> {
                sendEvent(HomeEvent.OpenCalendar)
            }

            is HomeAction.CategoryClicked -> {
                sendEvent(HomeEvent.OpenList(action.category))
            }

            HomeAction.AddClicked -> {
                sendEvent(HomeEvent.OpenServicePicker)
            }

            is HomeAction.QuickAddClicked -> {
                sendEvent(HomeEvent.OpenEdit(action.quickAdd.presetId))
            }

            HomeAction.BillingDateHintClicked -> {
                dismissBillingDateHint()
                sendEvent(HomeEvent.OpenList(category = null))
            }

            HomeAction.BillingDateHintDismissed -> {
                dismissBillingDateHint()
            }
        }
    }

    /** 실패해도 다음 실행에 다시 보일 뿐이라 알리지 않는다 */
    private fun dismissBillingDateHint() {
        viewModelScope.launch { settingsRepository.setBillingDateHintPending(false) }
    }
}
