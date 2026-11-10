package jpark.bro.subcal.core.domain.insight

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.monthlyEquivalentKrw
import jpark.bro.subcal.core.domain.billing.payments
import jpark.bro.subcal.core.domain.billing.periodTotalKrw
import jpark.bro.subcal.core.domain.billing.toKrw
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Payment
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import java.time.LocalDate

private const val MONTHS_PER_YEAR = 12L
private const val PERCENT = 100

/** 카테고리 하나의 한 달 환산액과 비율(정수 %, 합 100) */
data class CategoryShare(
    val category: Category,
    val monthlyKrw: Long,
    val percent: Int,
)

/** 기간 하나의 실제 결제 합 */
data class PeriodTotal(
    val period: BillingPeriod,
    val totalKrw: Long,
)

/**
 * 카테고리 비중 (product-spec 4-6): 이용 중 구독(체험 결과 대기 제외)의 한 달 환산액을 카테고리별로 더한다.
 * 큰 순서, 금액이 같으면 카테고리 순서. 0원인 카테고리는 뺀다.
 * 비율은 최대 나머지 방식으로 반올림해 합이 정확히 100이 되게 한다.
 */
fun categoryShares(
    records: List<SubscriptionRecord>,
    today: LocalDate,
    usdKrwRate: Long,
): List<CategoryShare> {
    val byCategory =
        records
            .filter { it.isActiveSpending(today) }
            .groupBy { it.subscription.category }
            .mapValues { (_, list) -> list.sumOf { it.monthlyEquivalentKrw(today, usdKrwRate) } }
            .filterValues { it > 0 }
            .toList()
            .sortedWith(compareByDescending<Pair<Category, Long>> { it.second }.thenBy { it.first.ordinal })
    val total = byCategory.sumOf { it.second }
    if (total == 0L) return emptyList()

    val exact = byCategory.map { (_, krw) -> krw * PERCENT }
    val floors = exact.map { (it / total).toInt() }
    val remaining = PERCENT - floors.sum()
    // 나머지가 큰 항목부터 1%씩 더한다 (같으면 앞 항목)
    val bonus =
        exact.indices
            .sortedWith(compareByDescending<Int> { exact[it] % total }.thenBy { it })
            .take(remaining)
            .toSet()
    return byCategory.mapIndexed { index, (category, krw) ->
        CategoryShare(category, krw, floors[index] + if (index in bonus) 1 else 0)
    }
}

/** [current]까지 최근 [count]개 기간의 실제 결제 합, 오래된 순 (product-spec 4-6 "12개월") */
fun recentPeriodTotals(
    records: List<SubscriptionRecord>,
    current: BillingPeriod,
    today: LocalDate,
    usdKrwRate: Long,
    count: Int,
): List<PeriodTotal> =
    generateSequence(current) { it.previous() }
        .take(count)
        .map { PeriodTotal(it, periodTotalKrw(records, it, today, usdKrwRate)) }
        .toList()
        .reversed()

/**
 * 구독 하나의 아낀 돈 (product-spec 2-9): 해지하지 않았다면 해지일부터 오늘(포함)까지 생겼을 결제의 합.
 * 체험 중 해지는 첫 결제일부터 센다(첫 결제일 전에는 결제가 없으므로 같은 계산). 일시정지 · 가격 변경 반영.
 * 해지하지 않은 구독은 0.
 */
fun SubscriptionRecord.savedKrw(
    today: LocalDate,
    usdKrwRate: Long,
): Long = avoidedPayments(LocalDate.MIN, today.plusDays(1)).sumOf { it.amount.toKrw(usdKrwRate) }

/**
 * 해지해서 내지 않은 결제: 해지하지 않았다면 해지일부터 생겼을 결제 중 [from] 이상 [untilExclusive] 미만.
 * 해지하지 않은 구독은 빈 목록.
 */
internal fun SubscriptionRecord.avoidedPayments(
    from: LocalDate,
    untilExclusive: LocalDate,
): List<Payment> {
    val cancelledOn = subscription.cancelledOn
    if (subscription.status != SubscriptionStatus.CANCELLED || cancelledOn == null) return emptyList()
    val notCancelled = copy(subscription = subscription.copy(status = SubscriptionStatus.ACTIVE, cancelledOn = null))
    return notCancelled.payments(maxOf(cancelledOn, from), untilExclusive)
}

/** 지금까지 아낀 돈: 해지한 구독 전체의 합 (삭제한 구독은 Repository가 빼고 준다) */
fun totalSavedKrw(
    records: List<SubscriptionRecord>,
    today: LocalDate,
    usdKrwRate: Long,
): Long = records.sumOf { it.savedKrw(today, usdKrwRate) }

/** 해지 고민함에 담은 이용 중 구독 */
fun considerCancelRecords(
    records: List<SubscriptionRecord>,
    today: LocalDate,
): List<SubscriptionRecord> = records.filter { it.subscription.considerCancel && it.isActiveSpending(today) }

/** "해지하면 연 n원" (product-spec 2-9): 해지 고민함 구독의 연간 환산 합 */
fun considerCancelAnnualKrw(
    records: List<SubscriptionRecord>,
    today: LocalDate,
    usdKrwRate: Long,
): Long = considerCancelRecords(records, today).sumOf { it.monthlyEquivalentKrw(today, usdKrwRate) } * MONTHS_PER_YEAR

/** 월 평균 · 비중에 넣는 구독: 이용 중이고 체험 결과를 기다리지 않는 것 */
internal fun SubscriptionRecord.isActiveSpending(today: LocalDate): Boolean =
    subscription.status == SubscriptionStatus.ACTIVE && !subscription.isPendingTrial(today)
