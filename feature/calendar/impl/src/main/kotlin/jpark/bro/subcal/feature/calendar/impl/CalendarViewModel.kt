package jpark.bro.subcal.feature.calendar.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

internal class CalendarViewModel(
    subscriptionRepository: SubscriptionRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<CalendarState, CalendarAction, CalendarEvent>(CalendarState()) {
    private val today: LocalDate get() = LocalDate.now(clock)

    /** 보고 있는 달과 고른 날 */
    private val selection = MutableStateFlow(Selection(YearMonth.from(today), date = null))

    init {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeAppSettings(),
            selection,
        ) { records, settings, selection ->
            val state = buildCalendarState(records, settings.usdKrwRate, selection.month, selection.date, today)
            setState { state }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: CalendarAction) {
        when (action) {
            CalendarAction.PreviousMonthClicked -> {
                if (currentState.canGoPrevious) selection.update { Selection(it.month.minusMonths(1), date = null) }
            }

            CalendarAction.NextMonthClicked -> {
                if (currentState.canGoNext) selection.update { Selection(it.month.plusMonths(1), date = null) }
            }

            is CalendarAction.DayClicked -> {
                // 결제가 없는 날은 아무 일도 없다 (4-5)
                val hasPayments =
                    currentState.weeks
                        .flatten()
                        .any { it?.date == action.date && it.paymentCount > 0 }
                if (hasPayments) selection.update { it.copy(date = action.date) }
            }

            CalendarAction.SheetDismissed -> {
                selection.update { it.copy(date = null) }
            }

            is CalendarAction.PaymentClicked -> {
                selection.update { it.copy(date = null) }
                sendEvent(CalendarEvent.OpenDetail(action.subscriptionId))
            }
        }
    }

    private data class Selection(
        val month: YearMonth,
        val date: LocalDate?,
    )
}
