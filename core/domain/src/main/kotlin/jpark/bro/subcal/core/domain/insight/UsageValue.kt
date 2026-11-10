package jpark.bro.subcal.core.domain.insight

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.monthlyEquivalentKrw
import jpark.bro.subcal.core.domain.billing.roundedDiv
import jpark.bro.subcal.core.domain.party.toMyShare
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.UsageLog
import java.time.LocalDate

/** 사용 체크 구독 한 건의 이번 달 가성비 (product-spec 4-10) */
data class UsageValue(
    val record: SubscriptionRecord,
    /** 이번 달 기간 사용 횟수 */
    val useCount: Int,
    /** 이번 달 내 부담액(월 환산) */
    val monthlyKrw: Long,
) {
    /** 이번 달에 한 번도 쓰지 않았다 ("사용 안 함") */
    val isUnused: Boolean get() = useCount == 0

    /** 1회당 비용 = 월 환산 ÷ 사용 횟수, 원 단위 반올림. [isUnused]면 null */
    val costPerUseKrw: Long? get() = if (isUnused) null else roundedDiv(monthlyKrw, useCount.toLong())
}

/** [period] 안에 [subscriptionId]를 쓴 횟수 */
fun usageCount(
    subscriptionId: String,
    usageLogs: List<UsageLog>,
    period: BillingPeriod,
): Int =
    usageLogs.count {
        it.subscriptionId == subscriptionId && !it.usedOn.isBefore(period.start) &&
            it.usedOn.isBefore(period.endExclusive)
    }

/**
 * 구독 한 건의 이번 달 가성비. 구독 상세의 사용 체크 카드에 쓴다 (product-spec 4-4).
 * 내 부담액은 내 몫의 월 환산이다 (파티가 아니면 전체).
 */
fun SubscriptionRecord.usageValue(
    usageLogs: List<UsageLog>,
    today: LocalDate,
    monthStartDay: Int,
    usdKrwRate: Long,
): UsageValue =
    UsageValue(
        record = this,
        useCount = usageCount(subscription.id, usageLogs, BillingPeriod.containing(today, monthStartDay)),
        monthlyKrw = toMyShare().monthlyEquivalentKrw(today, usdKrwRate),
    )

/**
 * 가성비 분석 (product-spec 4-10): 사용 체크를 켠 이용 중 구독.
 * 사용 안 함(월 환산 큰 순) → 1회당 비용 큰 순, 같으면 이름순.
 */
fun valueAnalysis(
    records: List<SubscriptionRecord>,
    usageLogs: List<UsageLog>,
    today: LocalDate,
    monthStartDay: Int,
    usdKrwRate: Long,
): List<UsageValue> =
    records
        .filter { it.subscription.trackUsage && it.isActiveSpending(today) }
        .map { it.usageValue(usageLogs, today, monthStartDay, usdKrwRate) }
        .sortedWith(
            compareByDescending<UsageValue> { it.isUnused }
                .thenByDescending { it.costPerUseKrw ?: it.monthlyKrw }
                .thenBy { it.record.subscription.name },
        )
