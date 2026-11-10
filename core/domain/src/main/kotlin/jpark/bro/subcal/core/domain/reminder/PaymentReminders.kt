package jpark.bro.subcal.core.domain.reminder

import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.domain.billing.payments
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.Payment
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 알림 시점이 "기본"인 매년 결제 구독은 전역 설정과 상관없이 7일 전에 알린다 (product-spec 2-12) */
const val YEARLY_DEFAULT_DAYS_BEFORE = 7

/** 체험 알림: 첫 결제일(체험 종료일) 2일 전 · 1일 전 · 당일 (product-spec 2-7) */
val TrialReminderDays: Set<Int> = setOf(0, 1, 2)

/** 오늘 보낼 결제 알림 한 건: [Payment.date]가 오늘부터 [daysBefore]일 뒤 */
data class PaymentReminder(
    val payment: Payment,
    val daysBefore: Int,
)

/** 오늘 보낼 체험 알림 한 건: [Payment.date]는 첫 결제일(체험 종료일), 금액은 첫 결제 금액 */
data class TrialReminder(
    val firstPayment: Payment,
    val daysBefore: Int,
)

/**
 * 이 구독의 결제를 며칠 전에 알릴지 (product-spec 2-12). 끈 구독이면 null.
 * 결제 알림 전체 스위치([NotificationSettings.paymentEnabled])는 보지 않는다. 그건 [paymentReminders]가 본다.
 */
fun Subscription.reminderLeadDays(settings: NotificationSettings): Int? =
    when (reminderMode) {
        ReminderMode.OFF -> {
            null
        }

        ReminderMode.CUSTOM -> {
            reminderDaysBefore ?: settings.defaultDaysBefore
        }

        ReminderMode.DEFAULT -> {
            if (cycle.unit == CycleUnit.YEAR) YEARLY_DEFAULT_DAYS_BEFORE else settings.defaultDaysBefore
        }
    }

/**
 * [today]에 보낼 결제 알림, 결제일 · 이름순 (product-spec 2-12).
 * 결제일이 (오늘 + 알림 시점)인 구독을 고른다. 결제가 생기는지는 결제 발생 규칙(2-3)을 그대로 따르므로
 * 일시정지 기간 · 해지일 이후 · 첫 결제일 전에는 알리지 않는다.
 * 같은 날 그 구독의 체험 알림([trialReminders])이 있으면 체험 알림 하나만 보내므로 뺀다.
 */
fun paymentReminders(
    records: List<SubscriptionRecord>,
    settings: NotificationSettings,
    today: LocalDate,
): List<PaymentReminder> {
    if (!settings.paymentEnabled) return emptyList()
    val trialIds = trialReminders(records, settings, today).map { it.firstPayment.subscription.id }.toSet()
    return records
        .filterNot { it.subscription.id in trialIds }
        .mapNotNull { record ->
            val daysBefore = record.subscription.reminderLeadDays(settings) ?: return@mapNotNull null
            val target = today.plusDays(daysBefore.toLong())
            record
                .payments(from = target, untilExclusive = target.plusDays(1))
                .firstOrNull()
                ?.let { PaymentReminder(it, daysBefore) }
        }.sortedWith(compareBy({ it.payment.date }, { it.payment.subscription.name }))
}

/**
 * [today]에 보낼 체험 알림 (product-spec 2-7): 결과를 고르기 전인 체험의 첫 결제일이 오늘부터 2 · 1 · 0일 뒤.
 * 무료체험 알림을 끄면 없다. 첫 결제일 · 이름순.
 */
fun trialReminders(
    records: List<SubscriptionRecord>,
    settings: NotificationSettings,
    today: LocalDate,
): List<TrialReminder> {
    if (!settings.trialEnabled) return emptyList()
    return records
        .filter { it.subscription.isPendingTrial(today) }
        .mapNotNull { record ->
            val first = record.subscription.firstBillingDate
            val daysBefore = ChronoUnit.DAYS.between(today, first).toInt()
            if (daysBefore !in TrialReminderDays) return@mapNotNull null
            record.payments(first, first.plusDays(1)).firstOrNull()?.let { TrialReminder(it, daysBefore) }
        }.sortedWith(compareBy({ it.firstPayment.date }, { it.firstPayment.subscription.name }))
}
