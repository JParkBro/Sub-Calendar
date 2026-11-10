package jpark.bro.subcal.core.notification

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import jpark.bro.subcal.core.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * 결제 알림 작업을 알림 설정의 시각에 하루 한 번 돌도록 예약한다 (product-spec 2-12).
 * 정확한 알람 권한을 쓰지 않아 몇 분 늦을 수 있다. 예약은 기기를 다시 켜도 WorkManager가 유지한다.
 */
class ReminderScheduler internal constructor(
    private val workManager: WorkManager,
    private val settingsRepository: SettingsRepository,
    private val notifier: PaymentNotifier,
    private val clock: Clock,
) {
    /** 앱 시작마다 한 번: 알림 채널을 만들고, 알림 시각을 지켜보다 바뀌면 다시 예약한다 */
    fun start(scope: CoroutineScope) {
        notifier.createChannel()
        settingsRepository
            .observeNotificationSettings()
            .map { it.time }
            .distinctUntilChanged()
            .onEach(::schedule)
            .launchIn(scope)
    }

    /** 같은 시각으로 이미 예약돼 있으면 그대로 두고(복원 · 재시작), 시각이 다르면 새로 예약한다 */
    internal suspend fun schedule(time: LocalTime) {
        val tag = timeTag(time)
        val alreadyScheduled =
            workManager
                .getWorkInfosForUniqueWorkFlow(WORK_NAME)
                .first()
                .any { !it.state.isFinished && tag in it.tags }
        if (alreadyScheduled) return

        val request =
            PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayUntilNext(LocalDateTime.now(clock), time))
                .addTag(tag)
                .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
    }

    internal companion object {
        const val WORK_NAME = "payment_reminder"

        fun timeTag(time: LocalTime): String = "payment_reminder_at_$time"
    }
}

/** [now] 다음으로 오는 [time]까지 남은 시간. 오늘 그 시각이 지났거나 지금이면 내일 */
internal fun delayUntilNext(
    now: LocalDateTime,
    time: LocalTime,
): Duration {
    val todayAt = now.toLocalDate().atTime(time)
    val next = if (now.isBefore(todayAt)) todayAt else todayAt.plusDays(1)
    return Duration.between(now, next)
}
