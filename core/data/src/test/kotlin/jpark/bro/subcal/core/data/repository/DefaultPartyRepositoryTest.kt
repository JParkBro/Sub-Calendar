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
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.model.SubscriptionRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** product-spec 9장 유튜브 프리미엄 파티 (오늘 = 2026.10.30) */
@RunWith(AndroidJUnit4::class)
class DefaultPartyRepositoryTest {
    private val zone: ZoneId = ZoneId.of("Asia/Seoul")
    private val clock =
        Clock.fixed(
            LocalDate
                .of(2026, 10, 30)
                .atTime(12, 0)
                .atZone(zone)
                .toInstant(),
            zone,
        )
    private val db =
        Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SubCalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    private var nextId = 0
    private val ids = IdGenerator { "id-${++nextId}" }
    private val subscriptions = DefaultSubscriptionRepository(db.subscriptionDao(), ids, clock)
    private val repository = DefaultPartyRepository(db.partyDao(), db.subscriptionDao(), ids, clock)

    @After
    fun tearDown() = db.close()

    private suspend fun youtube(): String =
        subscriptions
            .add(
                SubscriptionInput(
                    name = "유튜브 프리미엄",
                    amount = Money(14_900, Currency.KRW),
                    cycle = BillingCycle(CycleUnit.MONTH),
                    firstBillingDate = LocalDate.of(2025, 1, 14),
                    colorKey = "PINK",
                ),
            ).getOrThrow()

    private suspend fun record(id: String): SubscriptionRecord = requireNotNull(subscriptions.observeRecord(id).first())

    private suspend fun members(id: String): List<PartyMember> = record(id).partyMembers

    @Test
    fun `멤버는 추가한 순서로 구독에 붙고 이름 앞뒤 공백을 뺀다`() =
        runTest {
            val youtube = youtube()

            listOf(" 민지 ", "준호", "서연").forEach { repository.addMember(youtube, it).getOrThrow() }

            assertEquals(listOf("민지", "준호", "서연"), members(youtube).map { it.name })
            assertEquals(listOf(0, 1, 2), members(youtube).map { it.sortOrder })
        }

    @Test
    fun `이름은 1~10자, 멤버는 9명까지만 받는다`() =
        runTest {
            val youtube = youtube()

            assertTrue(repository.addMember(youtube, "  ").isFailure)
            assertTrue(repository.addMember(youtube, "열한글자가넘는이름이다").isFailure)
            repeat(9) { repository.addMember(youtube, "멤버$it").getOrThrow() }
            assertTrue(repository.addMember(youtube, "열번째").isFailure)
            assertEquals(9, members(youtube).size)
        }

    @Test
    fun `이름을 바꾸고 지운 멤버는 빠진다`() =
        runTest {
            val youtube = youtube()
            val minji = repository.addMember(youtube, "민지").getOrThrow()
            val junho = repository.addMember(youtube, "준호").getOrThrow()

            repository.renameMember(minji, "민지언니").getOrThrow()
            repository.removeMember(junho).getOrThrow()

            assertEquals(listOf("민지언니"), members(youtube).map { it.name })
            assertTrue(repository.renameMember(junho, "준호").isFailure)
        }

    @Test
    fun `직접 입력은 모든 멤버 금액이 있고 합이 구독 금액 이하일 때만 저장한다`() =
        runTest {
            val youtube = youtube()
            val minji = repository.addMember(youtube, "민지").getOrThrow()
            val junho = repository.addMember(youtube, "준호").getOrThrow()

            assertTrue(repository.setSplit(youtube, PartySplitMode.CUSTOM, mapOf(minji to 5_000)).isFailure)
            assertTrue(
                repository.setSplit(youtube, PartySplitMode.CUSTOM, mapOf(minji to 10_000, junho to 5_000)).isFailure,
            )
            repository.setSplit(youtube, PartySplitMode.CUSTOM, mapOf(minji to 5_000, junho to 4_000)).getOrThrow()

            assertEquals(PartySplitMode.CUSTOM, record(youtube).subscription.partySplitMode)
            assertEquals(listOf(5_000L, 4_000L), members(youtube).map { it.shareAmountMinor })
            // 직접 입력 중에 추가한 멤버는 0원
            repository.addMember(youtube, "서연").getOrThrow()
            assertEquals(0L, members(youtube).last().shareAmountMinor)
        }

    @Test
    fun `균등으로 바꾸면 저장한 금액을 지운다`() =
        runTest {
            val youtube = youtube()
            val minji = repository.addMember(youtube, "민지").getOrThrow()
            repository.setSplit(youtube, PartySplitMode.CUSTOM, mapOf(minji to 5_000)).getOrThrow()

            repository.setSplit(youtube, PartySplitMode.EQUAL).getOrThrow()

            assertEquals(PartySplitMode.EQUAL, record(youtube).subscription.partySplitMode)
            assertEquals(listOf<Long?>(null), members(youtube).map { it.shareAmountMinor })
        }

    @Test
    fun `받음 체크 · 해제하고 지운 멤버의 정산은 보이지 않는다`() =
        runTest {
            val youtube = youtube()
            val minji = repository.addMember(youtube, "민지").getOrThrow()
            val junho = repository.addMember(youtube, "준호").getOrThrow()
            val october = YearMonth.of(2026, 10)

            repository.setSettled(minji, october, settled = true).getOrThrow()
            repository.setSettled(junho, october, settled = true).getOrThrow()
            repository.setSettled(minji, october, settled = true).getOrThrow()
            repository.setSettled(junho, october, settled = false).getOrThrow()
            repository.setSettled(junho, YearMonth.of(2026, 9), settled = true).getOrThrow()
            repository.removeMember(junho).getOrThrow()

            assertEquals(
                listOf(PartySettlement(minji, october, clock.millis())),
                repository.observeSettlements().first(),
            )
        }

    @Test
    fun `지운 구독의 멤버 · 정산은 나오지 않는다`() =
        runTest {
            val youtube = youtube()
            val minji = repository.addMember(youtube, "민지").getOrThrow()
            repository.setSettled(minji, YearMonth.of(2026, 10), settled = true).getOrThrow()

            subscriptions.delete(youtube).getOrThrow()

            assertTrue(repository.observeSettlements().first().isEmpty())
            assertTrue(repository.addMember(youtube, "준호").isFailure)
        }
}
