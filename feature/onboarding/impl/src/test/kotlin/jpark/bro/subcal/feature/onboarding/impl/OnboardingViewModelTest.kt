package jpark.bro.subcal.feature.onboarding.impl

import app.cash.turbine.test
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Preset
import jpark.bro.subcal.core.model.PresetPlan
import jpark.bro.subcal.core.model.SubscriptionInput
import jpark.bro.subcal.core.testing.FakePresetRepository
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

/** product-spec 5장 (오늘 = 2026.10.30) */
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** 테스트 프리셋 5개 + 매년 요금제 하나 */
    private val catalog =
        SampleData.catalog.copy(
            services =
                SampleData.catalog.services +
                    Preset(
                        id = "google_one",
                        name = "구글 원",
                        category = Category.CLOUD,
                        colorKey = "BLUE",
                        plans =
                            listOf(
                                PresetPlan("200GB 연간", Money(44_400, Currency.KRW), BillingCycle(CycleUnit.YEAR)),
                            ),
                    ),
        )
    private val presets = FakePresetRepository(catalog)
    private val subscriptions = FakeSubscriptionRepository()
    private val settings = FakeSettingsRepository()
    private val viewModel by lazy { OnboardingViewModel(presets, subscriptions, settings, SampleData.clock) }

    private val OnboardingState.ids: List<String> get() = services.map { it.id }

    @Test
    fun `요금제가 있는 프리셋만 첫 요금제 금액과 함께 보여준다`() {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        // 왓챠는 테스트 프리셋에 요금제가 없다
        assertEquals(listOf("netflix", "disney_plus", "spotify", "chatgpt_plus", "google_one"), state.ids)
        assertEquals(Money(7_000, Currency.KRW), state.services.first().amount)
        assertEquals(listOf(Category.VIDEO, Category.MUSIC, Category.CLOUD, Category.AI), state.categories)
    }

    @Test
    fun `고르면 개수와 한 달 환산 합을 보여준다`() {
        viewModel.onAction(OnboardingAction.ServiceToggled("netflix"))
        viewModel.onAction(OnboardingAction.ServiceToggled("google_one"))
        viewModel.onAction(OnboardingAction.ServiceToggled("chatgpt_plus"))

        val state = viewModel.state.value
        assertEquals(3, state.selectedCount)
        // 7,000 + 44,400 ÷ 12 + $20 × 1,400
        assertEquals(7_000 + 3_700 + 28_000L, state.monthlyTotalKrw)
        assertTrue(state.services.first { it.id == "netflix" }.isSelected)

        viewModel.onAction(OnboardingAction.ServiceToggled("chatgpt_plus"))
        assertEquals(2, viewModel.state.value.selectedCount)
    }

    @Test
    fun `검색 · 카테고리로 걸러도 고른 것은 유지된다`() {
        viewModel.onAction(OnboardingAction.ServiceToggled("spotify"))
        viewModel.onAction(OnboardingAction.CategorySelected(Category.VIDEO))
        assertEquals(listOf("netflix", "disney_plus"), viewModel.state.value.ids)

        viewModel.onAction(OnboardingAction.CategorySelected(null))
        viewModel.onAction(OnboardingAction.QueryChanged("디즈니"))
        assertEquals(listOf("disney_plus"), viewModel.state.value.ids)
        assertEquals(1, viewModel.state.value.selectedCount)
    }

    @Test
    fun `등록하면 첫 요금제 · 요금제 주기 · 오늘로 저장하고 안내 카드를 켠다`() =
        runTest {
            viewModel.onAction(OnboardingAction.ServiceToggled("netflix"))
            viewModel.onAction(OnboardingAction.ServiceToggled("google_one"))

            viewModel.event.test {
                viewModel.onAction(OnboardingAction.RegisterClicked)
                assertEquals(OnboardingEvent.Finished, awaitItem())
            }

            val saved = subscriptions.records.value.map { it.subscription }
            assertEquals(listOf("넷플릭스", "구글 원"), saved.map { it.name })
            assertEquals(listOf("광고형 스탠다드", "200GB 연간"), saved.map { it.planName })
            assertEquals(BillingCycle(CycleUnit.YEAR), saved[1].cycle)
            assertTrue(saved.all { it.firstBillingDate == SampleData.today })
            assertEquals("https://example.com/netflix/cancel", saved[0].cancelUrl)
            assertTrue(settings.onboardingDone.value)
            assertTrue(settings.billingDateHintPending.value)
        }

    @Test
    fun `건너뛰면 저장 없이 끝내고 안내 카드도 없다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(OnboardingAction.SkipClicked)
                assertEquals(OnboardingEvent.Finished, awaitItem())
            }
            assertTrue(subscriptions.records.value.isEmpty())
            assertTrue(settings.onboardingDone.value)
            assertFalse(settings.billingDateHintPending.value)
        }

    @Test
    fun `아무것도 고르지 않으면 등록하지 않는다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(OnboardingAction.RegisterClicked)
                expectNoEvents()
            }
            assertFalse(settings.onboardingDone.value)
        }

    @Test
    fun `저장에 실패하면 알리고 다시 누르면 남은 것만 저장한다`() =
        runTest {
            viewModel.onAction(OnboardingAction.ServiceToggled("netflix"))
            subscriptions.failure = IllegalStateException("db")

            viewModel.event.test {
                viewModel.onAction(OnboardingAction.RegisterClicked)
                assertEquals(OnboardingEvent.SaveFailed, awaitItem())
                assertFalse(viewModel.state.value.isSaving)

                subscriptions.failure = null
                viewModel.onAction(OnboardingAction.RegisterClicked)
                assertEquals(OnboardingEvent.Finished, awaitItem())
            }
            // 실패한 넷플릭스를 한 번만 저장했다
            assertEquals(1, subscriptions.records.value.size)
        }

    @Test
    fun `프리셋을 읽지 못하면 건너뛰기만 안내`() {
        presets.failure = IllegalStateException("json")

        val state = viewModel.state.value
        assertTrue(state.loadFailed)
        assertTrue(state.services.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `저장한 서비스는 실패 뒤에도 선택을 풀 수 없다`() =
        runTest {
            // 넷플릭스는 저장되고 스포티파이에서 실패한다
            val viewModel = OnboardingViewModel(presets, FailAfterFirst(subscriptions), settings, SampleData.clock)
            viewModel.onAction(OnboardingAction.ServiceToggled("netflix"))
            viewModel.onAction(OnboardingAction.ServiceToggled("spotify"))

            viewModel.event.test {
                viewModel.onAction(OnboardingAction.RegisterClicked)
                assertEquals(OnboardingEvent.SaveFailed, awaitItem())
            }
            viewModel.onAction(OnboardingAction.ServiceToggled("netflix"))

            assertTrue(
                viewModel.state.value.services
                    .first { it.id == "netflix" }
                    .isSelected,
            )
        }

    @Test
    fun `일부를 저장한 뒤 건너뛰면 결제일 확인 안내를 켠다`() =
        runTest {
            val viewModel = OnboardingViewModel(presets, FailAfterFirst(subscriptions), settings, SampleData.clock)
            viewModel.onAction(OnboardingAction.ServiceToggled("netflix"))
            viewModel.onAction(OnboardingAction.ServiceToggled("spotify"))

            viewModel.event.test {
                viewModel.onAction(OnboardingAction.RegisterClicked)
                assertEquals(OnboardingEvent.SaveFailed, awaitItem())

                viewModel.onAction(OnboardingAction.SkipClicked)
                assertEquals(OnboardingEvent.Finished, awaitItem())
            }
            assertEquals(1, subscriptions.records.value.size)
            assertTrue(settings.billingDateHintPending.value)
        }

    /** 첫 저장은 성공하고 그 뒤 저장은 실패한다 */
    private class FailAfterFirst(
        private val delegate: FakeSubscriptionRepository,
    ) : SubscriptionRepository by delegate {
        private var count = 0

        override suspend fun add(input: SubscriptionInput): Result<String> =
            if (count++ == 0) delegate.add(input) else Result.failure(IllegalStateException("db"))
    }
}
