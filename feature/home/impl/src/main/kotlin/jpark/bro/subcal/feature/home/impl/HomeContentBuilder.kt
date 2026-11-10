package jpark.bro.subcal.feature.home.impl

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.annualizedKrw
import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.monthlyAverageKrw
import jpark.bro.subcal.core.domain.billing.periodChangeKrw
import jpark.bro.subcal.core.domain.billing.periodTotalKrw
import jpark.bro.subcal.core.domain.billing.priceOn
import jpark.bro.subcal.core.domain.billing.upcomingPayments
import jpark.bro.subcal.core.domain.insight.yearReport
import jpark.bro.subcal.core.domain.insight.yearReportBannerYear
import jpark.bro.subcal.core.domain.trial.trialsEndingSoon
import jpark.bro.subcal.core.domain.trial.unresolvedTrials
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 홈에 보여줄 값 (product-spec 4-1). 계산은 core:domain 규칙을 쓴다 */
internal fun buildHomeState(
    records: List<SubscriptionRecord>,
    settings: AppSettings,
    today: LocalDate,
    showBillingDateHint: Boolean = false,
): HomeState {
    val rate = settings.usdKrwRate
    val period = BillingPeriod.containing(today, settings.monthStartDay)
    val summary =
        MonthSummary(
            month = period.month.monthValue,
            range = if (period.startDay == 1) null else period.start..period.endExclusive.minusDays(1),
            totalKrw = periodTotalKrw(records, period, today, rate),
            changeKrw = periodChangeKrw(records, period, today, rate),
            monthlyAverageKrw = monthlyAverageKrw(records, today, rate),
            annualKrw = annualizedKrw(records, today, rate),
        )
    // 목록의 "이용 중" 필터와 같은 기준: 결과를 고르기 전인 체험은 뺀다
    val active =
        records.filter { it.subscription.status == SubscriptionStatus.ACTIVE && !it.subscription.isPendingTrial(today) }
    return HomeState(
        isLoading = false,
        isEmpty = records.isEmpty(),
        // 등록한 구독을 모두 지웠으면 안내할 결제일도 없다
        showBillingDateHint = showBillingDateHint && records.isNotEmpty(),
        summary = summary,
        upcoming =
            upcomingPayments(records, today, UPCOMING_LIMIT).map {
                UpcomingPayment(it, ChronoUnit.DAYS.between(today, it.date).toInt())
            },
        categories =
            active
                .groupingBy { it.subscription.category }
                .eachCount()
                .toSortedMap()
                .map { (category, count) -> CategoryCount(category, count) },
        trialsEndingSoon = trialsEndingSoon(records, today).size,
        unresolvedTrials =
            unresolvedTrials(records, today).map { record ->
                val subscription = record.subscription
                UnresolvedTrial(
                    id = subscription.id,
                    name = subscription.name,
                    colorKey = subscription.colorKey,
                    firstBillingDate = subscription.firstBillingDate,
                    amount = record.priceOn(subscription.firstBillingDate),
                )
            },
        yearReportBanner =
            yearReportBannerYear(today)
                ?.let { yearReport(records, it, today, rate) }
                ?.takeUnless { it.isEmpty }
                ?.let { YearReportBanner(it.year, it.totalKrw) },
    )
}
