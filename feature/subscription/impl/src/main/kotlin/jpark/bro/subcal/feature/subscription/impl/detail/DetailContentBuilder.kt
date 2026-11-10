package jpark.bro.subcal.feature.subscription.impl.detail

import jpark.bro.subcal.core.domain.billing.PaymentHistoryEntry
import jpark.bro.subcal.core.domain.billing.cumulativeSpendKrw
import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.nextPayment
import jpark.bro.subcal.core.domain.billing.paymentHistory
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.domain.insight.usageValue
import jpark.bro.subcal.core.domain.party.hasParty
import jpark.bro.subcal.core.domain.party.myShare
import jpark.bro.subcal.core.domain.party.partySize
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.UsageLog
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 상세 화면에 보여줄 값 (product-spec 4-4). 계산은 core:domain 규칙을 쓴다 */
internal fun buildDetailContent(
    record: SubscriptionRecord,
    today: LocalDate,
    settings: AppSettings,
    usageLogs: List<UsageLog> = emptyList(),
): DetailContent {
    val usdKrwRate = settings.usdKrwRate
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
        party = record.detailParty(status, today),
        usage = record.detailUsage(status, usageLogs, today, settings),
    )
}

/** 파티가 있거나 이용 중이면 파티 행 (멤버가 없으면 추가 안내) */
private fun SubscriptionRecord.detailParty(
    status: DetailStatus,
    today: LocalDate,
): DetailParty? =
    if (hasParty || status == DetailStatus.ACTIVE) DetailParty(partySize, myShare(priceOn(today))) else null

/** 가성비 분석과 같은 대상: 체험 결과를 기다리거나 일시정지 · 해지한 구독은 1회당 비용이 의미 없다 */
private fun SubscriptionRecord.detailUsage(
    status: DetailStatus,
    usageLogs: List<UsageLog>,
    today: LocalDate,
    settings: AppSettings,
): DetailUsage? =
    if (subscription.trackUsage && status == DetailStatus.ACTIVE) {
        usageValue(usageLogs, today, settings.monthStartDay, settings.usdKrwRate).let {
            DetailUsage(it.useCount, it.costPerUseKrw)
        }
    } else {
        null
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
