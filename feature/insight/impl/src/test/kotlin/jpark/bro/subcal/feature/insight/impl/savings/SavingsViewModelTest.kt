package jpark.bro.subcal.feature.insight.impl.savings

import app.cash.turbine.test
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Preset
import jpark.bro.subcal.core.model.PresetCatalog
import jpark.bro.subcal.core.model.PresetPlan
import jpark.bro.subcal.core.testing.FakePresetRepository
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import jpark.bro.subcal.feature.insight.api.SavingsKey
import jpark.bro.subcal.feature.insight.api.SavingsTab
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/** product-spec 9장 샘플 (오늘 = 2026.10.30, 환율 1,400원) */
class SavingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val settings = FakeSettingsRepository()
    private var presets = FakePresetRepository()

    private fun viewModel(tab: SavingsTab = SavingsTab.CONSIDER) =
        SavingsViewModel(SavingsKey(tab), subscriptions, settings, presets, SampleData.clock)

    @Test
    fun `해지 고민함은 티빙 · 멜론과 연 261,600원, 겹치는 구독을 함께 보여준다`() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertEquals(SavingsTab.CONSIDER, state.tab)
        assertEquals(listOf("멜론", "티빙"), state.considerItems.map { it.name })
        assertEquals(261_600, state.considerAnnualKrw)
        assertEquals(listOf("스포티파이"), state.considerItems.first { it.name == "멜론" }.duplicateWith)
        assertEquals(listOf(Category.VIDEO, Category.MUSIC), state.duplicates.map { it.category })
        assertEquals(listOf(42_800L, 21_800L), state.duplicates.map { it.monthlyKrw })
        assertTrue(state.annualOptions.isEmpty())
    }

    @Test
    fun `아낀 돈 탭은 지금까지 15,800원과 해지한 구독을 보여준다`() {
        val viewModel = viewModel(SavingsTab.SAVED)

        val state = viewModel.state.value

        assertEquals(SavingsTab.SAVED, state.tab)
        assertEquals(15_800, state.savedTotalKrw)
        assertEquals(listOf(SavedListItem("watcha", "왓챠", "PINK", LocalDate.of(2026, 8, 30), 15_800)), state.savedItems)

        viewModel.onAction(SavingsAction.TabSelected(SavingsTab.CONSIDER))
        assertEquals(SavingsTab.CONSIDER, viewModel.state.value.tab)
    }

    @Test
    fun `프리셋에 같은 요금제의 연간 요금이 있으면 연간 결제 비교를 보여준다`() {
        subscriptions.records.value =
            SampleData.records.map { record ->
                if (record.subscription.id == "youtube_premium") {
                    record.copy(subscription = record.subscription.copy(planName = "개인"))
                } else {
                    record
                }
            }
        presets =
            FakePresetRepository(
                PresetCatalog(
                    version = 1,
                    pricesAsOf = "2026.10",
                    services =
                        listOf(
                            Preset(
                                "youtube_premium",
                                "유튜브 프리미엄",
                                Category.VIDEO,
                                "PINK",
                                listOf(PresetPlan("개인 연간", Money(149_000, Currency.KRW), BillingCycle(CycleUnit.YEAR))),
                            ),
                        ),
                ),
            )

        val option =
            viewModel()
                .state.value.annualOptions
                .single()

        assertEquals("유튜브 프리미엄", option.name)
        assertEquals(29_800, option.yearlySavingKrw)
    }

    @Test
    fun `프리셋을 읽지 못해도 연간 비교만 빠지고 나머지는 보여준다`() {
        presets.failure = IOException("asset")

        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertTrue(state.annualOptions.isEmpty())
        assertEquals(2, state.considerItems.size)
    }

    @Test
    fun `해지하러 가기는 해지 주소가 있으면 열고 없으면 상세로 간다`() =
        runTest {
            subscriptions.records.value =
                SampleData.records.map { record ->
                    if (record.subscription.id == "tving") {
                        record.copy(subscription = record.subscription.copy(cancelUrl = "https://example.com/tving"))
                    } else {
                        record
                    }
                }
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(SavingsAction.CancelPageClicked("tving"))
                assertEquals(SavingsEvent.OpenCancelPage("https://example.com/tving"), awaitItem())

                viewModel.onAction(SavingsAction.CancelPageClicked("melon"))
                assertEquals(SavingsEvent.OpenDetail("melon"), awaitItem())

                viewModel.onAction(SavingsAction.BackClicked)
                assertEquals(SavingsEvent.NavigateBack, awaitItem())
            }
        }
}
