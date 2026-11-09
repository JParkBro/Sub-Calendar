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
                moveMonth(-1)
            }

            CalendarAction.NextMonthClicked -> {
                moveMonth(1)
            }

            is CalendarAction.DayClicked -> {
                // 결제가 없는 날은 빌더가 시트를 만들지 않아 아무 일도 없다 (4-5). 보고 있는 달의 날만 받는다
                selection.update { if (YearMonth.from(action.date) == it.month) it.copy(date = action.date) else it }
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

    /** 화면 상태가 아니라 선택 값으로 범위를 확인해 빠르게 여러 번 눌러도 넘지 않는다 */
    private fun moveMonth(delta: Long) {
        val range = movableMonths(today)
        selection.update { current ->
            val target = current.month.plusMonths(delta)
            if (target in range) Selection(target, date = null) else current
        }
    }

    private data class Selection(
        val month: YearMonth,
        val date: LocalDate?,
    )
}
