package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentsTest {
    @Test
    fun `31일 결제는 없는 달에 말일로 당겨지고 다음 달 원래 날짜로 돌아간다`() {
        val record = subscription(first = "2026-01-31").record()

        assertEquals(
            listOf("2026-01-31", "2026-02-28", "2026-03-31", "2026-04-30", "2026-05-31"),
            record.dates("2026-01-01", "2026-06-01"),
        )
    }

    @Test
    fun `윤년 2월에는 29일로 당겨진다`() {
        val record = subscription(first = "2028-01-31").record()

        assertEquals(listOf("2028-02-29"), record.dates("2028-02-01", "2028-03-01"))
    }

    @Test
    fun `2월 29일 매년 결제는 평년에 28일이고 윤년에 29일로 돌아간다`() {
        val record = subscription(unit = CycleUnit.YEAR, first = "2024-02-29").record()

        assertEquals(
            listOf("2024-02-29", "2025-02-28", "2026-02-28", "2027-02-28", "2028-02-29"),
            record.dates("2024-01-01", "2029-01-01"),
        )
    }

    @Test
    fun `3개월마다 결제도 말일 규칙을 따른다`() {
        val record = subscription(interval = 3, first = "2026-01-31").record()

        assertEquals(
            listOf("2026-01-31", "2026-04-30", "2026-07-31", "2026-10-31"),
            record.dates("2026-01-01", "2027-01-01"),
        )
    }

    @Test
    fun `매주와 일 간격 결제`() {
        assertEquals(
            listOf("2026-01-01", "2026-01-08", "2026-01-15"),
            subscription(unit = CycleUnit.WEEK, first = "2026-01-01").record().dates("2026-01-01", "2026-01-22"),
        )
        assertEquals(
            listOf("2026-01-01", "2026-01-11", "2026-01-21"),
            subscription(unit = CycleUnit.DAY, interval = 10, first = "2026-01-01")
                .record()
                .dates("2026-01-01", "2026-01-31"),
        )
    }

    @Test
    fun `기간의 시작은 포함하고 끝은 포함하지 않는다`() {
        val record = subscription(first = "2026-01-15").record()

        assertEquals(listOf("2026-02-15"), record.dates("2026-02-15", "2026-03-15"))
    }

    @Test
    fun `첫 결제일 이전에는 결제가 없다`() {
        assertEquals(emptyList<String>(), subscription(first = "2026-03-01").record().dates("2026-01-01", "2026-03-01"))
    }

    @Test
    fun `일시정지 시작일과 끝일의 결제는 빠지고 그다음은 원래 날짜로 이어진다`() {
        val record = subscription(first = "2026-01-10").record(pauses = listOf(pause("2026-02-10", "2026-03-10")))

        assertEquals(listOf("2026-01-10", "2026-04-10"), record.dates("2026-01-01", "2026-05-01"))
    }

    @Test
    fun `일시정지 전날과 다음 날의 결제는 남는다`() {
        val record = subscription(first = "2026-01-10").record(pauses = listOf(pause("2026-02-11", "2026-03-09")))

        assertEquals(
            listOf("2026-01-10", "2026-02-10", "2026-03-10"),
            record.dates("2026-01-01", "2026-04-01"),
        )
    }

    @Test
    fun `끝나지 않은 일시정지 이후에는 결제가 없다`() {
        val record = subscription(first = "2026-01-10").record(pauses = listOf(pause("2026-02-01")))

        assertEquals(listOf("2026-01-10"), record.dates("2026-01-01", "2027-01-01"))
    }

    @Test
    fun `해지일 당일 결제는 없고 전날까지만 생긴다`() {
        val onDay = subscription(first = "2026-01-10").cancelled("2026-03-10").record()
        val dayAfter = subscription(first = "2026-01-10").cancelled("2026-03-11").record()

        assertEquals(listOf("2026-01-10", "2026-02-10"), onDay.dates("2026-01-01", "2026-12-01"))
        assertEquals(listOf("2026-01-10", "2026-02-10", "2026-03-10"), dayAfter.dates("2026-01-01", "2026-12-01"))
    }

    @Test
    fun `가격 변경일 당일부터 새 금액이고 전날은 이전 금액`() {
        val record = subscription(amount = 13_500).record(priceChanges = listOf(priceChange("2026-08-02", 17_000)))

        assertEquals(13_500, record.priceOn(date("2026-08-01")).amountMinor)
        assertEquals(17_000, record.priceOn(date("2026-08-02")).amountMinor)
    }

    @Test
    fun `여러 가격 변경은 입력 순서와 상관없이 가장 최근 것을 쓴다`() {
        val record =
            subscription(amount = 1_000).record(
                priceChanges = listOf(priceChange("2026-06-01", 3_000), priceChange("2026-03-01", 2_000)),
            )

        assertEquals(2_000, record.priceOn(date("2026-05-31")).amountMinor)
        assertEquals(3_000, record.priceOn(date("2026-06-01")).amountMinor)
    }

    @Test
    fun `다음 결제는 오늘을 포함한다`() {
        val record = subscription(first = "2026-01-10").record()

        assertEquals(date("2026-02-10"), record.nextPayment(date("2026-02-10"))?.date)
        assertEquals(date("2026-03-10"), record.nextPayment(date("2026-02-11"))?.date)
    }

    @Test
    fun `다음 결제는 끝난 일시정지 기간을 건너뛴다`() {
        val record = subscription(first = "2026-01-10").record(pauses = listOf(pause("2026-02-01", "2026-04-30")))

        assertEquals(date("2026-05-10"), record.nextPayment(date("2026-02-01"))?.date)
    }

    @Test
    fun `해지했거나 일시정지 중이면 다음 결제가 없다`() {
        val cancelled = subscription().cancelled("2026-02-15").record()
        val paused = subscription().paused().record(pauses = listOf(pause("2026-02-15")))

        assertNull(cancelled.nextPayment(date("2026-02-20")))
        assertNull(paused.nextPayment(date("2026-02-20")))
    }

    @Test
    fun `체험은 결과를 고르지 않고 첫 결제일 당일까지 대기 상태`() {
        val trial =
            subscription(first = "2026-10-31").trial("2026-10-01", TrialOutcome.PENDING)

        assertTrue(trial.isPendingTrial(date("2026-10-31")))
        assertFalse(trial.isPendingTrial(date("2026-11-01")))
    }
}
