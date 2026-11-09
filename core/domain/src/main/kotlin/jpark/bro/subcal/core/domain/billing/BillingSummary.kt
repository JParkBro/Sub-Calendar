package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Payment
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import java.time.LocalDate

private const val MONTHS_PER_YEAR = 12L
private const val WEEKS_PER_YEAR = 52L
private const val DAYS_PER_YEAR = 365L

/** 기간 안 결제의 원화 합. 결과를 고르지 않은 체험은 뺀다 (product-spec 2-8) */
fun periodTotalKrw(
    records: List<SubscriptionRecord>,
    period: BillingPeriod,
    today: LocalDate,
    usdKrwRate: Long,
): Long =
    records
        .filterNot { it.subscription.isPendingTrial(today) }
        .flatMap { it.payments(period.start, period.endExclusive) }
        .sumOf { it.amount.toKrw(usdKrwRate) }

/** 지난달 대비 증감 */
fun periodChangeKrw(
    records: List<SubscriptionRecord>,
    period: BillingPeriod,
    today: LocalDate,
    usdKrwRate: Long,
): Long =
    periodTotalKrw(records, period, today, usdKrwRate) - periodTotalKrw(records, period.previous(), today, usdKrwRate)

/** 구독 한 건의 한 달 환산액. [today]에 유효한 금액 기준, 원 단위 반올림 */
fun SubscriptionRecord.monthlyEquivalentKrw(
    today: LocalDate,
    usdKrwRate: Long,
): Long {
    val krw = priceOn(today).toKrw(usdKrwRate)
    val interval = subscription.cycle.interval.toLong()
    return when (subscription.cycle.unit) {
        CycleUnit.DAY -> roundedDiv(krw * DAYS_PER_YEAR, interval * MONTHS_PER_YEAR)
        CycleUnit.WEEK -> roundedDiv(krw * WEEKS_PER_YEAR, interval * MONTHS_PER_YEAR)
        CycleUnit.MONTH -> roundedDiv(krw, interval)
        CycleUnit.YEAR -> roundedDiv(krw, interval * MONTHS_PER_YEAR)
    }
}

/** 이용 중 구독의 한 달 환산액 합 */
fun monthlyAverageKrw(
    records: List<SubscriptionRecord>,
    today: LocalDate,
    usdKrwRate: Long,
): Long =
    records
        .filter { it.subscription.status == SubscriptionStatus.ACTIVE && !it.subscription.isPendingTrial(today) }
        .sumOf { it.monthlyEquivalentKrw(today, usdKrwRate) }

/** 연간 환산: 월 평균 × 12 */
fun annualizedKrw(
    records: List<SubscriptionRecord>,
    today: LocalDate,
    usdKrwRate: Long,
): Long = monthlyAverageKrw(records, today, usdKrwRate) * MONTHS_PER_YEAR

/** 첫 결제일부터 [today](포함)까지 생긴 결제의 합 */
fun SubscriptionRecord.cumulativeSpendKrw(
    today: LocalDate,
    usdKrwRate: Long,
): Long {
    if (subscription.isPendingTrial(today)) return 0
    return payments(subscription.firstBillingDate, today.plusDays(1)).sumOf { it.amount.toKrw(usdKrwRate) }
}

/** 구독별 다음 결제 중 가까운 [limit]개, 날짜 → 이름 순. 결과를 고르지 않은 체험은 뺀다 */
fun upcomingPayments(
    records: List<SubscriptionRecord>,
    today: LocalDate,
    limit: Int,
): List<Payment> =
    records
        .filterNot { it.subscription.isPendingTrial(today) }
        .mapNotNull { it.nextPayment(today) }
        .sortedWith(compareBy({ it.date }, { it.subscription.name }))
        .take(limit)
