package jpark.bro.subcal.feature.insight.impl.stats

import app.cash.turbine.test
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/** product-spec 9장 샘플 (오늘 = 2026.10.30, 환율 1,400원) */
class StatsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val settings = FakeSettingsRepository()
    private val viewModel by lazy { StatsViewModel(subscriptions, settings, SampleData.clock) }

    private val state: StatsState get() = viewModel.state.value

    @Test
    fun `9장 샘플의 비중 · 이번 달 · 연간 · 해지 고민 · 아낀 돈`() {
        assertFalse(state.isLoading)
        assertEquals(YearMonth.of(2026, 10), state.month)
        assertNull(state.periodStart)
        assertEquals(
            listOf(Category.VIDEO, Category.MUSIC, Category.SHOPPING, Category.CLOUD),
            state.categories.map { it.category },
        )
        assertEquals(100, state.categories.sumOf { it.percent })
        assertEquals(76_190, state.monthlyAverageKrw)
        assertEquals(7, state.activeCount)
        assertEquals(72_490, state.thisPeriodKrw)
        assertEquals(10_900, state.changeKrw)
        assertEquals(914_280, state.annualKrw)
        assertEquals(2, state.considerCancelCount)
        assertEquals(261_600, state.considerCancelAnnualKrw)
        assertEquals(15_800, state.savedKrw)
    }

    @Test
    fun `최근 12개 기간을 오래된 순으로 보여주고 마지막이 이번 달이다`() {
        assertEquals(RECENT_PERIOD_COUNT, state.periods.size)
        assertEquals(
            YearMonth.of(2025, 11),
            state.periods
                .first()
                .period.month,
        )
        assertEquals(72_490, state.periods.last().totalKrw)
    }

    @Test
    fun `월 시작 기준일이 1이 아니면 기간을 함께 보여준다`() {
        settings.appSettings.value = AppSettings(monthStartDay = 25)

        assertEquals(YearMonth.of(2026, 10), state.month)
        assertEquals(LocalDate.of(2026, 10, 25), state.periodStart)
        assertEquals(LocalDate.of(2026, 11, 24), state.periodEndInclusive)
    }

    @Test
    fun `구독이 없으면 비중은 비고 금액은 0이다`() {
        subscriptions.records.value = emptyList()

        assertTrue(state.categories.isEmpty())
        assertEquals(0, state.thisPeriodKrw)
        assertEquals(0, state.savedKrw)
    }

    @Test
    fun `카테고리를 누르면 그 카테고리 목록으로 간다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(StatsAction.CategoryClicked(Category.MUSIC))

                assertEquals(StatsEvent.OpenList(Category.MUSIC), awaitItem())
            }
        }
}
