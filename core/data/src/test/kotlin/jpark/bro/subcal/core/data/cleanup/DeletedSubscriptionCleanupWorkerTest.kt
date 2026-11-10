package jpark.bro.subcal.core.data.cleanup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import jpark.bro.subcal.core.data.mapper.toEntity
import jpark.bro.subcal.core.database.SubCalDatabase
import jpark.bro.subcal.core.database.entity.UsageLogEntity
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Subscription
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class DeletedSubscriptionCleanupWorkerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val now: Instant = Instant.parse("2026-10-30T03:00:00Z")
    private val clock = Clock.fixed(now, ZoneId.of("Asia/Seoul"))
    private val db =
        Room
            .inMemoryDatabaseBuilder(context, SubCalDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @After
    fun tearDown() = db.close()

    private suspend fun insert(
        id: String,
        deletedAgo: Duration?,
    ) {
        val subscription =
            Subscription(
                id = id,
                name = id,
                amount = Money(10_000, Currency.KRW),
                cycle = BillingCycle(CycleUnit.MONTH),
                firstBillingDate = LocalDate.of(2026, 1, 15),
                colorKey = "RED",
                createdAt = 0,
                updatedAt = 0,
            )
        db.subscriptionDao().upsertSubscription(
            subscription.toEntity().copy(deletedAt = deletedAgo?.let { now.minus(it).toEpochMilli() }),
        )
    }

    private suspend fun runWorker(): ListenableWorker.Result =
        TestListenableWorkerBuilder<DeletedSubscriptionCleanupWorker>(context)
            .setWorkerFactory(
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters,
                    ): ListenableWorker =
                        DeletedSubscriptionCleanupWorker(appContext, workerParameters, db.dataDao(), clock)
                },
            ).build()
            .doWork()

    @Test
    fun `지운 지 30일이 지난 구독만 정리하고 이용 중이거나 최근 지운 구독은 남긴다`() =
        runTest {
            insert("이용 중", deletedAgo = null)
            insert("어제 지움", deletedAgo = Duration.ofDays(1))
            insert("딱 30일 전", deletedAgo = SoftDeleteRetention)
            insert("31일 전", deletedAgo = Duration.ofDays(31))

            assertEquals(ListenableWorker.Result.success(), runWorker())

            // 남은 행 수: 정리 대상이 없으면 0을 돌려준다
            assertEquals(0, db.dataDao().purgeDeletedBefore(now.minus(SoftDeleteRetention).toEpochMilli()))
            assertEquals(2, db.dataDao().purgeDeletedBefore(Long.MAX_VALUE))
            assertEquals(listOf("이용 중"), db.subscriptionDao().get("이용 중")?.let { listOf(it.subscription.id) })
        }

    @Test
    fun `지운 지 30일이 지난 사용 기록도 정리한다`() =
        runTest {
            insert("이용 중", deletedAgo = null)

            fun log(
                id: String,
                deletedAgo: Duration?,
            ) = UsageLogEntity(id, "이용 중", "2026-10-01", 0, deletedAgo?.let { now.minus(it).toEpochMilli() })
            db.dataDao().insertAll(
                subscriptions = emptyList(),
                priceChanges = emptyList(),
                pauses = emptyList(),
                usageLogs =
                    listOf(
                        log("남김", null),
                        log("어제 취소", Duration.ofDays(1)),
                        log("31일 전 취소", Duration.ofDays(31)),
                    ),
                partyMembers = emptyList(),
                partySettlements = emptyList(),
            )

            runWorker()

            assertEquals(1, db.dataDao().purgeDeletedBefore(Long.MAX_VALUE))
            assertEquals(listOf("남김"), db.dataDao().getUsageLogs().map { it.id })
        }
}
