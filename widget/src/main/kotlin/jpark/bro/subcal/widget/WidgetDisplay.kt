package jpark.bro.subcal.widget

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.periodTotalKrw
import jpark.bro.subcal.core.domain.billing.upcomingPayments
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 위젯에 보일 다가오는 결제 한 건 */
internal data class WidgetPayment(
    val subscriptionId: String,
    val name: String,
    val colorKey: String,
    val daysUntil: Int,
    val amount: Money,
)

/** 위젯 한 번 그리기에 필요한 값. 작게는 [upcoming] 첫 건, 중간은 이번 달 구독료 + 두 건 */
internal data class WidgetDisplay(
    /** 이번 달 기간 이름이 되는 달 (1~12) */
    val month: Int,
    val totalKrw: Long,
    val upcoming: List<WidgetPayment>,
    val hasSubscriptions: Boolean,
)

/** 위젯이 보여줄 다가오는 결제 수 (product-spec 7장 중간 위젯) */
internal const val WIDGET_UPCOMING_LIMIT = 2

/** 홈과 같은 계산(widget/CLAUDE.md): 이번 달 구독료 = [periodTotalKrw], 다가오는 결제 = [upcomingPayments] */
internal fun buildWidgetDisplay(
    records: List<SubscriptionRecord>,
    settings: AppSettings,
    today: LocalDate,
): WidgetDisplay {
    val period = BillingPeriod.containing(today, settings.monthStartDay)
    return WidgetDisplay(
        month = period.month.monthValue,
        totalKrw = periodTotalKrw(records, period, today, settings.usdKrwRate),
        upcoming =
            upcomingPayments(records, today, WIDGET_UPCOMING_LIMIT).map {
                WidgetPayment(
                    subscriptionId = it.subscription.id,
                    name = it.subscription.name,
                    colorKey = it.subscription.colorKey,
                    daysUntil = ChronoUnit.DAYS.between(today, it.date).toInt(),
                    amount = it.amount,
                )
            },
        hasSubscriptions = records.isNotEmpty(),
    )
}
