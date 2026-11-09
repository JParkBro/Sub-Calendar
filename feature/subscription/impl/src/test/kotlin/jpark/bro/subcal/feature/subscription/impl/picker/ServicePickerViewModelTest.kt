package jpark.bro.subcal.feature.subscription.impl.picker

import app.cash.turbine.test
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.testing.FakePresetRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServicePickerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val presets = FakePresetRepository()
    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val viewModel by lazy { ServicePickerViewModel(presets, subscriptions) }

    private val ServicePickerState.ids: List<String> get() = services.map { it.id }

    @Test
    fun `프리셋을 순서대로 보여주고 이용 중인 서비스에 등록됨 표시`() {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(listOf("netflix", "disney_plus", "watcha", "spotify", "chatgpt_plus"), state.ids)
        assertEquals(listOf(Category.VIDEO, Category.MUSIC, Category.AI), state.categories)
        val byId = state.services.associateBy { it.id }
        // 체험 중인 디즈니+도 이용 중이고, 해지한 왓챠는 아니다
        assertTrue(byId.getValue("netflix").isRegistered)
        assertTrue(byId.getValue("disney_plus").isRegistered)
        assertFalse(byId.getValue("watcha").isRegistered)
        assertFalse(byId.getValue("chatgpt_plus").isRegistered)
    }

    @Test
    fun `가장 싼 요금제와 요금제가 여러 개인지`() {
        val byId =
            viewModel.state.value.services
                .associateBy { it.id }

        assertEquals(Money(7_000, Currency.KRW), byId.getValue("netflix").lowestAmount)
        assertTrue(byId.getValue("netflix").hasMorePlans)
        assertEquals(Money(2_000, Currency.USD), byId.getValue("chatgpt_plus").lowestAmount)
        assertFalse(byId.getValue("chatgpt_plus").hasMorePlans)
        assertEquals(null, byId.getValue("watcha").lowestAmount)
    }

    @Test
    fun `검색은 대소문자 · 공백을 무시한다`() {
        viewModel.onAction(ServicePickerAction.QueryChanged("chat gpt"))

        assertEquals(listOf("chatgpt_plus"), viewModel.state.value.ids)
        assertEquals("chat gpt", viewModel.state.value.query)
    }

    @Test
    fun `카테고리 칩으로 거른다`() {
        viewModel.onAction(ServicePickerAction.CategorySelected(Category.VIDEO))
        assertEquals(listOf("netflix", "disney_plus", "watcha"), viewModel.state.value.ids)

        viewModel.onAction(ServicePickerAction.CategorySelected(null))
        assertEquals(5, viewModel.state.value.services.size)
    }

    @Test
    fun `프리셋을 읽지 못하면 직접 입력만 안내`() {
        presets.failure = IllegalStateException("json")

        val state = viewModel.state.value
        assertTrue(state.loadFailed)
        assertTrue(state.services.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `서비스를 고르면 그 프리셋으로, 목록에 없으면 직접 입력으로`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(ServicePickerAction.ServiceClicked("netflix"))
                assertEquals(ServicePickerEvent.OpenEdit("netflix"), awaitItem())

                viewModel.onAction(ServicePickerAction.ManualEntryClicked)
                assertEquals(ServicePickerEvent.OpenEdit(null), awaitItem())

                viewModel.onAction(ServicePickerAction.CloseClicked)
                assertEquals(ServicePickerEvent.NavigateBack, awaitItem())
            }
        }
}
