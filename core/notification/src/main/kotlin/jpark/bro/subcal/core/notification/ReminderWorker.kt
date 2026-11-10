package jpark.bro.subcal.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import jpark.bro.subcal.core.data.repository.PartyRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.datastore.SettingsDataStore
import jpark.bro.subcal.core.domain.party.partyReminders
import jpark.bro.subcal.core.domain.reminder.monthlySummary
import jpark.bro.subcal.core.domain.reminder.paymentReminders
import jpark.bro.subcal.core.domain.reminder.trialReminders
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

/**
 * 하루 한 번 알림 시각에 돈다: 오늘 보낼 결제 · 체험 · 정산 알림과 월초 요약을 계산해 띄운다
 * (product-spec 2-7, 2-12, 4-9, 6장).
 * 알림 시각을 바꾸거나 작업이 늦게 돌아 같은 날 두 번 실행돼도 알림은 한 번만 보낸다.
 */
@Suppress("LongParameterList") // WorkManager 진입점: context · params는 WorkManager가, 나머지는 Koin이 넘긴다
internal class ReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val subscriptionRepository: SubscriptionRepository,
    private val partyRepository: PartyRepository,
    private val settingsDataStore: SettingsDataStore,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val today = LocalDate.now(clock)
        if (settingsDataStore.lastPaymentReminderDate.first() == today) return Result.success()

        val records = subscriptionRepository.observeRecords().first()
        val settings = settingsDataStore.notificationSettings.first()
        val appSettings = settingsDataStore.appSettings.first()
        val settlements = partyRepository.observeSettlements().first()
        notifier.notify(
            payments = paymentReminders(records, settings, today),
            trials = trialReminders(records, settings, today),
            monthly = monthlySummary(records, appSettings, settings, today),
            parties = partyReminders(records, settlements, settings, appSettings.monthStartDay, today),
        )
        settingsDataStore.setLastPaymentReminderDate(today)
        return Result.success()
    }
}
