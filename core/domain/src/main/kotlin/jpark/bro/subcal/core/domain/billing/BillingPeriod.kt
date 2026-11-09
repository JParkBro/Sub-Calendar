package jpark.bro.subcal.core.domain.billing

import java.time.LocalDate
import java.time.YearMonth

/** "이번 달" 기간: [month]의 [startDay]일부터 다음 달 [startDay]일 전까지 (product-spec 2-8). 이름은 시작일이 속한 달 */
data class BillingPeriod(
    val month: YearMonth,
    val startDay: Int = MIN_START_DAY,
) {
    init {
        require(startDay in MIN_START_DAY..MAX_START_DAY) { "startDay must be in $MIN_START_DAY..$MAX_START_DAY" }
    }

    val start: LocalDate get() = month.atDay(startDay)

    val endExclusive: LocalDate get() = month.plusMonths(1).atDay(startDay)

    fun previous(): BillingPeriod = copy(month = month.minusMonths(1))

    companion object {
        private const val MIN_START_DAY = 1
        private const val MAX_START_DAY = 28

        /** [date]가 들어 있는 기간 */
        fun containing(
            date: LocalDate,
            startDay: Int,
        ): BillingPeriod {
            val month = YearMonth.from(date)
            return BillingPeriod(if (date.dayOfMonth >= startDay) month else month.minusMonths(1), startDay)
        }
    }
}
