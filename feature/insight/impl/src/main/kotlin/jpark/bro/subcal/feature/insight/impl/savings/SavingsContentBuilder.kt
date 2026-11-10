package jpark.bro.subcal.feature.insight.impl.savings

import jpark.bro.subcal.core.domain.billing.monthlyEquivalentKrw
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.domain.insight.annualPlanOptions
import jpark.bro.subcal.core.domain.insight.considerCancelAnnualKrw
import jpark.bro.subcal.core.domain.insight.considerCancelRecords
import jpark.bro.subcal.core.domain.insight.duplicateGroups
import jpark.bro.subcal.core.domain.insight.savedItems
import jpark.bro.subcal.core.domain.insight.totalSavedKrw
import jpark.bro.subcal.core.model.PresetCatalog
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.feature.insight.api.SavingsTab
import java.time.LocalDate

/** 저장된 구독에서 아낀 돈 · 해지 고민 화면 값을 만든다. 계산은 모두 core:domain */
internal fun buildSavingsState(
    tab: SavingsTab,
    records: List<SubscriptionRecord>,
    catalog: PresetCatalog?,
    usdKrwRate: Long,
    today: LocalDate,
): SavingsState {
    val duplicates = duplicateGroups(records, today, usdKrwRate)
    val duplicateNames = duplicates.associate { group -> group.category to group.records.map { it.subscription.name } }
    return SavingsState(
        isLoading = false,
        tab = tab,
        considerItems =
            considerCancelRecords(records, today)
                .map { record ->
                    val subscription = record.subscription
                    ConsiderItem(
                        id = subscription.id,
                        name = subscription.name,
                        colorKey = subscription.colorKey,
                        monthlyKrw = record.monthlyEquivalentKrw(today, usdKrwRate),
                        duplicateWith = duplicateNames[subscription.category].orEmpty() - subscription.name,
                    )
                }.sortedWith(compareByDescending<ConsiderItem> { it.monthlyKrw }.thenBy { it.name }),
        considerAnnualKrw = considerCancelAnnualKrw(records, today, usdKrwRate),
        duplicates = duplicates.map { DuplicateItem(it.category, duplicateNames.getValue(it.category), it.monthlyKrw) },
        annualOptions =
            catalog
                ?.let { annualPlanOptions(records, it, today, usdKrwRate) }
                .orEmpty()
                .map {
                    AnnualOptionItem(
                        id = it.record.subscription.id,
                        name = it.record.subscription.name,
                        monthlyAmount = it.record.priceOn(today),
                        annualAmount = it.annualPlan.amount,
                        yearlySavingKrw = it.yearlySavingKrw,
                    )
                },
        savedTotalKrw = totalSavedKrw(records, today, usdKrwRate),
        savedItems =
            savedItems(records, today, usdKrwRate).mapNotNull { item ->
                val subscription = item.record.subscription
                val cancelledOn = subscription.cancelledOn ?: return@mapNotNull null
                SavedListItem(subscription.id, subscription.name, subscription.colorKey, cancelledOn, item.savedKrw)
            },
    )
}
