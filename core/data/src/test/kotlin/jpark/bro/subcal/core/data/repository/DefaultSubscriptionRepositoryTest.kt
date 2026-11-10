package jpark.bro.subcal.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.subcal.core.common.IdGenerator
import jpark.bro.subcal.core.database.SubCalDatabase
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 오늘 = 2026.10.30 (product-spec 9장) */
@RunWith(AndroidJUnit4::class)
class DefaultSubscriptionRepositoryTest {
    private lateinit var db: SubCalDatabase
    private lateinit var repository: DefaultSubscriptionRepository
    private val zone: ZoneId = ZoneId.of("Asia/Seoul")
    private val today: LocalDate = LocalDate.of(2026, 10, 30)
    private val clock = Clock.fixed(today.atTime(12, 0).atZone(zone).toInstant(), zone)
    private val tomorrowClock = Clock.fixed(Instant.parse("2026-10-31T03:00:00Z"), zone)
    private var nextId = 0
    private val ids = IdGenerator { "id-${++nextId}" }

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SubCalDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = DefaultSubscriptionRepository(db.subscriptionDao(), ids, clock)
    }

    @After
    fun tearDown() = db.close()

    private fun input(
        amount: Long = 10_000,
        currency: Currency = Currency.KRW,
        first: LocalDate = LocalDate.of(2026, 1, 15),
        trialStartedOn: LocalDate? = null,
    ) = SubscriptionInput(
        name = "넷플릭스",
        category = Category.VIDEO,
        amount = Money(amount, currency),
        cycle = BillingCycle(CycleUnit.MONTH),
        firstBillingDate = first,
        trialStartedOn = trialStartedOn,
        colorKey = "RED",
    )

    private suspend fun add(input: SubscriptionInput = input()): String = repository.add(input).getOrThrow()

    private suspend fun record(id: String): SubscriptionRecord = repository.observeRecord(id).first()!!

    private fun assertFailure(result: Result<Unit>) = assertTrue(result.isFailure)

    @Test
    fun `추가하면 입력값과 기본 상태로 저장된다`() =
        runTest {
            val id = add(input().copy(memo = "메모", paymentMethod = "신한카드"))

            val subscription = record(id).subscription

            assertEquals("id-1", subscription.id)
            assertEquals("넷플릭스", subscription.name)
            assertEquals(Category.VIDEO, subscription.category)
            assertEquals(Money(10_000, Currency.KRW), subscription.amount)
            assertEquals("메모", subscription.memo)
            assertEquals("신한카드", subscription.paymentMethod)
            assertEquals(SubscriptionStatus.ACTIVE, subscription.status)
            assertEquals(TrialOutcome.NONE, subscription.trialOutcome)
            assertEquals(clock.millis(), subscription.createdAt)
        }

    @Test
    fun `체험 시작일이 있으면 결과 대기 상태로 저장된다`() =
        runTest {
            val id = add(input(trialStartedOn = LocalDate.of(2026, 10, 1)))

            assertEquals(TrialOutcome.PENDING, record(id).subscription.trialOutcome)
        }

    @Test
    fun `금액이 그대로면 가격 변경이 생기지 않는다`() =
        runTest {
            val id = add()

            repository.update(id, input().copy(name = "새 이름")).getOrThrow()

            assertEquals("새 이름", record(id).subscription.name)
            assertTrue(record(id).priceChanges.isEmpty())
        }

    @Test
    fun `금액을 바꾸면 기본은 다음 결제일부터 적용되고 기본 금액은 그대로다`() =
        runTest {
            val id = add()

            repository.update(id, input(amount = 12_000)).getOrThrow()

            val saved = record(id)
            assertEquals(10_000, saved.subscription.amount.amountMinor)
            assertEquals(listOf(LocalDate.of(2026, 11, 15)), saved.priceChanges.map { it.effectiveFrom })
            assertEquals(12_000, saved.priceChanges.single().amountMinor)
        }

    @Test
    fun `적용일이 같은 가격 변경은 새로 만들지 않고 고쳐 쓴다`() =
        runTest {
            val id = add()

            repository.update(id, input(amount = 12_000)).getOrThrow()
            repository.update(id, input(amount = 13_000)).getOrThrow()

            assertEquals(13_000, record(id).priceChanges.single().amountMinor)
        }

    @Test
    fun `처음부터 적용하면 가격 변경을 지우고 기본 금액을 바꾼다`() =
        runTest {
            val id = add()
            repository.update(id, input(amount = 12_000)).getOrThrow()

            repository.update(id, input(amount = 15_000), PriceChangeTiming.FROM_START).getOrThrow()

            assertEquals(15_000, record(id).subscription.amount.amountMinor)
            assertTrue(record(id).priceChanges.isEmpty())
        }

    @Test
    fun `통화가 바뀌면 처음부터 적용한다`() =
        runTest {
            val id = add()
            repository.update(id, input(amount = 12_000)).getOrThrow()

            repository.update(id, input(amount = 999, currency = Currency.USD)).getOrThrow()

            assertEquals(Money(999, Currency.USD), record(id).subscription.amount)
            assertTrue(record(id).priceChanges.isEmpty())
        }

    @Test
    fun `일시정지하면 상태가 바뀌고 오늘 시작하는 열린 기간이 생긴다`() =
        runTest {
            val id = add()

            repository.pause(id).getOrThrow()

            val saved = record(id)
            assertEquals(SubscriptionStatus.PAUSED, saved.subscription.status)
            assertEquals(today, saved.pauses.single().startDate)
            assertNull(saved.pauses.single().endDate)
        }

    @Test
    fun `이미 일시정지 중이면 다시 일시정지할 수 없다`() =
        runTest {
            val id = add()
            repository.pause(id).getOrThrow()

            assertFailure(repository.pause(id))

            assertEquals(1, record(id).pauses.size)
        }

    @Test
    fun `다음 날 다시 시작하면 끝일은 어제다`() =
        runTest {
            val id = add()
            repository.pause(id).getOrThrow()
            val nextDay = DefaultSubscriptionRepository(db.subscriptionDao(), ids, tomorrowClock)

            nextDay.resume(id).getOrThrow()

            val saved = record(id)
            assertEquals(SubscriptionStatus.ACTIVE, saved.subscription.status)
            assertEquals(today, saved.pauses.single().endDate)
        }

    @Test
    fun `일시정지한 날 바로 다시 시작하면 기간이 남지 않는다`() =
        runTest {
            val id = add()
            repository.pause(id).getOrThrow()

            repository.resume(id).getOrThrow()

            assertEquals(SubscriptionStatus.ACTIVE, record(id).subscription.status)
            assertTrue(record(id).pauses.isEmpty())
        }

    @Test
    fun `일시정지 중이 아니면 다시 시작할 수 없다`() =
        runTest {
            assertFailure(repository.resume(add()))
        }

    @Test
    fun `해지하면 상태와 해지일이 저장된다`() =
        runTest {
            val id = add()

            repository.cancel(id, today).getOrThrow()

            assertEquals(SubscriptionStatus.CANCELLED, record(id).subscription.status)
            assertEquals(today, record(id).subscription.cancelledOn)
        }

    @Test
    fun `일시정지 중 해지하면 열린 기간을 닫는다`() =
        runTest {
            val id = add()
            repository.pause(id).getOrThrow()
            val nextWeek = LocalDate.of(2026, 11, 6)

            repository.cancel(id, nextWeek).getOrThrow()

            assertEquals(nextWeek.minusDays(1), record(id).pauses.single().endDate)
        }

    @Test
    fun `해지를 취소하면 이용 중으로 돌아간다`() =
        runTest {
            val id = add()
            repository.cancel(id, today).getOrThrow()

            repository.undoCancel(id).getOrThrow()

            assertEquals(SubscriptionStatus.ACTIVE, record(id).subscription.status)
            assertNull(record(id).subscription.cancelledOn)
        }

    @Test
    fun `체험 해지와 취소는 체험 결과도 함께 바꾼다`() =
        runTest {
            val id = add(input(trialStartedOn = LocalDate.of(2026, 10, 1), first = LocalDate.of(2026, 11, 1)))

            repository.cancel(id, today).getOrThrow()
            assertEquals(TrialOutcome.CANCELLED, record(id).subscription.trialOutcome)

            repository.undoCancel(id).getOrThrow()
            assertEquals(TrialOutcome.PENDING, record(id).subscription.trialOutcome)
        }

    @Test
    fun `구독별 알림 시점을 바꾸고 직접 고른 값이 아니면 일수를 지운다`() =
        runTest {
            val id = add()

            repository.setReminder(id, ReminderMode.CUSTOM, daysBefore = 3).getOrThrow()
            assertEquals(ReminderMode.CUSTOM, record(id).subscription.reminderMode)
            assertEquals(3, record(id).subscription.reminderDaysBefore)

            repository.setReminder(id, ReminderMode.OFF, daysBefore = 3).getOrThrow()
            assertEquals(ReminderMode.OFF, record(id).subscription.reminderMode)
            assertNull(record(id).subscription.reminderDaysBefore)
        }

    @Test
    fun `고를 수 없는 알림 시점은 저장하지 않는다`() =
        runTest {
            val id = add()

            assertFailure(repository.setReminder(id, ReminderMode.CUSTOM, daysBefore = 2))
            assertFailure(repository.setReminder(id, ReminderMode.CUSTOM, daysBefore = null))
            assertEquals(ReminderMode.DEFAULT, record(id).subscription.reminderMode)
        }

    @Test
    fun `삭제하면 목록과 단건에서 사라진다`() =
        runTest {
            val id = add()

            repository.delete(id).getOrThrow()

            assertTrue(repository.observeRecords().first().isEmpty())
            assertNull(repository.observeRecord(id).first())
        }

    @Test
    fun `없는 구독을 고치면 실패한다`() =
        runTest {
            assertFailure(repository.pause("없음"))
            assertFailure(repository.update("없음", input()))
            assertFailure(repository.delete("없음"))
        }
}
