package jpark.bro.subcal.feature.insight.impl.value

import app.cash.turbine.test
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.FakeUsageRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

/** product-spec 9장 샘플 (오늘 = 2026.10.30, 환율 1,400원) */
class ValueAnalysisViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val usage = FakeUsageRepository(SampleData.usageLogs)
    private val settings = FakeSettingsRepository()
    private val viewModel by lazy { ValueAnalysisViewModel(subscriptions, usage, settings, SampleData.clock) }

    private val state: ValueAnalysisState get() = viewModel.state.value

    @Test
    fun `10월 가성비는 사용 안 함 → 1회당 비용 큰 순이고 막대는 최대값 대비다`() {
        assertFalse(state.isLoading)
        assertEquals(YearMonth.of(2026, 10), state.month)
        assertEquals(listOf("tving", "netflix", "youtube_premium", "coupang_wow"), state.items.map { it.id })
        // 유튜브 프리미엄은 파티 정산 전이라 전체 금액 기준 (14,900 ÷ 20)
        assertEquals(listOf(null, 8_500L, 745L, 526L), state.items.map { it.costPerUseKrw })
        assertEquals(listOf(0, 2, 20, 15), state.items.map { it.useCount })
        assertEquals(1f, state.items[0].barFraction)
        assertEquals(1f, state.items[1].barFraction)
        assertEquals(526f / 8_500, state.items[3].barFraction)
    }

    @Test
    fun `해지 고민함에 이미 담긴 사용 안 함 구독은 제안하지 않는다`() {
        // 티빙은 샘플에서 이미 해지 고민함
        assertTrue(state.considerSuggestions.isEmpty())

        usage.logs.value = SampleData.usageLogs.filterNot { it.subscriptionId == "netflix" }

        assertEquals(listOf(ConsiderSuggestion("netflix", "넷플릭스 프리미엄", 17_000)), state.considerSuggestions)
    }

    @Test
    fun `담기를 누르면 해지 고민함에 넣고 알린다`() =
        runTest {
            usage.logs.value = emptyList()
            viewModel.event.test {
                viewModel.onAction(ValueAnalysisAction.AddToConsiderClicked("netflix"))

                assertEquals(ValueAnalysisEvent.AddedToConsider, awaitItem())
            }
            assertTrue(
                subscriptions.records.value
                    .first { it.subscription.id == "netflix" }
                    .subscription.considerCancel,
            )
            assertFalse(state.considerSuggestions.any { it.id == "netflix" })
            assertFalse(state.isWorking)
        }

    @Test
    fun `담기에 실패하면 알린다`() =
        runTest {
            subscriptions.failure = IllegalStateException("db")
            viewModel.event.test {
                viewModel.onAction(ValueAnalysisAction.AddToConsiderClicked("tving"))

                assertEquals(ValueAnalysisEvent.ActionFailed, awaitItem())
            }
        }

    @Test
    fun `월 시작 기준일이 바뀌면 그 기간의 기록만 센다`() {
        // 25일 시작: 10.25 ~ 11.24 기간에는 넷플릭스 기록(10.01 · 10.02)이 없다
        settings.appSettings.value = AppSettings(monthStartDay = 25)

        assertEquals(YearMonth.of(2026, 10), state.month)
        assertEquals(0, state.items.first { it.id == "netflix" }.useCount)
    }

    @Test
    fun `항목을 누르면 상세로, 뒤로를 누르면 뒤로 간다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(ValueAnalysisAction.SubscriptionClicked("netflix"))
                assertEquals(ValueAnalysisEvent.OpenDetail("netflix"), awaitItem())

                viewModel.onAction(ValueAnalysisAction.BackClicked)
                assertEquals(ValueAnalysisEvent.NavigateBack, awaitItem())
            }
        }
}
