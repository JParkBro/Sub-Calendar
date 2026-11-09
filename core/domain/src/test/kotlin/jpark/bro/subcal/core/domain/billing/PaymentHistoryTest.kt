package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentHistoryTest {
    private val today = date("2026-10-30")

    private fun List<PaymentHistoryEntry>.paid() = filterIsInstance<PaymentHistoryEntry.Paid>()

    @Test
    fun `최신순으로 오늘까지 결제하고 인상한 회차를 표시한다`() {
        // product-spec 9장 넷플릭스: 2026.08.02부터 13,500 → 17,000
        val netflix =
            subscription(amount = 13_500, first = "2024-07-02")
                .record(priceChanges = listOf(priceChange("2026-08-02", 17_000)))

        val history = netflix.paymentHistory(today).paid()

        assertEquals(28, history.size)
        assertEquals(date("2026-10-02"), history.first().date)
        assertEquals(date("2024-07-02"), history.last().date)
        assertEquals(
            listOf(PriceTrend.SAME, PriceTrend.SAME, PriceTrend.UP, PriceTrend.SAME),
            history.take(4).map { it.trend },
        )
        assertEquals(17_000, history[2].payment.amount.amountMinor)
        assertEquals(13_500, history[3].payment.amount.amountMinor)
    }

    @Test
    fun `가격이 내린 회차는 인하`() {
        val record =
            subscription(amount = 10_000, first = "2026-07-01")
                .record(priceChanges = listOf(priceChange("2026-09-01", 8_000)))

        assertEquals(
            listOf(PriceTrend.SAME, PriceTrend.DOWN, PriceTrend.SAME, PriceTrend.SAME),
            record.paymentHistory(today).paid().map { it.trend },
        )
    }

    @Test
    fun `결제일 당일도 이력에 들어간다`() {
        val history = subscription(first = "2026-08-30").record().paymentHistory(today)

        assertEquals(listOf(date("2026-10-30"), date("2026-09-30"), date("2026-08-30")), history.map { it.date })
    }

    @Test
    fun `일시정지 구간을 날짜 자리에 끼우고 그 사이 결제는 없다`() {
        val record =
            subscription(first = "2026-06-10")
                .record(pauses = listOf(pause("2026-07-01", "2026-08-31")))

        val history = record.paymentHistory(today)

        assertEquals(
            listOf(date("2026-10-10"), date("2026-09-10"), date("2026-07-01"), date("2026-06-10")),
            history.map { it.date },
        )
        assertEquals(PaymentHistoryEntry.Paused(date("2026-07-01"), date("2026-08-31")), history[2])
    }

    @Test
    fun `끝나지 않은 일시정지는 끝 날짜가 없다`() {
        val record = subscription(first = "2026-08-10").paused().record(pauses = listOf(pause("2026-10-01")))

        val history = record.paymentHistory(today)

        assertEquals(PaymentHistoryEntry.Paused(date("2026-10-01"), null), history.first())
        assertEquals(2, history.paid().size)
    }

    @Test
    fun `해지일 이후 결제는 없다`() {
        val record = subscription(first = "2026-06-15").cancelled(on = "2026-09-15").record()

        assertEquals(
            listOf(date("2026-08-15"), date("2026-07-15"), date("2026-06-15")),
            record.paymentHistory(today).map { it.date },
        )
    }

    @Test
    fun `결과를 고르지 않은 체험은 종료일 당일에도 이력이 없다`() {
        val record = subscription(first = "2026-10-30").trial("2026-09-30", TrialOutcome.PENDING).record()

        assertTrue(record.paymentHistory(today).isEmpty())
    }

    @Test
    fun `계속 쓰기로 한 체험은 종료일이 첫 결제`() {
        val record = subscription(first = "2026-10-30").trial("2026-09-30", TrialOutcome.KEPT).record()

        assertEquals(listOf(date("2026-10-30")), record.paymentHistory(today).map { it.date })
    }
}
