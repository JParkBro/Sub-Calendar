package jpark.bro.subcal.feature.home.impl

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

internal class HomeViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    /** 해지일을 정하기 위한 최근 구독 목록 */
    private var records: List<SubscriptionRecord> = emptyList()

    init {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
            settingsRepository.observeBillingDateHintPending(),
        ) { records, settings, hint ->
            this.records = records
            val state = buildHomeState(records, settings, LocalDate.now(clock), showBillingDateHint = hint)
            setState { state.copy(isWorking = isWorking) }
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

            HomeAction.YearReportBannerClicked -> {
                currentState.yearReportBanner?.let { sendEvent(HomeEvent.OpenYearReport(it.year)) }
            }

            HomeAction.BillingDateHintClicked -> {
                dismissBillingDateHint()
                sendEvent(HomeEvent.OpenList(category = null))
            }

            HomeAction.BillingDateHintDismissed -> {
                dismissBillingDateHint()
            }

            is HomeAction.Trial -> {
                onTrialAction(action)
            }
        }
    }

    private fun onTrialAction(action: HomeAction.Trial) {
        when (action) {
            HomeAction.TrialWarningClicked -> {
                sendEvent(HomeEvent.OpenTrial)
            }

            is HomeAction.UnresolvedKeepClicked -> {
                write { subscriptionRepository.keepTrial(action.subscriptionId) }
            }

            is HomeAction.UnresolvedCancelledClicked -> {
                val record = records.firstOrNull { it.subscription.id == action.subscriptionId } ?: return
                val cancelledOn = record.trialCancelDate(LocalDate.now(clock))
                write { subscriptionRepository.cancel(action.subscriptionId, cancelledOn) }
            }
        }
    }

    private fun write(block: suspend () -> Result<Unit>) {
        if (currentState.isWorking) return
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            block().onFailure { sendEvent(HomeEvent.ActionFailed) }
            setState { copy(isWorking = false) }
        }
    }

    /** 실패해도 다음 실행에 다시 보일 뿐이라 알리지 않는다 */
    private fun dismissBillingDateHint() {
        viewModelScope.launch { settingsRepository.setBillingDateHintPending(false) }
    }
}
