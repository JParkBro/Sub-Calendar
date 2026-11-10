package jpark.bro.subcal.core.domain.insight

import jpark.bro.subcal.core.domain.billing.monthlyEquivalentKrw
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.domain.billing.toKrw
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.PresetCatalog
import jpark.bro.subcal.core.model.PresetPlan
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import java.time.LocalDate

private const val MONTHS_PER_YEAR = 12L

/** 중복으로 보는 카테고리 (product-spec 2-9). 쇼핑 · 생활 · 독서 · 게임 · 기타는 여러 개여도 중복이 아니다 */
val DuplicateCategories: Set<Category> = setOf(Category.VIDEO, Category.MUSIC, Category.CLOUD, Category.AI)

/** 같은 카테고리에 이용 중 구독이 2개 이상인 묶음 */
data class DuplicateGroup(
    val category: Category,
    /** 이름순 */
    val records: List<SubscriptionRecord>,
    /** 묶음의 한 달 환산 합 */
    val monthlyKrw: Long,
)

/** 해지한 구독 하나의 아낀 돈 */
data class SavedItem(
    val record: SubscriptionRecord,
    val savedKrw: Long,
)

/** 연간 요금제로 바꾸면 1년에 아끼는 돈: (월 금액 × 12) − 연간 금액 */
data class AnnualPlanOption(
    val record: SubscriptionRecord,
    val annualPlan: PresetPlan,
    val yearlySavingKrw: Long,
)

/** 중복 (product-spec 2-9): 이용 중 구독 중 같은 카테고리가 2개 이상. 한 달 환산 합이 큰 순 */
fun duplicateGroups(
    records: List<SubscriptionRecord>,
    today: LocalDate,
    usdKrwRate: Long,
): List<DuplicateGroup> =
    records
        .filter { it.isActiveSpending(today) && it.subscription.category in DuplicateCategories }
        .groupBy { it.subscription.category }
        .filterValues { it.size >= 2 }
        .map { (category, group) ->
            DuplicateGroup(
                category = category,
                records = group.sortedBy { it.subscription.name },
                monthlyKrw = group.sumOf { it.monthlyEquivalentKrw(today, usdKrwRate) },
            )
        }.sortedWith(compareByDescending<DuplicateGroup> { it.monthlyKrw }.thenBy { it.category.ordinal })

/** 해지한 구독별 아낀 돈, 큰 순 (product-spec 4-8 "아낀 돈" 탭) */
fun savedItems(
    records: List<SubscriptionRecord>,
    today: LocalDate,
    usdKrwRate: Long,
): List<SavedItem> =
    records
        .filter { it.subscription.status == SubscriptionStatus.CANCELLED }
        .map { SavedItem(it, it.savedKrw(today, usdKrwRate)) }
        .sortedWith(compareByDescending<SavedItem> { it.savedKrw }.thenBy { it.record.subscription.name })

/**
 * 연간 결제 비교 (product-spec 2-9): 매월 내는 이용 중 구독 중 프리셋에 같은 서비스의 연간 요금제가 있을 때만.
 * 요금제 이름이 같은 연간 요금제를 먼저 고르고, 없으면 연간 요금제가 하나뿐일 때만 그것과 비교한다
 * (다른 등급끼리 비교하지 않기 위해). 아끼는 돈이 0보다 클 때만, 큰 순.
 */
fun annualPlanOptions(
    records: List<SubscriptionRecord>,
    catalog: PresetCatalog,
    today: LocalDate,
    usdKrwRate: Long,
): List<AnnualPlanOption> {
    val presets = catalog.services.associateBy { it.id }
    return records
        .filter { it.isActiveSpending(today) && it.subscription.cycle == BillingCycle(CycleUnit.MONTH) }
        .mapNotNull { record ->
            val subscription = record.subscription
            val yearlyPlans =
                presets[subscription.presetId]
                    ?.plans
                    .orEmpty()
                    .filter {
                        it.cycle == BillingCycle(CycleUnit.YEAR) &&
                            it.amount.currency == subscription.amount.currency
                    }
            val plan =
                yearlyPlans.firstOrNull {
                    subscription.planName != null &&
                        it.name.startsWith(subscription.planName.orEmpty())
                }
                    ?: yearlyPlans.singleOrNull()
                    ?: return@mapNotNull null
            val saving = record.priceOn(today).toKrw(usdKrwRate) * MONTHS_PER_YEAR - plan.amount.toKrw(usdKrwRate)
            AnnualPlanOption(record, plan, saving).takeIf { saving > 0 }
        }.sortedByDescending { it.yearlySavingKrw }
}
