package jpark.bro.subcal.core.domain.reminder

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.periodPayments
import jpark.bro.subcal.core.domain.billing.toKrw
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate

/** 월초 요약 알림 내용: 이번 달 기간에 생길 결제 합 · 건수 (홈 "이번 달 구독료"와 같은 계산) */
data class MonthlySummary(
    val period: BillingPeriod,
    val totalKrw: Long,
    val paymentCount: Int,
)

/**
 * 월초 요약 (product-spec 6장 알림 설정): 이번 달 기간 첫날(월 시작 기준일)에 한 번.
 * 월초 요약을 꺼 두었거나 오늘이 기간 첫날이 아니면 null. 결제가 없는 달에도 0원으로 알린다.
 */
fun monthlySummary(
    records: List<SubscriptionRecord>,
    appSettings: AppSettings,
    notificationSettings: NotificationSettings,
    today: LocalDate,
): MonthlySummary? {
    val period = BillingPeriod.containing(today, appSettings.monthStartDay)
    if (!notificationSettings.monthlySummaryEnabled || today != period.start) return null
    val payments = periodPayments(records, period, today)
    return MonthlySummary(
        period = period,
        totalKrw = payments.sumOf { it.amount.toKrw(appSettings.usdKrwRate) },
        paymentCount = payments.size,
    )
}
