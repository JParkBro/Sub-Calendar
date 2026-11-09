package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Payment
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.TrialOutcome
import java.time.LocalDate

/**
 * 결과를 고르지 않은 체험인지 (product-spec 2-7). 합계에 넣지 않는다.
 * 첫 결제일이 지나도 고르지 않았으면 KEPT로 간주한다.
 */
fun Subscription.isPendingTrial(today: LocalDate): Boolean =
    trialOutcome == TrialOutcome.PENDING && !today.isAfter(firstBillingDate)

/** [date]에 유효한 금액: 그 날짜 이하인 가장 최근 변경, 없으면 기본 금액 (product-spec 2-5) */
fun SubscriptionRecord.priceOn(date: LocalDate): Money {
    val latest =
        priceChanges
            .filter { !it.effectiveFrom.isAfter(date) }
            .maxByOrNull { it.effectiveFrom }
    return latest?.let { subscription.amount.copy(amountMinor = it.amountMinor) } ?: subscription.amount
}

/** [from] 이상 [untilExclusive] 미만에 실제로 생기는 결제, 날짜순 (product-spec 2-3) */
fun SubscriptionRecord.payments(
    from: LocalDate,
    untilExclusive: LocalDate,
): List<Payment> =
    billingDates()
        .takeWhile { it.isBefore(untilExclusive) && !isCancelledBy(it) }
        .filter { !it.isBefore(from) && !isPausedOn(it) }
        .map(::paymentOn)
        .toList()

/** [today] 포함 다음 결제. 해지했거나 끝나지 않은 일시정지에 막혀 더 없으면 null */
fun SubscriptionRecord.nextPayment(today: LocalDate): Payment? {
    val openPauseStart = pauses.firstOrNull { it.endDate == null }?.startDate
    return billingDates()
        .takeWhile { !isCancelledBy(it) && (openPauseStart == null || it.isBefore(openPauseStart)) }
        .firstOrNull { !it.isBefore(today) && !isPausedOn(it) }
        ?.let(::paymentOn)
}

/** 항상 첫 결제일에서 계산해 말일 보정이 누적되지 않게 한다 (product-spec 2-2) */
private fun SubscriptionRecord.billingDates(): Sequence<LocalDate> =
    generateSequence(0L) { it + subscription.cycle.interval }.map { steps ->
        val first = subscription.firstBillingDate
        when (subscription.cycle.unit) {
            CycleUnit.DAY -> first.plusDays(steps)
            CycleUnit.WEEK -> first.plusWeeks(steps)
            CycleUnit.MONTH -> first.plusMonths(steps)
            CycleUnit.YEAR -> first.plusYears(steps)
        }
    }

private fun SubscriptionRecord.isCancelledBy(date: LocalDate): Boolean =
    subscription.cancelledOn?.let { !date.isBefore(it) } ?: false

private fun SubscriptionRecord.isPausedOn(date: LocalDate): Boolean =
    pauses.any { pause ->
        !date.isBefore(pause.startDate) && (pause.endDate == null || !date.isAfter(pause.endDate))
    }

private fun SubscriptionRecord.paymentOn(date: LocalDate): Payment = Payment(subscription, date, priceOn(date))
