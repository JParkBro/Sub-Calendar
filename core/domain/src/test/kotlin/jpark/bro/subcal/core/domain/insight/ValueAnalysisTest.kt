package jpark.bro.subcal.core.domain.insight

import jpark.bro.subcal.core.domain.billing.BillingPeriod
import jpark.bro.subcal.core.domain.billing.cancelled
import jpark.bro.subcal.core.domain.billing.date
import jpark.bro.subcal.core.domain.billing.paused
import jpark.bro.subcal.core.domain.billing.record
import jpark.bro.subcal.core.domain.billing.subscription
import jpark.bro.subcal.core.domain.billing.trial
import jpark.bro.subcal.core.domain.billing.usd
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.TrialOutcome
import jpark.bro.subcal.core.model.UsageLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/** product-spec 9장 샘플 (오늘 = 2026.10.30, 월 시작 기준일 1일, 환율 1,400원) */
class ValueAnalysisTest {
    private val today = date("2026-10-30")
    private val rate = 1_400L

    private fun tracked(
        name: String,
        amount: Long,
        first: String = "2025-01-14",
    ): Subscription = subscription(name = name, amount = amount, first = first).copy(trackUsage = true)

    private fun logs(
        name: String,
        vararg days: String,
    ): List<UsageLog> = days.mapIndexed { index, day -> UsageLog("$name-$index", name, date(day), createdAt = 0) }

    private fun times(
        name: String,
        count: Int,
        day: String = "2026-10-10",
    ): List<UsageLog> = logs(name, *Array(count) { day })

    @Test
    fun `샘플 10월 가성비는 사용 안 함이 먼저이고 1회당 비용 큰 순이다`() {
        val records =
            listOf(
                tracked("넷플릭스 프리미엄", 17_000).record(),
                tracked("쿠팡 와우", 7_890).record(),
                tracked("유튜브 프리미엄", 14_900).record().copy(
                    partyMembers =
                        listOf("민지", "준호", "서연").map { PartyMember(it, "유튜브 프리미엄", it, null, 0, 0, 0) },
                ),
                tracked("티빙", 10_900).record(),
            )
        val usage = times("넷플릭스 프리미엄", 2) + times("쿠팡 와우", 15) + times("유튜브 프리미엄", 20)

        val values = valueAnalysis(records, usage, today, monthStartDay = 1, usdKrwRate = rate)

        assertEquals(listOf("티빙", "넷플릭스 프리미엄", "쿠팡 와우", "유튜브 프리미엄"), values.map { it.record.subscription.name })
        // 유튜브 프리미엄은 파티 4인 내 몫 기준: 3,725 ÷ 20 = 186
        assertEquals(listOf(null, 8_500L, 526L, 186L), values.map { it.costPerUseKrw })
        assertTrue(values.first().isUnused)
        assertEquals(10_900, values.first().monthlyKrw)
    }

    @Test
    fun `1회당 비용은 원 단위 반올림이다`() {
        val record = tracked("구독", 10_000).record()

        assertEquals(3_333L, record.usageValue(times("구독", 3), today, 1, rate).costPerUseKrw)
        assertEquals(1_667L, record.usageValue(times("구독", 6), today, 1, rate).costPerUseKrw)
    }

    @Test
    fun `달러 · 연간 구독은 원화 월 환산으로 나눈다`() {
        val chatGpt = tracked("ChatGPT", 2_000).usd().record()
        val yearly = tracked("구글 원", 44_400).copy(cycle = subscription(unit = CycleUnit.YEAR).cycle).record()

        assertEquals(14_000L, chatGpt.usageValue(times("ChatGPT", 2), today, 1, rate).costPerUseKrw)
        assertEquals(3_700L, yearly.usageValue(times("구글 원", 1), today, 1, rate).costPerUseKrw)
    }

    @Test
    fun `월 시작 기준일이 25일이면 25일부터 다음 달 24일까지 센다`() {
        val usage = logs("구독", "2026-10-24", "2026-10-25", "2026-10-30", "2026-11-24", "2026-11-25")

        assertEquals(3, usageCount("구독", usage, BillingPeriod(YearMonth.of(2026, 10), startDay = 25)))
        assertEquals(1, usageCount("구독", usage, BillingPeriod.containing(date("2026-10-24"), 25)))
    }

    @Test
    fun `월 시작 기준일이 1일이면 달의 첫날부터 마지막 날까지 센다`() {
        val usage = logs("구독", "2026-09-30", "2026-10-01", "2026-10-31", "2026-11-01")

        assertEquals(2, usageCount("구독", usage, BillingPeriod.containing(today, 1)))
    }

    @Test
    fun `다른 구독의 기록은 세지 않는다`() {
        val record = tracked("넷플릭스", 17_000).record()

        val value = record.usageValue(times("티빙", 5), today, 1, rate)

        assertEquals(0, value.useCount)
        assertNull(value.costPerUseKrw)
    }

    @Test
    fun `사용 체크를 끈 구독 · 일시정지 · 해지 · 체험 결과를 기다리는 구독은 빠진다`() {
        val records =
            listOf(
                subscription(name = "끔", amount = 10_000).record(),
                tracked("일시정지", 10_000).paused().record(),
                tracked("해지", 10_000).cancelled("2026-10-01").record(),
                tracked("체험 중", 10_000, first = "2026-11-12").trial("2026-10-13", TrialOutcome.PENDING).record(),
                tracked("체험 후 계속", 10_000, first = "2026-10-01").trial("2026-09-01", TrialOutcome.KEPT).record(),
            )

        val values = valueAnalysis(records, emptyList(), today, 1, rate)

        assertEquals(listOf("체험 후 계속"), values.map { it.record.subscription.name })
    }

    @Test
    fun `사용 안 함이 여럿이면 월 환산 큰 순이다`() {
        val records = listOf(tracked("작은", 5_000).record(), tracked("큰", 20_000).record())

        val values = valueAnalysis(records, emptyList(), today, 1, rate)

        assertEquals(listOf("큰", "작은"), values.map { it.record.subscription.name })
    }
}
