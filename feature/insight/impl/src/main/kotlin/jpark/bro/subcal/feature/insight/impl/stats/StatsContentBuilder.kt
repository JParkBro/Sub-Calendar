package jpark.bro.subcal.feature.insight.impl.stats

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.annualizedKrw
import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.insight.categoryShares
import jpark.bro.subcal.core.domain.insight.considerCancelAnnualKrw
import jpark.bro.subcal.core.domain.insight.considerCancelRecords
import jpark.bro.subcal.core.domain.insight.recentPeriodTotals
import jpark.bro.subcal.core.domain.insight.totalSavedKrw
import jpark.bro.subcal.core.domain.insight.valueAnalysis
import jpark.bro.subcal.core.domain.party.hasParty
import jpark.bro.subcal.core.domain.party.toMyShare
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.UsageLog
import java.time.LocalDate

/**
 * 저장된 구독 · 설정에서 통계 화면 값을 만든다. 계산은 모두 core:domain.
 * 비중 · 12개월 · 연간은 내 부담액(파티 구독은 내 몫) 기준, 해지 고민 · 아낀 돈 카드는 이동할 화면과 같은 전체 금액 기준.
 */
internal fun buildStatsState(
    records: List<SubscriptionRecord>,
    settings: AppSettings,
    today: LocalDate,
    usageLogs: List<UsageLog> = emptyList(),
): StatsState {
    val values = valueAnalysis(records, usageLogs, today, settings.monthStartDay, settings.usdKrwRate)
    val rate = settings.usdKrwRate
    val current = BillingPeriod.containing(today, settings.monthStartDay)
    val mine = records.map(SubscriptionRecord::toMyShare)
    val categories = categoryShares(mine, today, rate)
    val periods = recentPeriodTotals(mine, current, today, rate, RECENT_PERIOD_COUNT)
    val thisPeriod = periods.last().totalKrw
    val previous = periods.getOrNull(periods.lastIndex - 1)?.totalKrw ?: 0
    return StatsState(
        isLoading = false,
        month = current.month,
        periodStart = current.start.takeIf { settings.monthStartDay != 1 },
        periodEndInclusive = current.endExclusive.minusDays(1).takeIf { settings.monthStartDay != 1 },
        categories = categories,
        monthlyAverageKrw = categories.sumOf { it.monthlyKrw },
        activeCount =
            records.count {
                it.subscription.status == SubscriptionStatus.ACTIVE && !it.subscription.isPendingTrial(today)
            },
        periods = periods,
        thisPeriodKrw = thisPeriod,
        changeKrw = thisPeriod - previous,
        annualKrw = annualizedKrw(mine, today, rate),
        considerCancelCount = considerCancelRecords(records, today).size,
        considerCancelAnnualKrw = considerCancelAnnualKrw(records, today, rate),
        savedKrw = totalSavedKrw(records, today, rate),
        hasParty = records.any { it.hasParty && it.subscription.status == SubscriptionStatus.ACTIVE },
        valueTrackedCount = values.size,
        valueUnusedCount = values.count { it.isUnused },
    )
}
