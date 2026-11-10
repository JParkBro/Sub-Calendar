package jpark.bro.subcal.core.domain.party

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.date
import jpark.bro.subcal.core.domain.billing.monthlyEquivalentKrw
import jpark.bro.subcal.core.domain.billing.pause
import jpark.bro.subcal.core.domain.billing.paused
import jpark.bro.subcal.core.domain.billing.priceChange
import jpark.bro.subcal.core.domain.billing.record
import jpark.bro.subcal.core.domain.billing.subscription
import jpark.bro.subcal.core.domain.billing.trial
import jpark.bro.subcal.core.domain.billing.usd
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/** product-spec 9장 유튜브 프리미엄 파티 (오늘 = 2026.10.30, 월 시작 기준일 1일) */
class PartyTest {
    private val today = date("2026-10-30")

    private fun member(
        name: String,
        share: Long? = null,
    ): PartyMember = PartyMember(name, "테스트", name, share, sortOrder = 0, createdAt = 0, updatedAt = 0)

    private fun Subscription.party(
        vararg members: PartyMember,
        mode: PartySplitMode = PartySplitMode.EQUAL,
    ): SubscriptionRecord = copy(partySplitMode = mode).record().copy(partyMembers = members.toList())

    private val youtube =
        subscription(name = "유튜브 프리미엄", amount = 14_900, first = "2025-01-14")
            .party(member("민지"), member("준호"), member("서연"))

    private fun krw(amount: Long) = Money(amount, Currency.KRW)

    @Test
    fun `균등 분담은 총액을 인원으로 나눈다`() {
        assertEquals(4, youtube.partySize)
        assertEquals(krw(3_725), youtube.memberShare(youtube.partyMembers[0], krw(14_900)))
        assertEquals(krw(3_725), youtube.myShare(krw(14_900)))
    }

    @Test
    fun `균등 분담에서 남는 원은 나에게 온다`() {
        val record = subscription(amount = 10_000).party(member("민지"), member("준호"))

        assertEquals(krw(3_333), record.memberShare(record.partyMembers[0], krw(10_000)))
        assertEquals(krw(3_334), record.myShare(krw(10_000)))
    }

    @Test
    fun `직접 입력은 내 몫 = 총액 − 다른 멤버 몫 합이고 넘치면 0이다`() {
        val record =
            subscription(amount = 14_900).party(member("민지", 5_000), member("준호", 4_000), mode = PartySplitMode.CUSTOM)

        assertEquals(krw(5_900), record.myShare(krw(14_900)))
        assertEquals(krw(0), record.myShare(krw(8_000)))
    }

    @Test
    fun `달러는 센트 단위로 나눈다`() {
        val record = subscription(amount = 2_000).usd().party(member("민지"), member("준호"))

        assertEquals(Money(666, Currency.USD), record.memberShare(record.partyMembers[0], Money(2_000, Currency.USD)))
        assertEquals(Money(668, Currency.USD), record.myShare(Money(2_000, Currency.USD)))
    }

    @Test
    fun `파티가 아니면 내 몫은 전체이고 그대로 돌려준다`() {
        val record = subscription(amount = 17_000).record()

        assertEquals(krw(17_000), record.myShare(krw(17_000)))
        assertEquals(record, record.toMyShare())
    }

    @Test
    fun `내 몫 구독은 가격 변경에도 분담을 적용한다`() {
        val record =
            subscription(amount = 13_500, first = "2024-07-02")
                .party(member("민지"))
                .copy(priceChanges = listOf(priceChange("2026-08-02", 17_000)))

        val mine = record.toMyShare()

        assertEquals(krw(6_750), mine.subscription.amount)
        assertEquals(8_500L, mine.priceChanges.single().amountMinor)
        assertEquals(8_500L, mine.monthlyEquivalentKrw(today, 1_400))
    }

    @Test
    fun `정산 기간은 이번 달과 지난 3개 기간 중 결제가 있는 것, 최근 순`() {
        val bills = youtube.partyBills(today, monthStartDay = 1)

        assertEquals(
            listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 9), YearMonth.of(2026, 8), YearMonth.of(2026, 7)),
            bills.map { it.period.month },
        )
        assertEquals(date("2026-10-14"), bills.first().firstPaymentDate)
        assertEquals(krw(14_900), bills.first().total)
    }

    @Test
    fun `이번 달 결제가 아직 오지 않았어도 이번 달 정산에 넣는다`() {
        // 25일 시작: 10월 기간 = 10.25 ~ 11.24, 결제는 11.14
        val bills = youtube.partyBills(today, monthStartDay = 25)

        assertEquals(BillingPeriod(YearMonth.of(2026, 10), 25), bills.first().period)
        assertEquals(date("2026-11-14"), bills.first().firstPaymentDate)
    }

    @Test
    fun `매년 결제는 결제가 있는 기간만, 매주 결제는 기간 합으로 정산한다`() {
        val yearly =
            subscription(amount = 44_400, first = "2023-11-09", unit = CycleUnit.YEAR)
                .party(member("민지"))
        val weekly =
            subscription(amount = 1_000, first = "2026-10-01", unit = CycleUnit.WEEK)
                .party(member("민지"))

        assertTrue(yearly.partyBills(today, 1).isEmpty())
        assertEquals(krw(5_000), weekly.partyBills(today, 1).single().total)
    }

    @Test
    fun `결과를 고르기 전인 체험과 일시정지 기간에는 정산할 결제가 없다`() {
        val trial =
            subscription(amount = 13_900, first = "2026-10-31")
                .trial("2026-10-01", TrialOutcome.PENDING)
                .party(member("민지"))
        val paused =
            subscription(amount = 10_000, first = "2026-01-15")
                .paused()
                .party(member("민지"))
                .copy(pauses = listOf(pause("2026-07-01")))

        assertTrue(trial.partyBills(today, 1).isEmpty())
        assertTrue(paused.partyBills(today, 1).isEmpty())
    }

    @Test
    fun `정산 알림은 결제 다음 날 미정산 멤버만 담는다`() {
        val settlements =
            listOf(
                PartySettlement("민지", YearMonth.of(2026, 11), 0),
                PartySettlement("준호", YearMonth.of(2026, 11), 0),
                PartySettlement("서연", YearMonth.of(2026, 10), 0),
            )
        val settings = NotificationSettings()

        val reminders = partyReminders(listOf(youtube), settlements, settings, 1, date("2026-11-15"))

        assertEquals(listOf("서연"), reminders.single().unsettledMembers.map { it.name })
        assertEquals(YearMonth.of(2026, 11), reminders.single().period.month)
        assertTrue(partyReminders(listOf(youtube), settlements, settings, 1, date("2026-11-14")).isEmpty())
        assertTrue(partyReminders(listOf(youtube), settlements, settings, 1, date("2026-11-16")).isEmpty())
    }

    @Test
    fun `모두 받았거나 정산 알림을 끄면 알리지 않는다`() {
        val all = youtube.partyMembers.map { PartySettlement(it.id, YearMonth.of(2026, 11), 0) }
        val off = NotificationSettings(partyEnabled = false)

        assertTrue(partyReminders(listOf(youtube), all, NotificationSettings(), 1, date("2026-11-15")).isEmpty())
        assertTrue(partyReminders(listOf(youtube), emptyList(), off, 1, date("2026-11-15")).isEmpty())
    }

    @Test
    fun `정산 알림의 기간은 월 시작 기준일을 따른다`() {
        // 25일 시작이면 11.14 결제는 10월 기간(10.25 ~ 11.24)
        val reminders = partyReminders(listOf(youtube), emptyList(), NotificationSettings(), 25, date("2026-11-15"))

        assertEquals(YearMonth.of(2026, 10), reminders.single().period.month)
    }

    @Test
    fun `받침이 있으면 아, 없으면 야`() {
        assertEquals("아", vocativeParticle("서연"))
        assertEquals("야", vocativeParticle("민지"))
        assertEquals("야", vocativeParticle("Tom"))
        assertEquals("아", vocativeParticle(" 준 "))
        assertEquals("야", vocativeParticle(""))
    }
}
