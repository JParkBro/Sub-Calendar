package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.Payment
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate

/** 구독 상세 · 결제 이력의 한 줄 (product-spec 4-4) */
sealed interface PaymentHistoryEntry {
    /** 최신순 정렬 기준 */
    val date: LocalDate

    data class Paid(
        val payment: Payment,
        /** 직전 회차 대비. 첫 회차는 SAME */
        val trend: PriceTrend,
    ) : PaymentHistoryEntry {
        override val date: LocalDate get() = payment.date
    }

    /** 일시정지 구간. [end]가 null이면 아직 일시정지 중 */
    data class Paused(
        val start: LocalDate,
        val end: LocalDate?,
    ) : PaymentHistoryEntry {
        override val date: LocalDate get() = start
    }
}

/** 직전 회차보다 오르면 인상, 내리면 인하 (product-spec 2-5) */
enum class PriceTrend {
    SAME,
    UP,
    DOWN,
}

/**
 * 첫 결제일부터 [today]까지(오늘 포함) 생긴 결제와 시작한 일시정지 구간, 최신순.
 * 결과를 고르지 않은 체험은 아직 결제가 없어 비어 있다 (2-7).
 */
fun SubscriptionRecord.paymentHistory(today: LocalDate): List<PaymentHistoryEntry> {
    if (subscription.isPendingTrial(today)) return emptyList()
    val payments = payments(subscription.firstBillingDate, today.plusDays(1))
    val paid =
        payments.mapIndexed { index, payment ->
            val previous = payments.getOrNull(index - 1)?.amount?.amountMinor
            val current = payment.amount.amountMinor
            val trend =
                when {
                    previous == null || previous == current -> PriceTrend.SAME
                    current > previous -> PriceTrend.UP
                    else -> PriceTrend.DOWN
                }
            PaymentHistoryEntry.Paid(payment, trend)
        }
    val paused =
        pauses
            .filter { !it.startDate.isAfter(today) }
            .map { PaymentHistoryEntry.Paused(it.startDate, it.endDate) }
    return (paid + paused).sortedByDescending { it.date }
}
