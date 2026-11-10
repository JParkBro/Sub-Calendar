package jpark.bro.subcal.feature.subscription.impl.edit

import app.cash.turbine.test
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.testing.FakePresetRepository
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import jpark.bro.subcal.feature.subscription.api.SubscriptionEditKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** product-spec 9장 샘플 (오늘 = 2026.10.30) */
class SubscriptionEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val presets = FakePresetRepository()
    private val settings = FakeSettingsRepository()

    private fun viewModel(key: SubscriptionEditKey = SubscriptionEditKey()) =
        SubscriptionEditViewModel(key, subscriptions, presets, settings, SampleData.clock)

    private fun SubscriptionEditViewModel.fill(
        name: String = "헬스장",
        amount: String = "60000",
    ) {
        onAction(SubscriptionEditAction.NameChanged(name))
        onAction(SubscriptionEditAction.AmountChanged(amount))
    }

    @Test
    fun `프리셋으로 들어오면 이름 · 카테고리 · 색 · 첫 요금제를 채운다`() {
        val state = viewModel(SubscriptionEditKey(presetId = "netflix")).state.value

        assertFalse(state.isLoading)
        assertFalse(state.isEditMode)
        assertEquals("넷플릭스", state.name)
        assertEquals(Category.VIDEO, state.category)
        assertEquals("RED", state.colorKey)
        assertEquals("https://example.com/netflix/cancel", state.cancelUrl)
        assertEquals("2026.10", state.pricesAsOf)
        assertEquals(0, state.selectedPlanIndex)
        assertEquals("7000", state.amountInput)
        assertEquals(SampleData.today, state.firstBillingDate)
        assertTrue(state.canSave)
    }

    @Test
    fun `요금제를 고르면 금액 · 통화 · 주기가 바뀐다`() {
        val viewModel = viewModel(SubscriptionEditKey(presetId = "netflix", planIndex = 2))
        assertEquals("17000", viewModel.state.value.amountInput)

        viewModel.onAction(SubscriptionEditAction.PlanSelected(1))

        assertEquals(1, viewModel.state.value.selectedPlanIndex)
        assertEquals("13500", viewModel.state.value.amountInput)
    }

    @Test
    fun `달러 요금제는 통화도 달러로`() {
        val state = viewModel(SubscriptionEditKey(presetId = "chatgpt_plus")).state.value

        assertEquals(Currency.USD, state.currency)
        assertEquals("20", state.amountInput)
    }

    @Test
    fun `직접 입력은 이름과 금액이 있어야 저장할 수 있다`() {
        val viewModel = viewModel()
        assertFalse(viewModel.state.value.canSave)

        viewModel.onAction(SubscriptionEditAction.NameChanged("  "))
        viewModel.onAction(SubscriptionEditAction.AmountChanged("60000"))
        assertFalse(viewModel.state.value.canSave)

        viewModel.onAction(SubscriptionEditAction.NameChanged("헬스장"))
        viewModel.onAction(SubscriptionEditAction.AmountChanged("0"))
        assertFalse(viewModel.state.value.canSave)

        viewModel.onAction(SubscriptionEditAction.AmountChanged("60000"))
        assertTrue(viewModel.state.value.canSave)
    }

    @Test
    fun `이름은 30자까지만 받는다`() {
        val viewModel = viewModel()

        viewModel.onAction(SubscriptionEditAction.NameChanged("가".repeat(31)))

        assertEquals(30, viewModel.state.value.name.length)
    }

    @Test
    fun `직접 주기는 간격 2~99만 저장할 수 있다`() {
        val viewModel = viewModel()
        viewModel.fill()
        viewModel.onAction(SubscriptionEditAction.CycleOptionSelected(CycleOption.CUSTOM))
        viewModel.onAction(SubscriptionEditAction.CustomIntervalChanged("1"))
        assertFalse(viewModel.state.value.canSave)

        viewModel.onAction(SubscriptionEditAction.CustomIntervalChanged("3"))
        viewModel.onAction(SubscriptionEditAction.CustomUnitSelected(CycleUnit.MONTH))

        assertTrue(viewModel.state.value.canSave)
        assertEquals(BillingCycle(CycleUnit.MONTH, 3), viewModel.state.value.billingCycle())
    }

    @Test
    fun `과거 첫 결제일을 고르면 오늘까지 결제 횟수를 보여준다`() {
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.onAction(SubscriptionEditAction.DateClicked(DateTarget.FIRST_BILLING))
        viewModel.onAction(SubscriptionEditAction.DatePicked(LocalDate.of(2024, 7, 2)))

        assertNull(viewModel.state.value.datePicker)
        assertEquals(LocalDate.of(2024, 7, 2), viewModel.state.value.firstBillingDate)
        // 넷플릭스와 같은 2024.07.02부터 매월: 28회 (9장)
        assertEquals(28, viewModel.state.value.pastPaymentCount)
    }

    @Test
    fun `첫 결제일이 오늘이면 지난 결제 횟수를 보여주지 않는다`() {
        val viewModel = viewModel()
        viewModel.fill()

        assertEquals(0, viewModel.state.value.pastPaymentCount)
    }

    @Test
    fun `무료체험을 켜면 오늘 시작 · 한 달 뒤 종료`() {
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.onAction(SubscriptionEditAction.TrialToggled(true))

        assertEquals(SampleData.today, viewModel.state.value.trialStartedOn)
        assertEquals(LocalDate.of(2026, 11, 30), viewModel.state.value.firstBillingDate)
        assertTrue(viewModel.state.value.canSave)
    }

    @Test
    fun `체험 시작일이 종료일보다 늦으면 저장할 수 없다`() {
        val viewModel = viewModel()
        viewModel.fill()
        viewModel.onAction(SubscriptionEditAction.TrialToggled(true))

        viewModel.onAction(SubscriptionEditAction.DateClicked(DateTarget.TRIAL_START))
        viewModel.onAction(SubscriptionEditAction.DatePicked(LocalDate.of(2026, 11, 30)))

        assertFalse(viewModel.state.value.canSave)
    }

    @Test
    fun `무료체험으로 시작하는 키`() {
        val state = viewModel(SubscriptionEditKey(presetId = "disney_plus", startAsTrial = true)).state.value

        assertEquals(SampleData.today, state.trialStartedOn)
        assertEquals(LocalDate.of(2026, 11, 30), state.firstBillingDate)
    }

    @Test
    fun `결제 수단은 기본값을 채우고 다른 구독에서 쓴 값을 추천한다`() {
        settings.appSettings.value = AppSettings(defaultPaymentMethod = "신한카드")
        subscriptions.records.value =
            SampleData.records.mapIndexed { index, record ->
                val method = listOf("신한카드", "카카오페이", "현대카드")[index % 3]
                record.copy(subscription = record.subscription.copy(paymentMethod = method))
            }
        val viewModel = viewModel()
        assertEquals("신한카드", viewModel.state.value.paymentMethod)
        // 입력과 같은 값은 추천하지 않는다
        assertEquals(emptyList<String>(), viewModel.state.value.paymentMethodSuggestions)

        viewModel.onAction(SubscriptionEditAction.PaymentMethodChanged(""))
        assertEquals(listOf("신한카드", "카카오페이", "현대카드"), viewModel.state.value.paymentMethodSuggestions)

        viewModel.onAction(SubscriptionEditAction.PaymentMethodChanged("카드"))
        assertEquals(listOf("신한카드", "현대카드"), viewModel.state.value.paymentMethodSuggestions)
    }

    @Test
    fun `추가하면 입력한 값으로 저장하고 알린다`() =
        runTest {
            val viewModel = viewModel(SubscriptionEditKey(presetId = "spotify"))
            viewModel.onAction(SubscriptionEditAction.ReminderSelected(ReminderOption.THREE_DAYS))
            viewModel.onAction(SubscriptionEditAction.PaymentMethodChanged(" 신한카드 "))

            viewModel.event.test {
                viewModel.onAction(SubscriptionEditAction.SaveClicked)
                // 새 구독이라 화면이 알림 권한을 묻고 돌아간다 (product-spec 5장)
                assertEquals(SubscriptionEditEvent.Saved(isNew = true), awaitItem())
            }

            val added =
                subscriptions.records.value
                    .last()
                    .subscription
            assertEquals("스포티파이", added.name)
            assertEquals("spotify", added.presetId)
            assertEquals("프리미엄", added.planName)
            assertEquals(Money(10_900, Currency.KRW), added.amount)
            assertEquals(BillingCycle(CycleUnit.MONTH), added.cycle)
            assertEquals("신한카드", added.paymentMethod)
            assertEquals(ReminderMode.CUSTOM, added.reminderMode)
            assertEquals(3, added.reminderDaysBefore)
        }

    @Test
    fun `저장에 실패하면 알리고 다시 저장할 수 있다`() =
        runTest {
            subscriptions.failure = IllegalStateException("db")
            val viewModel = viewModel(SubscriptionEditKey(presetId = "spotify"))

            viewModel.event.test {
                viewModel.onAction(SubscriptionEditAction.SaveClicked)
                assertEquals(SubscriptionEditEvent.SaveFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isSaving)
            assertTrue(viewModel.state.value.canSave)
        }

    @Test
    fun `수정은 저장된 값과 오늘 유효한 금액을 채운다`() {
        val state = viewModel(SubscriptionEditKey(id = "netflix")).state.value

        assertTrue(state.isEditMode)
        assertEquals("넷플릭스 프리미엄", state.name)
        // 2026.08.02에 13,500 → 17,000 인상
        assertEquals("17000", state.amountInput)
        assertEquals(LocalDate.of(2024, 7, 2), state.firstBillingDate)
        assertEquals(28, state.pastPaymentCount)
        assertEquals(3, state.plans.size)
        assertTrue(state.canSave)
    }

    @Test
    fun `금액을 바꾸지 않으면 바로 저장한다`() =
        runTest {
            val viewModel = viewModel(SubscriptionEditKey(id = "netflix"))
            viewModel.onAction(SubscriptionEditAction.MemoChanged("가족 공유"))

            viewModel.event.test {
                viewModel.onAction(SubscriptionEditAction.SaveClicked)
                assertEquals(SubscriptionEditEvent.Saved(isNew = false), awaitItem())
            }
            assertNull(viewModel.state.value.sheet)
            assertEquals(
                "가족 공유",
                subscriptions.records.value
                    .first { it.subscription.id == "netflix" }
                    .subscription.memo,
            )
        }

    @Test
    fun `금액을 바꾸면 적용 시점을 묻고 고른 시점으로 저장한다`() =
        runTest {
            val viewModel = viewModel(SubscriptionEditKey(id = "netflix"))
            viewModel.onAction(SubscriptionEditAction.AmountChanged("19000"))

            viewModel.onAction(SubscriptionEditAction.SaveClicked)
            assertEquals(EditSheet.PRICE_CHANGE, viewModel.state.value.sheet)

            viewModel.event.test {
                viewModel.onAction(SubscriptionEditAction.PriceChangeTimingSelected(PriceChangeTiming.NEXT_PAYMENT))
                assertEquals(SubscriptionEditEvent.Saved(isNew = false), awaitItem())
            }
            assertNull(viewModel.state.value.sheet)
            assertEquals(PriceChangeTiming.NEXT_PAYMENT, subscriptions.lastUpdateTiming)
        }

    @Test
    fun `통화를 바꾸면 묻지 않고 저장한다`() =
        runTest {
            val viewModel = viewModel(SubscriptionEditKey(id = "netflix"))
            viewModel.onAction(SubscriptionEditAction.CurrencyChanged(Currency.USD))
            viewModel.onAction(SubscriptionEditAction.AmountChanged("12.99"))

            viewModel.event.test {
                viewModel.onAction(SubscriptionEditAction.SaveClicked)
                assertEquals(SubscriptionEditEvent.Saved(isNew = false), awaitItem())
            }
            assertNull(viewModel.state.value.sheet)
        }

    @Test
    fun `삭제를 확인하면 지우고 알린다`() =
        runTest {
            val viewModel = viewModel(SubscriptionEditKey(id = "netflix"))

            viewModel.onAction(SubscriptionEditAction.DeleteClicked)
            assertEquals(EditSheet.DELETE, viewModel.state.value.sheet)

            viewModel.event.test {
                viewModel.onAction(SubscriptionEditAction.DeleteConfirmed)
                assertEquals(SubscriptionEditEvent.Deleted, awaitItem())
            }
            assertFalse(subscriptions.records.value.any { it.subscription.id == "netflix" })
        }

    @Test
    fun `삭제에 실패하면 알린다`() =
        runTest {
            val viewModel = viewModel(SubscriptionEditKey(id = "netflix"))
            subscriptions.failure = IllegalStateException("db")

            viewModel.event.test {
                viewModel.onAction(SubscriptionEditAction.DeleteConfirmed)
                assertEquals(SubscriptionEditEvent.DeleteFailed, awaitItem())
            }
        }

    @Test
    fun `수정할 구독이 없으면 뒤로 간다`() =
        runTest {
            val viewModel = viewModel(SubscriptionEditKey(id = "missing"))

            viewModel.event.test {
                assertEquals(SubscriptionEditEvent.NavigateBack, awaitItem())
            }
        }

    @Test
    fun `프리셋을 읽지 못해도 직접 입력처럼 쓸 수 있다`() {
        presets.failure = IllegalStateException("json")

        val state = viewModel(SubscriptionEditKey(presetId = "netflix")).state.value

        assertFalse(state.isLoading)
        assertEquals("", state.name)
        assertTrue(state.plans.isEmpty())
    }
}
