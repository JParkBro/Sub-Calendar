package jpark.bro.subcal.core.domain.trial

import jpark.bro.subcal.core.domain.billing.cancelled
import jpark.bro.subcal.core.domain.billing.date
import jpark.bro.subcal.core.domain.billing.record
import jpark.bro.subcal.core.domain.billing.subscription
import jpark.bro.subcal.core.domain.billing.trial
import jpark.bro.subcal.core.model.TrialOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** product-spec 9장 샘플 (오늘 = 2026.10.30) */
class TrialsTest {
    private val today = date("2026-10-30")

    private val disney =
        subscription(
            name = "디즈니+",
            first = "2026-10-31",
        ).trial("2026-10-01", TrialOutcome.PENDING).record()
    private val millie =
        subscription(
            name = "밀리의 서재",
            first = "2026-11-12",
        ).trial("2026-10-13", TrialOutcome.PENDING).record()
    private val spotify =
        subscription(
            name = "스포티파이",
            first = "2026-10-30",
        ).trial("2026-09-30", TrialOutcome.KEPT).record()
    private val watcha =
        subscription(name = "왓챠", first = "2026-08-31")
            .trial("2026-08-01", TrialOutcome.CANCELLED)
            .cancelled(on = "2026-08-30")
            .record()
    private val unresolved =
        subscription(
            name = "플로",
            first = "2026-10-29",
        ).trial("2026-09-29", TrialOutcome.PENDING).record()
    private val records = listOf(millie, spotify, disney, watcha, unresolved)

    @Test
    fun `진행 중인 체험은 첫 결제일 당일까지이고 종료 가까운 순이다`() {
        assertEquals(listOf("디즈니+", "밀리의 서재"), activeTrials(records, today).map { it.subscription.name })
        // 첫 결제일 당일(10/31)까지는 진행 중
        assertEquals(listOf("디즈니+", "밀리의 서재"), activeTrials(records, date("2026-10-31")).map { it.subscription.name })
    }

    @Test
    fun `종료 임박은 3일 이내 체험만이다`() {
        assertEquals(listOf("디즈니+"), trialsEndingSoon(records, today).map { it.subscription.name })
        assertEquals(
            listOf("밀리의 서재"),
            trialsEndingSoon(listOf(millie), date("2026-11-09")).map { it.subscription.name },
        )
        assertTrue(trialsEndingSoon(listOf(millie), date("2026-11-08")).isEmpty())
    }

    @Test
    fun `첫 결제일 다음 날부터 결과를 고르지 않은 체험은 결제됐나요에 나온다`() {
        assertEquals(listOf("플로"), unresolvedTrials(records, today).map { it.subscription.name })
        assertTrue(unresolvedTrials(listOf(disney), date("2026-10-31")).isEmpty())
        assertEquals(listOf("디즈니+"), unresolvedTrials(listOf(disney), date("2026-11-01")).map { it.subscription.name })
    }

    @Test
    fun `지난 체험은 결과를 고른 체험 중 최근 90일, 최근 순이다`() {
        assertEquals(listOf("스포티파이", "왓챠"), endedTrials(records, today).map { it.subscription.name })
        // 왓챠 체험 종료(해지일 8/30)부터 90일이 지나면 빠진다
        assertEquals(listOf("왓챠"), endedTrials(listOf(watcha), date("2026-11-28")).map { it.subscription.name })
        assertTrue(endedTrials(listOf(watcha), date("2026-11-29")).isEmpty())
    }

    @Test
    fun `체험이 끝난 날은 첫 결제일 전 해지면 해지일이다`() {
        assertEquals(date("2026-08-30"), watcha.trialEndedOn())
        assertEquals(date("2026-10-30"), spotify.trialEndedOn())
    }

    @Test
    fun `진행 막대는 시작일부터 첫 결제일까지의 비율이다`() {
        // 디즈니+: 10/1 ~ 10/31 (30일) 중 29일
        assertEquals(29f / 30f, disney.trialProgress(today), 0.0001f)
        assertEquals(0f, disney.trialProgress(date("2026-09-01")), 0f)
        assertEquals(1f, disney.trialProgress(date("2026-11-05")), 0f)
    }

    @Test
    fun `해지했어요의 해지일은 오늘이고 첫 결제일이 지났으면 첫 결제일이다`() {
        assertEquals(today, disney.trialCancelDate(today))
        assertEquals(date("2026-10-31"), disney.trialCancelDate(date("2026-10-31")))
        assertEquals(date("2026-10-29"), unresolved.trialCancelDate(today))
    }
}
