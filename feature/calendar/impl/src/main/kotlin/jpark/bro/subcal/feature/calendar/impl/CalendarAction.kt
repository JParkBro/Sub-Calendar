package jpark.bro.subcal.feature.calendar.impl

import java.time.LocalDate

internal sealed interface CalendarAction {
    data object PreviousMonthClicked : CalendarAction

    data object NextMonthClicked : CalendarAction

    data class DayClicked(
        val date: LocalDate,
    ) : CalendarAction

    data object SheetDismissed : CalendarAction

    data class PaymentClicked(
        val subscriptionId: String,
    ) : CalendarAction
}
