package jpark.bro.subcal.feature.insight.impl.yearreport

import jpark.bro.subcal.core.domain.insight.TopCategory
import jpark.bro.subcal.core.model.Category
import java.time.YearMonth

/** product-spec 9장 샘플의 2026년 리포트 (내 몫 기준, 12월까지 예정 결제 포함) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewYearReportContent(): YearReportContent {
    val monthly =
        listOf(
            36_015L,
            36_015L,
            46_915L,
            46_915L,
            46_915L,
            46_915L,
            46_915L,
            50_415L,
            50_415L,
            61_315L,
            105_715L,
            61_315L,
        )
    return YearReportContent(
        totalKrw = monthly.sum(),
        monthlyAverageKrw = 52_982,
        monthlyKrw = monthly,
        topCategory = TopCategory(Category.VIDEO, totalKrw = 333_200, percent = 52, subscriptionCount = 3),
        longest = LongestItem("구글 원 200GB", "BLUE", since = YearMonth.of(2023, 11), yearCount = 4),
        savedKrw = 39_500,
        savedCount = 1,
    )
}

@Suppress("MagicNumber") // 샘플 데이터
internal fun previewYearReportState(): YearReportState =
    YearReportState(
        isLoading = false,
        year = 2026,
        years = listOf(2025, 2026),
        content = previewYearReportContent(),
    )
