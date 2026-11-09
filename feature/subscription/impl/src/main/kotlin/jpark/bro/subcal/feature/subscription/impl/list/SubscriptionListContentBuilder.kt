package jpark.bro.subcal.feature.subscription.impl.list

import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.monthlyEquivalentKrw
import jpark.bro.subcal.core.domain.billing.nextPayment
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import java.text.Collator
import java.time.LocalDate
import java.util.Locale

/** 목록 화면에 보여줄 칩 · 합계 · 묶음 (product-spec 4-2) */
internal fun buildListContent(
    records: List<SubscriptionRecord>,
    filter: ListFilter,
    sort: ListSort,
    today: LocalDate,
    usdKrwRate: Long,
): SubscriptionListContent {
    val inCategory = records.filter { filter.category == null || it.subscription.category == filter.category }
    val byFilter = inCategory.groupBy { it.statusFilter(today) }
    val visible = byFilter[filter.status].orEmpty()
    val monthly = visible.associate { it.subscription.id to it.monthlyEquivalentKrw(today, usdKrwRate) }

    val sorted =
        when (sort) {
            ListSort.NEXT_BILLING -> {
                visible.sortedWith(
                    compareBy<SubscriptionRecord, LocalDate?>(nullsLast()) { it.nextPayment(today)?.date }
                        .then(byName),
                )
            }

            ListSort.AMOUNT -> {
                visible.sortedWith(
                    compareByDescending<SubscriptionRecord> { monthly.getValue(it.subscription.id) }.then(byName),
                )
            }

            ListSort.NAME -> {
                visible.sortedWith(byName)
            }
        }
    val rows = sorted.map { it.toRow(today, monthly.getValue(it.subscription.id)) }
    val sections =
        when {
            rows.isEmpty() -> {
                emptyList()
            }

            // 카테고리 묶음은 결제일순에서만
            sort == ListSort.NEXT_BILLING -> {
                sorted
                    .zip(rows)
                    .groupBy({ (record, _) -> record.subscription.category }, { (_, row) -> row })
                    .toSortedMap()
                    .map { (category, rows) -> SubscriptionSection(category, rows) }
            }

            else -> {
                listOf(SubscriptionSection(category = null, rows = rows))
            }
        }

    return SubscriptionListContent(
        filters =
            StatusFilter.entries
                .map { StatusFilterChip(it, byFilter[it].orEmpty().size) }
                .filter { it.filter == StatusFilter.ACTIVE || it.filter == filter.status || it.count > 0 },
        monthlyTotalKrw = monthly.values.sum(),
        sections = sections,
    )
}

internal fun SubscriptionRecord.statusFilter(today: LocalDate): StatusFilter =
    when (subscription.status) {
        SubscriptionStatus.PAUSED -> StatusFilter.PAUSED
        SubscriptionStatus.CANCELLED -> StatusFilter.CANCELLED
        SubscriptionStatus.ACTIVE -> if (subscription.isPendingTrial(today)) StatusFilter.TRIAL else StatusFilter.ACTIVE
    }

private val koreanCollator: Collator = Collator.getInstance(Locale.KOREAN)

private val byName: Comparator<SubscriptionRecord> =
    compareBy<SubscriptionRecord, String>(koreanCollator) { it.subscription.name }

private fun SubscriptionRecord.toRow(
    today: LocalDate,
    monthlyKrw: Long,
): SubscriptionRow {
    val cycle = subscription.cycle
    val isMonthlyOrWeekly = cycle.interval == 1 && (cycle.unit == CycleUnit.MONTH || cycle.unit == CycleUnit.WEEK)
    return SubscriptionRow(
        id = subscription.id,
        name = subscription.name,
        planName = subscription.planName,
        colorKey = subscription.colorKey,
        cycle = cycle,
        firstBillingDate = subscription.firstBillingDate,
        amount = priceOn(today),
        monthlyKrw = if (isMonthlyOrWeekly) null else monthlyKrw,
        isInactive = subscription.status != SubscriptionStatus.ACTIVE,
    )
}
