package jpark.bro.subcal.feature.insight.impl.stats

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.insight.CategoryShare
import jpark.bro.subcal.core.domain.insight.PeriodTotal
import jpark.bro.subcal.core.model.Category
import java.time.YearMonth

/** product-spec 9장 샘플 (2026년 10월) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewStatsState(): StatsState {
    val totals = listOf(52_300L, 52_300, 61_200, 61_200, 61_200, 72_100, 61_590, 61_590, 61_590, 61_590, 61_590, 72_490)
    return StatsState(
        isLoading = false,
        month = YearMonth.of(2026, 10),
        categories =
            listOf(
                CategoryShare(Category.VIDEO, 42_800, 56),
                CategoryShare(Category.MUSIC, 21_800, 29),
                CategoryShare(Category.SHOPPING, 7_890, 10),
                CategoryShare(Category.CLOUD, 3_700, 5),
            ),
        monthlyAverageKrw = 76_190,
        activeCount = 7,
        periods =
            totals.mapIndexed { i, krw ->
                PeriodTotal(BillingPeriod(YearMonth.of(2025, 11).plusMonths(i.toLong())), krw)
            },
        thisPeriodKrw = 72_490,
        changeKrw = 10_900,
        annualKrw = 914_280,
        considerCancelCount = 2,
        considerCancelAnnualKrw = 261_600,
        savedKrw = 15_800,
    )
}
