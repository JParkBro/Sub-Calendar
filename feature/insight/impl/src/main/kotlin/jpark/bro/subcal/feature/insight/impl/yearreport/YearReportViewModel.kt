package jpark.bro.subcal.feature.insight.impl.yearreport

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.ui.MviViewModel
import jpark.bro.subcal.feature.insight.api.YearReportKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import java.time.Clock
import java.time.LocalDate

internal class YearReportViewModel(
    key: YearReportKey,
    subscriptionRepository: SubscriptionRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<YearReportState, YearReportAction, YearReportEvent>(YearReportState()) {
    private val year = MutableStateFlow(key.year)

    init {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
            year,
        ) { records, settings, year ->
            val built = buildYearReportState(records, settings, year, LocalDate.now(clock))
            setState { built.copy(page = page) }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: YearReportAction) {
        when (action) {
            YearReportAction.BackClicked -> {
                sendEvent(YearReportEvent.NavigateBack)
            }

            is YearReportAction.YearSelected -> {
                if (action.year == year.value) return
                setState { copy(page = YearReportPage.TOTAL) }
                year.value = action.year
            }

            is YearReportAction.PageChanged -> {
                setState { copy(page = action.page) }
            }

            YearReportAction.ShareClicked -> {
                if (currentState.content != null) sendEvent(YearReportEvent.ShareImage)
            }
        }
    }
}
