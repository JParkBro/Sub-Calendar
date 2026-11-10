package jpark.bro.subcal.feature.insight.impl.yearreport

import jpark.bro.subcal.core.domain.insight.yearReport
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate
import java.time.YearMonth

/** 연말 리포트 화면 값. 계산은 core:domain [yearReport] */
internal fun buildYearReportState(
    records: List<SubscriptionRecord>,
    settings: AppSettings,
    year: Int,
    today: LocalDate,
): YearReportState {
    val report = yearReport(records, year, today, settings.usdKrwRate)
    val content =
        if (report.isEmpty) {
            null
        } else {
            YearReportContent(
                totalKrw = report.totalKrw,
                monthlyAverageKrw = report.monthlyAverageKrw,
                monthlyKrw = report.monthlyKrw,
                topCategory = report.topCategory,
                longest =
                    report.longest?.let {
                        val subscription = it.record.subscription
                        LongestItem(
                            name = subscription.name,
                            colorKey = subscription.colorKey,
                            since = YearMonth.from(subscription.firstBillingDate),
                            yearCount = it.yearCount,
                        )
                    },
                savedKrw = report.savedKrw,
                savedCount = report.savedCount,
            )
        }
    return YearReportState(
        isLoading = false,
        year = year,
        years = listOf(today.year - 1, today.year, year).distinct().sorted(),
        content = content,
    )
}
