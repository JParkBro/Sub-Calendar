package jpark.bro.subcal.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import jpark.bro.subcal.core.domain.reminder.MonthlySummary
import jpark.bro.subcal.core.domain.reminder.PaymentReminder
import jpark.bro.subcal.core.domain.reminder.TrialReminder
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.navigation.AppIntents
import jpark.bro.subcal.core.ui.formatMoney
import java.time.LocalDate

/**
 * 결제 · 체험 알림을 띄운다. 구독마다 하나씩 보내고, 종류별로 두 건 이상이면 묶음 요약을 함께 보낸다.
 * 결제 알림을 누르면 그 구독 상세, 체험 알림을 누르면 무료체험 화면, 결제 요약 · 월초 요약을 누르면 앱이 열린다.
 */
internal class ReminderNotifier(
    private val context: Context,
) {
    private val manager = NotificationManagerCompat.from(context)

    /** 알림 종류: 채널 · 묶음 · 누르면 열 화면 */
    private enum class Kind(
        val channelId: String,
        @StringRes val channelName: Int,
        @StringRes val channelDescription: Int,
        val group: String,
        @StringRes val summaryTitle: Int,
    ) {
        PAYMENT(
            CHANNEL_PAYMENT,
            R.string.notification_channel_payment,
            R.string.notification_channel_payment_description,
            "jpark.bro.subcal.PAYMENT",
            R.string.notification_payment_summary_title,
        ),
        TRIAL(
            CHANNEL_TRIAL,
            R.string.notification_channel_trial,
            R.string.notification_channel_trial_description,
            "jpark.bro.subcal.TRIAL",
            R.string.notification_trial_summary_title,
        ),

        /** 월초 요약: 한 달에 한 건이라 묶음 요약이 없다 */
        MONTHLY(
            CHANNEL_MONTHLY,
            R.string.notification_channel_monthly,
            R.string.notification_channel_monthly_description,
            "jpark.bro.subcal.MONTHLY",
            R.string.notification_monthly_title,
        ),
    }

    /** 알림 한 건 */
    private class Item(
        val subscriptionId: String,
        val title: String,
        val text: String,
        val summaryLine: String,
    )

    fun createChannels() {
        Kind.entries.forEach { kind ->
            manager.createNotificationChannel(
                NotificationChannelCompat
                    .Builder(kind.channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .setName(context.getString(kind.channelName))
                    .setDescription(context.getString(kind.channelDescription))
                    .build(),
            )
        }
    }

    fun notify(
        payments: List<PaymentReminder>,
        trials: List<TrialReminder>,
        monthly: MonthlySummary? = null,
    ) {
        val nothingToSend = payments.isEmpty() && trials.isEmpty() && monthly == null
        if (nothingToSend || !canNotify()) return
        createChannels()
        post(
            Kind.PAYMENT,
            payments.map { reminder ->
                val name = reminder.payment.subscription.name
                val text = paymentMessage(reminder)
                Item(reminder.payment.subscription.id, name, text, summaryLine(name, text))
            },
        )
        post(
            Kind.TRIAL,
            trials.map { reminder ->
                val name = reminder.firstPayment.subscription.name
                val text = trialMessage(reminder)
                Item(
                    subscriptionId = reminder.firstPayment.subscription.id,
                    title = context.getString(R.string.notification_trial_title, name),
                    text = text,
                    summaryLine = summaryLine(name, text),
                )
            },
        )
        monthly?.let { summary ->
            val title = context.getString(R.string.notification_monthly_title, summary.period.month.monthValue)
            val text =
                context.getString(
                    R.string.notification_monthly_text,
                    context.formatMoney(Money(summary.totalKrw, Currency.KRW)),
                    summary.paymentCount,
                )
            post(Kind.MONTHLY, listOf(Item(MONTHLY_TAG, title, text, text)))
        }
    }

    // 권한은 canNotify()로 확인했다
    @SuppressLint("MissingPermission")
    private fun post(
        kind: Kind,
        items: List<Item>,
    ) {
        items.forEach { item ->
            val tag = "${kind.name}_${item.subscriptionId}"
            val notification =
                builder(kind)
                    .setContentTitle(item.title)
                    .setContentText(item.text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(item.text))
                    .setContentIntent(openIntent(kind, item.subscriptionId, requestCode = tag.hashCode()))
                    .build()
            manager.notify(tag, NOTIFICATION_ID, notification)
        }
        if (items.size > 1) {
            val style = NotificationCompat.InboxStyle()
            items.forEach { style.addLine(it.summaryLine) }
            val summaryTag = "${kind.name}_summary"
            val summary =
                builder(kind)
                    .setContentTitle(context.getString(kind.summaryTitle, items.size))
                    .setStyle(style)
                    .setGroupSummary(true)
                    .setContentIntent(openIntent(kind, subscriptionId = null, requestCode = summaryTag.hashCode()))
                    .build()
            manager.notify(summaryTag, NOTIFICATION_ID, summary)
        }
    }

    private fun canNotify(): Boolean {
        val permitted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        return permitted && manager.areNotificationsEnabled()
    }

    private fun builder(kind: Kind): NotificationCompat.Builder =
        NotificationCompat
            .Builder(context, kind.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setGroup(kind.group)
            .setAutoCancel(true)

    private fun summaryLine(
        name: String,
        text: String,
    ): String = context.getString(R.string.notification_payment_summary_line, name, text)

    private fun paymentMessage(reminder: PaymentReminder): String {
        val amount = context.formatMoney(reminder.payment.amount)
        val date = reminder.payment.date
        return when (reminder.daysBefore) {
            0 -> {
                context.getString(R.string.notification_payment_today, amount)
            }

            1 -> {
                context.getString(R.string.notification_payment_tomorrow, amount)
            }

            else -> {
                context.getString(
                    R.string.notification_payment_days_later,
                    reminder.daysBefore,
                    date.monthValue,
                    date.dayOfMonth,
                    amount,
                )
            }
        }
    }

    private fun trialMessage(reminder: TrialReminder): String {
        val amount = context.formatMoney(reminder.firstPayment.amount)
        val date: LocalDate = reminder.firstPayment.date
        return when (reminder.daysBefore) {
            0 -> {
                context.getString(R.string.notification_trial_today, amount)
            }

            1 -> {
                context.getString(R.string.notification_trial_tomorrow, amount)
            }

            else -> {
                context.getString(
                    R.string.notification_trial_days_later,
                    reminder.daysBefore,
                    date.monthValue,
                    date.dayOfMonth,
                    amount,
                )
            }
        }
    }

    /**
     * 앱의 첫 Activity를 연다. app이 백스택을 만든다 (module-map 4-3):
     * 결제는 그 구독 상세(요약은 홈), 체험은 무료체험 화면.
     */
    private fun openIntent(
        kind: Kind,
        subscriptionId: String?,
        requestCode: Int,
    ): PendingIntent? {
        val intent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                when (kind) {
                    Kind.PAYMENT -> subscriptionId?.let { putExtra(AppIntents.EXTRA_OPEN_SUBSCRIPTION_ID, it) }
                    Kind.TRIAL -> putExtra(AppIntents.EXTRA_OPEN_TRIAL, true)
                    Kind.MONTHLY -> Unit
                }
            } ?: return null
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_PAYMENT = "payment"
        const val CHANNEL_TRIAL = "trial"
        const val CHANNEL_MONTHLY = "monthly"
        private const val MONTHLY_TAG = "this_month"
        private const val NOTIFICATION_ID = 1
    }
}
