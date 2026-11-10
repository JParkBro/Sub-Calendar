package jpark.bro.subcal.feature.insight.impl.yearreport

import app.cash.turbine.test
import jpark.bro.subcal.core.domain.insight.TopCategory
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import jpark.bro.subcal.feature.insight.api.YearReportKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

/** product-spec 9장 샘플 (오늘 = 2026.10.30, 환율 1,400원) */
class YearReportViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val settings = FakeSettingsRepository()

    private fun viewModel(year: Int = 2026) =
        YearReportViewModel(YearReportKey(year), subscriptions, settings, SampleData.clock)

    @Test
    fun `올해 리포트는 내 몫 기준 총 지출 · 카테고리 1위 · 가장 오래 쓴 구독 · 아낀 돈`() {
        val state = viewModel().state.value
        val content = requireNotNull(state.content)

        assertFalse(state.isLoading)
        assertEquals(listOf(2025, 2026), state.years)
        assertEquals(635_780, content.totalKrw)
        assertEquals(52_982, content.monthlyAverageKrw)
        assertEquals(TopCategory(Category.VIDEO, 333_200, 52, 3), content.topCategory)
        assertEquals("구글 원 200GB", content.longest?.name)
        assertEquals(YearMonth.of(2023, 11), content.longest?.since)
        assertEquals(4, content.longest?.yearCount)
        assertEquals(39_500, content.savedKrw)
        assertEquals(1, content.savedCount)
    }

    @Test
    fun `연도 칩을 바꾸면 그 해 리포트를 첫 카드부터 보여준다`() {
        val viewModel = viewModel()
        viewModel.onAction(YearReportAction.PageChanged(YearReportPage.SAVED))

        viewModel.onAction(YearReportAction.YearSelected(2025))

        assertEquals(2025, viewModel.state.value.year)
        assertEquals(YearReportPage.TOTAL, viewModel.state.value.page)
        assertEquals(
            425_090L,
            viewModel.state.value.content
                ?.totalKrw,
        )
    }

    @Test
    fun `결제도 아낀 돈도 없는 해는 비어 있고 공유하지 않는다`() =
        runTest {
            subscriptions.records.value = emptyList()
            val viewModel = viewModel()

            assertNull(viewModel.state.value.content)
            viewModel.event.test {
                viewModel.onAction(YearReportAction.ShareClicked)

                expectNoEvents()
            }
        }

    @Test
    fun `공유를 누르면 이미지 공유를, 뒤로를 누르면 뒤로 가기를 보낸다`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(YearReportAction.ShareClicked)
                assertEquals(YearReportEvent.ShareImage, awaitItem())

                viewModel.onAction(YearReportAction.BackClicked)
                assertEquals(YearReportEvent.NavigateBack, awaitItem())
            }
        }

    @Test
    fun `다른 앱에서 연 지난해보다 오래된 연도도 칩에 넣는다`() {
        assertEquals(listOf(2024, 2025, 2026), viewModel(2024).state.value.years)
    }
}
