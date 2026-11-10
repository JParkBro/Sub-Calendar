package jpark.bro.subcal.core.domain.reminder

import jpark.bro.subcal.core.domain.billing.cancelled
import jpark.bro.subcal.core.domain.billing.date
import jpark.bro.subcal.core.domain.billing.pause
import jpark.bro.subcal.core.domain.billing.priceChange
import jpark.bro.subcal.core.domain.billing.record
import jpark.bro.subcal.core.domain.billing.subscription
import jpark.bro.subcal.core.domain.billing.trial
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentRemindersTest {
    private val settings = NotificationSettings()

    private fun reminders(
        today: String,
        vararg records: SubscriptionRecord,
        settings: NotificationSettings = this.settings,
    ): List<String> =
        paymentReminders(records.toList(), settings, date(today)).map {
            "${it.payment.subscription.name} ${it.payment.date} ${it.daysBefore}"
        }

    private fun Subscription.custom(days: Int): Subscription =
        copy(reminderMode = ReminderMode.CUSTOM, reminderDaysBefore = days)

    @Test
    fun `기본은 전역 설정의 1일 전에 알린다`() {
        val record = subscription(name = "넷플릭스", first = "2026-01-14").record()

        assertEquals(listOf("넷플릭스 2026-11-14 1"), reminders("2026-11-13", record))
        assertTrue(reminders("2026-11-14", record).isEmpty())
    }

    @Test
    fun `전역 설정이 당일이면 결제일에 알린다`() {
        val record = subscription(name = "넷플릭스", first = "2026-01-14").record()

        assertEquals(
            listOf("넷플릭스 2026-11-14 0"),
            reminders("2026-11-14", record, settings = settings.copy(defaultDaysBefore = 0)),
        )
    }

    @Test
    fun `매년 결제는 기본이면 전역 설정과 상관없이 7일 전에 알린다`() {
        val yearly = subscription(name = "구글 원", unit = CycleUnit.YEAR, first = "2025-11-20")

        assertEquals(YEARLY_DEFAULT_DAYS_BEFORE, yearly.reminderLeadDays(settings.copy(defaultDaysBefore = 3)))
        assertEquals(listOf("구글 원 2026-11-20 7"), reminders("2026-11-13", yearly.record()))
        assertTrue(reminders("2026-11-19", yearly.record()).isEmpty())
    }

    @Test
    fun `직접 고른 시점은 매년 결제에도 그대로 쓴다`() {
        val yearly = subscription(name = "구글 원", unit = CycleUnit.YEAR, first = "2025-11-20").custom(days = 3)

        assertEquals(listOf("구글 원 2026-11-20 3"), reminders("2026-11-17", yearly.record()))
    }

    @Test
    fun `알림을 끈 구독과 결제 알림 전체를 끈 경우에는 보내지 않는다`() {
        val off = subscription(first = "2026-01-14").copy(reminderMode = ReminderMode.OFF)
        val on = subscription(first = "2026-01-14")

        assertNull(off.reminderLeadDays(settings))
        assertTrue(reminders("2026-11-13", off.record()).isEmpty())
        assertTrue(reminders("2026-11-13", on.record(), settings = settings.copy(paymentEnabled = false)).isEmpty())
    }

    @Test
    fun `31일 결제는 30일인 달에 당겨진 결제일 기준으로 알린다`() {
        val record = subscription(name = "유튜브", first = "2026-01-31").record()

        assertEquals(listOf("유튜브 2026-11-30 1"), reminders("2026-11-29", record))
        assertTrue(reminders("2026-11-30", record).isEmpty())
    }

    @Test
    fun `2월 29일 매년 결제는 평년에 2월 28일의 7일 전에 알린다`() {
        val record = subscription(name = "연간", unit = CycleUnit.YEAR, first = "2024-02-29").record()

        assertEquals(listOf("연간 2027-02-28 7"), reminders("2027-02-21", record))
    }

    @Test
    fun `일시정지 기간의 결제는 알리지 않고 끝난 다음 결제부터 알린다`() {
        val record =
            subscription(name = "티빙", first = "2026-01-14")
                .record(pauses = listOf(pause(start = "2026-10-01", end = "2026-11-14")))
        val openPause = subscription(name = "웨이브", first = "2026-01-14").record(pauses = listOf(pause("2026-10-01")))

        assertTrue(reminders("2026-11-13", record, openPause).isEmpty())
        assertEquals(listOf("티빙 2026-12-14 1"), reminders("2026-12-13", record, openPause))
    }

    @Test
    fun `해지일 당일 결제는 알리지 않는다`() {
        val record = subscription(first = "2026-01-14").cancelled(on = "2026-11-14").record()

        assertTrue(reminders("2026-11-13", record).isEmpty())
    }

    @Test
    fun `가격 변경일 당일 결제는 바뀐 금액으로 알린다`() {
        val record =
            subscription(first = "2026-01-14", amount = 13_500)
                .record(priceChanges = listOf(priceChange(effectiveFrom = "2026-11-14", amount = 17_000)))

        val reminder = paymentReminders(listOf(record), settings, date("2026-11-13")).single()

        assertEquals(17_000L, reminder.payment.amount.amountMinor)
    }

    @Test
    fun `체험 중인 구독은 첫 결제일을 일반 결제처럼 알리고 체험 기간에는 알리지 않는다`() {
        val record =
            subscription(name = "디즈니+", first = "2026-11-14")
                .trial(startedOn = "2026-10-15", outcome = TrialOutcome.PENDING)
                .record()

        assertEquals(listOf("디즈니+ 2026-11-14 1"), reminders("2026-11-13", record))
        assertTrue(reminders("2026-10-30", record).isEmpty())
    }

    @Test
    fun `결제일이 빠른 순, 같은 날이면 이름순`() {
        val later = subscription(name = "가", unit = CycleUnit.YEAR, first = "2025-11-20").record()
        val b = subscription(name = "다", first = "2026-01-14").record()
        val a = subscription(name = "나", first = "2026-01-14").record()

        assertEquals(
            listOf("나 2026-11-14 1", "다 2026-11-14 1", "가 2026-11-20 7"),
            reminders("2026-11-13", later, b, a),
        )
    }
}
