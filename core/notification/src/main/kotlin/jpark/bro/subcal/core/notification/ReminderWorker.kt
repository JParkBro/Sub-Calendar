package jpark.bro.subcal.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.datastore.SettingsDataStore
import jpark.bro.subcal.core.domain.reminder.monthlySummary
import jpark.bro.subcal.core.domain.reminder.paymentReminders
import jpark.bro.subcal.core.domain.reminder.trialReminders
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

/**
 * 하루 한 번 알림 시각에 돈다: 오늘 보낼 결제 · 체험 알림과 월초 요약을 계산해 띄운다 (product-spec 2-7, 2-12, 6장).
 * 알림 시각을 바꾸거나 작업이 늦게 돌아 같은 날 두 번 실행돼도 알림은 한 번만 보낸다.
 */
internal class ReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val subscriptionRepository: SubscriptionRepository,
    private val settingsDataStore: SettingsDataStore,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val today = LocalDate.now(clock)
        if (settingsDataStore.lastPaymentReminderDate.first() == today) return Result.success()

        val records = subscriptionRepository.observeRecords().first()
        val settings = settingsDataStore.notificationSettings.first()
        notifier.notify(
            payments = paymentReminders(records, settings, today),
            trials = trialReminders(records, settings, today),
            monthly = monthlySummary(records, settingsDataStore.appSettings.first(), settings, today),
        )
        settingsDataStore.setLastPaymentReminderDate(today)
        return Result.success()
    }
}
