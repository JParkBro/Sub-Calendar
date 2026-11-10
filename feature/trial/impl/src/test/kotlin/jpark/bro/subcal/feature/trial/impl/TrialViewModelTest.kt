package jpark.bro.subcal.feature.trial.impl

import app.cash.turbine.test
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/** product-spec 9장 샘플 (오늘 = 2026.10.30) */
class TrialViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val viewModel by lazy { TrialViewModel(subscriptions, FakeSettingsRepository(), SampleData.clock) }

    private fun subscription(id: String) =
        subscriptions.records.value
            .first { it.subscription.id == id }
            .subscription

    @Test
    fun `진행 중은 디즈니+ 내일 · 밀리의 서재 D-13, 지난 체험은 스포티파이 전환 · 왓챠 해지`() {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(listOf("disney_plus", "millie"), state.active.map { it.id })
        val disney = state.active.first()
        assertEquals(1, disney.daysUntil)
        assertTrue(disney.isUrgent)
        assertEquals(30, disney.totalDays)
        assertEquals(29, disney.elapsedDays)
        assertFalse(state.active[1].isUrgent)
        assertEquals(
            listOf(
                EndedTrial("spotify", "스포티파이", "GREEN", LocalDate.of(2026, 10, 30), cancelled = false, savedKrw = 0),
                EndedTrial("watcha", "왓챠", "PINK", LocalDate.of(2026, 8, 30), cancelled = true, savedKrw = 15_800),
            ),
            state.ended,
        )
    }

    @Test
    fun `계속 쓸래요는 체험 결과를 KEPT로 바꾸고 진행 중에서 빠진다`() {
        viewModel.onAction(TrialAction.KeepClicked("disney_plus"))

        assertEquals(TrialOutcome.KEPT, subscription("disney_plus").trialOutcome)
        assertEquals(
            listOf("millie"),
            viewModel.state.value.active
                .map { it.id },
        )
    }

    @Test
    fun `해지했어요는 오늘 날짜로 해지한다`() {
        viewModel.onAction(TrialAction.CancelledClicked("millie"))

        assertEquals(SubscriptionStatus.CANCELLED, subscription("millie").status)
        assertEquals(SampleData.today, subscription("millie").cancelledOn)
    }

    @Test
    fun `저장에 실패하면 알린다`() =
        runTest {
            subscriptions.failure = IOException("db")

            viewModel.event.test {
                viewModel.onAction(TrialAction.KeepClicked("disney_plus"))

                assertEquals(TrialEvent.ActionFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isWorking)
        }

    @Test
    fun `추가 · 상세 · 뒤로 이동`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(TrialAction.AddClicked)
                assertEquals(TrialEvent.OpenAddTrial, awaitItem())

                viewModel.onAction(TrialAction.TrialClicked("millie"))
                assertEquals(TrialEvent.OpenDetail("millie"), awaitItem())

                viewModel.onAction(TrialAction.BackClicked)
                assertEquals(TrialEvent.NavigateBack, awaitItem())
            }
        }
}
