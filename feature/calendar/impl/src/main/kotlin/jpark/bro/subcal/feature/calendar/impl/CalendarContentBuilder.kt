package jpark.bro.subcal.feature.calendar.impl

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.periodPayments
import jpark.bro.subcal.core.domain.billing.toKrw
import jpark.bro.subcal.core.model.Payment
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private const val DAYS_PER_WEEK = 7

/** 캘린더에 보여줄 한 달 (product-spec 4-5). 결제는 core:domain 규칙으로 만든다 */
internal fun buildCalendarState(
    records: List<SubscriptionRecord>,
    usdKrwRate: Long,
    month: YearMonth,
    selectedDate: LocalDate?,
    today: LocalDate,
): CalendarState {
    val payments = periodPayments(records, BillingPeriod(month), today)
    val byDate = payments.groupBy { it.date }
    val trialEnds =
        records
            .filter {
                it.subscription.isPendingTrial(
                    today,
                ) && YearMonth.from(it.subscription.firstBillingDate) == month
            }.mapTo(mutableSetOf()) { it.subscription.firstBillingDate }
    val current = YearMonth.from(today)
    return CalendarState(
        isLoading = false,
        month = month,
        canGoPrevious = month > current.minusMonths(PAST_MONTHS),
        canGoNext = month < current.plusMonths(FUTURE_MONTHS),
        paymentCount = payments.size,
        totalKrw = payments.sumOf { it.amount.toKrw(usdKrwRate) },
        weeks = weeks(month) { date -> day(date, byDate[date].orEmpty(), date in trialEnds, today) },
        selected =
            selectedDate?.let { date ->
                byDate[date]?.let { dayPayments ->
                    SelectedDay(
                        date = date,
                        payments = dayPayments.map { DayPayment(it, ChronoUnit.DAYS.between(today, date).toInt()) },
                        totalKrw = dayPayments.sumOf { it.amount.toKrw(usdKrwRate) },
                    )
                }
            },
    )
}

/** 일요일 시작. 첫 주 앞 · 마지막 주 뒤의 다른 달 칸은 null */
private fun weeks(
    month: YearMonth,
    day: (LocalDate) -> CalendarDay,
): List<List<CalendarDay?>> {
    // 일요일 = 0
    val leading = month.atDay(1).dayOfWeek.value % DAYS_PER_WEEK
    val cells = List<CalendarDay?>(leading) { null } + (1..month.lengthOfMonth()).map { day(month.atDay(it)) }
    val trailing = (DAYS_PER_WEEK - cells.size % DAYS_PER_WEEK) % DAYS_PER_WEEK
    return (cells + List(trailing) { null }).chunked(DAYS_PER_WEEK)
}

private fun day(
    date: LocalDate,
    payments: List<Payment>,
    hasTrialEnd: Boolean,
    today: LocalDate,
): CalendarDay =
    CalendarDay(
        date = date,
        icons = payments.take(MAX_DAY_ICONS).map { DayIcon(it.subscription.name, it.subscription.colorKey) },
        paymentCount = payments.size,
        hasTrialEnd = hasTrialEnd,
        isToday = date == today,
    )

/** 그리드 머리글 순서 */
internal val WeekHeader: List<DayOfWeek> =
    listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.filter { it != DayOfWeek.SUNDAY }
