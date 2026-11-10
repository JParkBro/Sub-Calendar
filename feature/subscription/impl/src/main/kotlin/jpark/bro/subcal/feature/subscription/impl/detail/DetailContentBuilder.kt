package jpark.bro.subcal.feature.subscription.impl.detail

import jpark.bro.subcal.core.domain.billing.PaymentHistoryEntry
import jpark.bro.subcal.core.domain.billing.cumulativeSpendKrw
import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.nextPayment
import jpark.bro.subcal.core.domain.billing.paymentHistory
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 상세 화면에 보여줄 값 (product-spec 4-4). 계산은 core:domain 규칙을 쓴다 */
internal fun buildDetailContent(
    record: SubscriptionRecord,
    today: LocalDate,
    usdKrwRate: Long,
): DetailContent {
    val subscription = record.subscription
    val status =
        when (subscription.status) {
            SubscriptionStatus.PAUSED -> {
                DetailStatus.PAUSED
            }

            SubscriptionStatus.CANCELLED -> {
                DetailStatus.CANCELLED
            }

            SubscriptionStatus.ACTIVE -> {
                if (subscription.isPendingTrial(
                        today,
                    )
                ) {
                    DetailStatus.TRIAL
                } else {
                    DetailStatus.ACTIVE
                }
            }
        }
    val history = record.paymentHistory(today)
    val paid = history.filterIsInstance<PaymentHistoryEntry.Paid>()
    // 해지 · 일시정지면 다음 결제를 숨긴다
    val next =
        if (status == DetailStatus.ACTIVE || status == DetailStatus.TRIAL) {
            record.nextPayment(today)?.let { NextPayment(it.date, ChronoUnit.DAYS.between(today, it.date).toInt()) }
        } else {
            null
        }
    return DetailContent(
        id = subscription.id,
        name = subscription.name,
        planName = subscription.planName,
        colorKey = subscription.colorKey,
        cycle = subscription.cycle,
        firstBillingDate = subscription.firstBillingDate,
        paymentMethod = subscription.paymentMethod,
        status = status,
        amount = record.priceOn(today),
        cumulativeKrw = record.cumulativeSpendKrw(today, usdKrwRate),
        paymentCount = paid.size,
        nextPayment = next,
        recentHistory = recentHistory(history, paid),
        hasMoreHistory = paid.size > RECENT_PAYMENTS,
        memo = subscription.memo,
        hasCancelUrl = !subscription.cancelUrl.isNullOrBlank(),
        considerCancel = subscription.considerCancel,
    )
}

/** 최근 결제 [RECENT_PAYMENTS]회까지. 그보다 오래된 일시정지 구간은 빼고 자른다 */
private fun recentHistory(
    history: List<PaymentHistoryEntry>,
    paid: List<PaymentHistoryEntry.Paid>,
): List<PaymentHistoryEntry> {
    if (paid.size <= RECENT_PAYMENTS) return history
    val oldestShown = paid[RECENT_PAYMENTS - 1]
    return history.take(history.indexOf(oldestShown) + 1)
}
