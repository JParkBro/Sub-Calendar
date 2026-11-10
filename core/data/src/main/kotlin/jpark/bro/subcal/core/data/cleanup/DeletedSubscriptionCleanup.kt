package jpark.bro.subcal.core.data.cleanup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import jpark.bro.subcal.core.database.dao.DataDao
import java.time.Clock
import java.time.Duration
import java.util.concurrent.TimeUnit

/** soft delete한 구독을 이 기간이 지나면 지운다 (data-model-erd 1장). 그 전에는 실수 복구 · 백업 일관성에 쓴다 */
val SoftDeleteRetention: Duration = Duration.ofDays(SOFT_DELETE_RETENTION_DAYS)

private const val SOFT_DELETE_RETENTION_DAYS = 30L

/** 하루 한 번 정리 작업을 예약한다. 이미 예약돼 있으면 그대로 둔다 */
class DeletedSubscriptionCleanupScheduler internal constructor(
    private val workManager: WorkManager,
) {
    fun start() {
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DeletedSubscriptionCleanupWorker>(1, TimeUnit.DAYS).build(),
        )
    }

    internal companion object {
        const val WORK_NAME = "deleted_subscription_cleanup"
    }
}

/** [SoftDeleteRetention]보다 오래전에 soft delete한 구독과 하위 행(CASCADE)을 지운다 */
internal class DeletedSubscriptionCleanupWorker(
    context: Context,
    params: WorkerParameters,
    private val dataDao: DataDao,
    private val clock: Clock,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        dataDao.purgeDeletedBefore(cutoff = clock.millis() - SoftDeleteRetention.toMillis())
        return Result.success()
    }
}
