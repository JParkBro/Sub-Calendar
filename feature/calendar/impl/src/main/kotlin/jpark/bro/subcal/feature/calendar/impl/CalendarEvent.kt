package jpark.bro.subcal.feature.calendar.impl

internal sealed interface CalendarEvent {
    data class OpenDetail(
        val subscriptionId: String,
    ) : CalendarEvent
}
