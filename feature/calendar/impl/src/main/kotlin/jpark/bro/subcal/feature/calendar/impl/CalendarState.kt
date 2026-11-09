package jpark.bro.subcal.feature.calendar.impl

import jpark.bro.subcal.core.model.Payment
import java.time.LocalDate
import java.time.YearMonth

internal data class CalendarState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.from(LocalDate.ofEpochDay(0)),
    val canGoPrevious: Boolean = false,
    val canGoNext: Boolean = false,
    /** 달력 월 기준 결제 건수 · 원화 합 (월 시작 기준일과 무관) */
    val paymentCount: Int = 0,
    val totalKrw: Long = 0,
    /** 일요일 시작 주 5~6개. 다른 달 칸은 null */
    val weeks: List<List<CalendarDay?>> = emptyList(),
    /** 결제가 있는 날을 누르면 바텀시트 */
    val selected: SelectedDay? = null,
)

internal data class CalendarDay(
    val date: LocalDate,
    /** 보여줄 서비스 아이콘 (최대 [MAX_DAY_ICONS]개) */
    val icons: List<DayIcon>,
    val paymentCount: Int,
    /** 결과를 고르기 전인 체험의 종료일 (점선 원) */
    val hasTrialEnd: Boolean,
    val isToday: Boolean,
) {
    /** 아이콘 뒤 "+n" */
    val moreCount: Int get() = paymentCount - icons.size
}

internal data class DayIcon(
    val name: String,
    val colorKey: String,
)

internal data class SelectedDay(
    val date: LocalDate,
    val payments: List<DayPayment>,
    val totalKrw: Long,
)

internal data class DayPayment(
    val payment: Payment,
    val daysUntil: Int,
)

internal const val MAX_DAY_ICONS = 2

/** 이동할 수 있는 범위 (product-spec 4-5) */
internal const val PAST_MONTHS = 24L
internal const val FUTURE_MONTHS = 12L
