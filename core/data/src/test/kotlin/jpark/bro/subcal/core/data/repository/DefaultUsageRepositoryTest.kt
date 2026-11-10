package jpark.bro.subcal.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.subcal.core.common.IdGenerator
import jpark.bro.subcal.core.database.SubCalDatabase
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.model.UsageLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/** 오늘 = 2026.10.30 (product-spec 9장) */
@RunWith(AndroidJUnit4::class)
class DefaultUsageRepositoryTest {
    private val zone: ZoneId = ZoneId.of("Asia/Seoul")
    private val today: LocalDate = LocalDate.of(2026, 10, 30)
    private val clock = Clock.fixed(today.atTime(12, 0).atZone(zone).toInstant(), zone)
    private val db =
        Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SubCalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    private var nextId = 0
    private val ids = IdGenerator { "id-${++nextId}" }
    private val subscriptions = DefaultSubscriptionRepository(db.subscriptionDao(), ids, clock)
    private val repository = DefaultUsageRepository(db.usageLogDao(), db.subscriptionDao(), ids, clock)

    @After
    fun tearDown() = db.close()

    private suspend fun addSubscription(name: String): String =
        subscriptions
            .add(
                SubscriptionInput(
                    name = name,
                    amount = Money(17_000, Currency.KRW),
                    cycle = BillingCycle(CycleUnit.MONTH),
                    firstBillingDate = LocalDate.of(2024, 7, 2),
                    colorKey = "RED",
                    trackUsage = true,
                ),
            ).getOrThrow()

    @Test
    fun `같은 날 여러 번 기록할 수 있고 날짜순으로 나온다`() =
        runTest {
            val netflix = addSubscription("넷플릭스")

            repository.add(netflix, today).getOrThrow()
            repository.add(netflix, LocalDate.of(2026, 10, 2)).getOrThrow()
            repository.add(netflix, today).getOrThrow()

            assertEquals(
                listOf(LocalDate.of(2026, 10, 2), today, today),
                repository.observeUsageLogs().first().map(UsageLog::usedOn),
            )
        }

    @Test
    fun `내일 날짜나 없는 구독에는 기록하지 않는다`() =
        runTest {
            val netflix = addSubscription("넷플릭스")

            assertTrue(repository.add(netflix, today.plusDays(1)).isFailure)
            assertTrue(repository.add("없는-구독", today).isFailure)
            assertTrue(repository.observeUsageLogs().first().isEmpty())
        }

    @Test
    fun `실행 취소한 기록은 사라지고 두 번 취소할 수 없다`() =
        runTest {
            val netflix = addSubscription("넷플릭스")
            val kept = repository.add(netflix, today).getOrThrow()
            val undone = repository.add(netflix, today).getOrThrow()

            repository.remove(undone).getOrThrow()

            assertEquals(listOf(kept), repository.observeUsageLogs().first().map(UsageLog::id))
            assertTrue(repository.remove(undone).isFailure)
        }

    @Test
    fun `삭제한 구독의 기록은 나오지 않는다`() =
        runTest {
            val netflix = addSubscription("넷플릭스")
            val tving = addSubscription("티빙")
            repository.add(netflix, today).getOrThrow()
            repository.add(tving, today).getOrThrow()

            subscriptions.delete(tving).getOrThrow()

            assertEquals(listOf(netflix), repository.observeUsageLogs().first().map(UsageLog::subscriptionId))
        }
}
