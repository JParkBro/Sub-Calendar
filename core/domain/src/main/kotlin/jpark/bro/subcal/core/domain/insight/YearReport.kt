package jpark.bro.subcal.core.domain.insight

import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.payments
import jpark.bro.subcal.core.domain.billing.roundedDiv
import jpark.bro.subcal.core.domain.billing.toKrw
import jpark.bro.subcal.core.domain.party.toMyShare
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate
import java.time.Month
import java.time.Year

private const val MONTHS_PER_YEAR = 12
private const val PERCENT = 100L

/**
 * 연말 리포트 (product-spec 4-11). 기간은 [year]의 1월 1일 ~ 12월 31일이고 아직 오지 않은 결제도 넣는다.
 * 지출 · 카테고리는 내 부담액(파티 구독은 내 몫, 통계와 같음), 아낀 돈은 아낀 돈 화면과 같은 전체 금액.
 */
data class YearReport(
    val year: Int,
    val totalKrw: Long,
    /** 1월 ~ 12월 결제 합 (12개) */
    val monthlyKrw: List<Long>,
    val topCategory: TopCategory?,
    val longest: LongestSubscription?,
    val savedKrw: Long,
    /** 그 해에 아낀 돈이 있는 해지한 구독 수 */
    val savedCount: Int,
) {
    /** 월 평균: 총 지출 ÷ 12, 원 단위 반올림 */
    val monthlyAverageKrw: Long get() = roundedDiv(totalKrw, MONTHS_PER_YEAR.toLong())

    /** 보여줄 내용이 없는 해 (결제도 아낀 돈도 없음) */
    val isEmpty: Boolean get() = totalKrw == 0L && savedKrw == 0L
}

/** 그 해 결제 합이 가장 큰 카테고리 */
data class TopCategory(
    val category: Category,
    val totalKrw: Long,
    /** 총 지출 대비 정수 %, 내림 */
    val percent: Int,
    /** 그 해 이 카테고리에서 결제가 있었던 구독 수 */
    val subscriptionCount: Int,
)

/** 그 해 결제가 있던 구독 중 첫 결제일이 가장 이른 것 (해지한 구독 포함) */
data class LongestSubscription(
    val record: SubscriptionRecord,
    /** n년째 = 연도 − 첫 결제 연도 + 1 */
    val yearCount: Int,
)

fun yearReport(
    records: List<SubscriptionRecord>,
    year: Int,
    today: LocalDate,
    usdKrwRate: Long,
): YearReport {
    val from = Year.of(year).atDay(1)
    val until = from.plusYears(1)
    val payments =
        records
            .filterNot { it.subscription.isPendingTrial(today) }
            .map(SubscriptionRecord::toMyShare)
            .flatMap { it.payments(from, until) }
    val total = payments.sumOf { it.amount.toKrw(usdKrwRate) }
    val monthly =
        Month.entries.map { month ->
            payments.filter { it.date.month == month }.sumOf { it.amount.toKrw(usdKrwRate) }
        }
    val topCategory =
        payments
            .groupBy { it.subscription.category }
            .map { (category, list) ->
                TopCategory(
                    category = category,
                    totalKrw = list.sumOf { it.amount.toKrw(usdKrwRate) },
                    percent = 0,
                    subscriptionCount = list.distinctBy { it.subscription.id }.size,
                )
            }.filter { it.totalKrw > 0 }
            .sortedWith(compareByDescending<TopCategory> { it.totalKrw }.thenBy { it.category.ordinal })
            .firstOrNull()
            ?.let { it.copy(percent = (it.totalKrw * PERCENT / total).toInt()) }
    val paidIds = payments.map { it.subscription.id }.toSet()
    val longest =
        records
            .filter { it.subscription.id in paidIds }
            .minWithOrNull(compareBy({ it.subscription.firstBillingDate }, { it.subscription.name }))
            ?.let { LongestSubscription(it, year - it.subscription.firstBillingDate.year + 1) }
    val saved =
        records
            .map { record -> record.avoidedPayments(from, until).sumOf { it.amount.toKrw(usdKrwRate) } }
            .filter { it > 0 }
    return YearReport(
        year = year,
        totalKrw = total,
        monthlyKrw = monthly,
        topCategory = topCategory,
        longest = longest,
        savedKrw = saved.sum(),
        savedCount = saved.size,
    )
}

/** 홈 배너에 띄울 리포트 연도: 12월에는 올해, 1월에는 지난해, 그 밖의 달에는 없음 (product-spec 4-11) */
fun yearReportBannerYear(today: LocalDate): Int? =
    when (today.month) {
        Month.DECEMBER -> today.year
        Month.JANUARY -> today.year - 1
        else -> null
    }
