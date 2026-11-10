package jpark.bro.subcal.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import jpark.bro.subcal.core.domain.reminder.PaymentReminder
import jpark.bro.subcal.core.navigation.AppIntents
import jpark.bro.subcal.core.ui.formatMoney

/**
 * 결제 알림을 띄운다. 구독마다 하나씩 보내고, 두 건 이상이면 묶음 요약을 함께 보낸다.
 * 알림을 누르면 그 구독의 상세가, 요약을 누르면 앱이 열린다.
 */
internal class PaymentNotifier(
    private val context: Context,
) {
    private val manager = NotificationManagerCompat.from(context)

    fun createChannel() {
        manager.createNotificationChannel(
            NotificationChannelCompat
                .Builder(CHANNEL_PAYMENT, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.notification_channel_payment))
                .setDescription(context.getString(R.string.notification_channel_payment_description))
                .build(),
        )
    }

    // 권한은 canNotify()로 확인한다
    @SuppressLint("MissingPermission")
    fun notify(reminders: List<PaymentReminder>) {
        if (reminders.isEmpty() || !canNotify()) return
        createChannel()
        reminders.forEach { reminder ->
            val subscription = reminder.payment.subscription
            val notification =
                baseBuilder()
                    .setContentTitle(subscription.name)
                    .setContentText(reminder.message())
                    .setContentIntent(openAppIntent(subscription.id))
                    .build()
            manager.notify(subscription.id, NOTIFICATION_ID, notification)
        }
        if (reminders.size > 1) {
            val style = NotificationCompat.InboxStyle()
            reminders.forEach { reminder ->
                style.addLine(
                    context.getString(
                        R.string.notification_payment_summary_line,
                        reminder.payment.subscription.name,
                        reminder.message(),
                    ),
                )
            }
            val summary =
                baseBuilder()
                    .setContentTitle(context.getString(R.string.notification_payment_summary_title, reminders.size))
                    .setStyle(style)
                    .setGroupSummary(true)
                    .setContentIntent(openAppIntent(subscriptionId = null))
                    .build()
            manager.notify(SUMMARY_TAG, NOTIFICATION_ID, summary)
        }
    }

    private fun canNotify(): Boolean {
        val permitted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        return permitted && manager.areNotificationsEnabled()
    }

    private fun baseBuilder(): NotificationCompat.Builder =
        NotificationCompat
            .Builder(context, CHANNEL_PAYMENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setGroup(GROUP_PAYMENT)
            .setAutoCancel(true)

    private fun PaymentReminder.message(): String {
        val amount = context.formatMoney(payment.amount)
        return when (daysBefore) {
            0 -> {
                context.getString(R.string.notification_payment_today, amount)
            }

            1 -> {
                context.getString(R.string.notification_payment_tomorrow, amount)
            }

            else -> {
                context.getString(
                    R.string.notification_payment_days_later,
                    daysBefore,
                    payment.date.monthValue,
                    payment.date.dayOfMonth,
                    amount,
                )
            }
        }
    }

    /** 앱의 첫 Activity를 연다. 구독 id가 있으면 app이 그 상세까지 백스택을 만든다 */
    private fun openAppIntent(subscriptionId: String?): PendingIntent? {
        val intent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                subscriptionId?.let { putExtra(AppIntents.EXTRA_OPEN_SUBSCRIPTION_ID, it) }
            } ?: return null
        return PendingIntent.getActivity(
            context,
            subscriptionId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_PAYMENT = "payment"
        private const val GROUP_PAYMENT = "jpark.bro.subcal.PAYMENT"
        private const val SUMMARY_TAG = "payment_summary"
        private const val NOTIFICATION_ID = 1
    }
}
