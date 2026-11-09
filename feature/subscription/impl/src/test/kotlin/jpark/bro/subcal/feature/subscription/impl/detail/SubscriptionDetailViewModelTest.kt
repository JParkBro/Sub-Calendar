package jpark.bro.subcal.feature.subscription.impl.detail

import app.cash.turbine.test
import jpark.bro.subcal.core.domain.billing.PaymentHistoryEntry
import jpark.bro.subcal.core.domain.billing.PriceTrend
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** product-spec 9장 샘플 (오늘 = 2026.10.30) */
class SubscriptionDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val settings = FakeSettingsRepository()

    private fun viewModel(id: String) =
        SubscriptionDetailViewModel(SubscriptionDetailKey(id), subscriptions, settings, SampleData.clock)

    private val SubscriptionDetailViewModel.content: DetailContent
        get() = requireNotNull(state.value.content)

    private fun withCancelUrl(
        id: String,
        url: String?,
    ) {
        subscriptions.records.value =
            SampleData.records.map { record: SubscriptionRecord ->
                if (record.subscription.id == id) {
                    record.copy(subscription = record.subscription.copy(cancelUrl = url))
                } else {
                    record
                }
            }
    }

    @Test
    fun `넷플릭스는 인상 후 금액 · 누적 28회 388,500원 · 다음 결제 D-3`() {
        val content = viewModel("netflix").content

        assertEquals(DetailStatus.ACTIVE, content.status)
        assertEquals(Money(17_000, Currency.KRW), content.amount)
        assertEquals(388_500, content.cumulativeKrw)
        assertEquals(28, content.paymentCount)
        assertEquals(NextPayment(LocalDate.of(2026, 11, 2), 3), content.nextPayment)
    }

    @Test
    fun `결제 이력은 최근 12회만 보여주고 더보기가 있다`() {
        val content = viewModel("netflix").content
        val paid = content.recentHistory.filterIsInstance<PaymentHistoryEntry.Paid>()

        assertEquals(12, paid.size)
        assertEquals(LocalDate.of(2026, 10, 2), paid.first().date)
        assertEquals(LocalDate.of(2025, 11, 2), paid.last().date)
        assertEquals(PriceTrend.UP, paid[2].trend)
        assertTrue(content.hasMoreHistory)
    }

    @Test
    fun `결과를 고르기 전인 체험은 결제가 없고 체험 종료일이 다음 결제`() {
        val content = viewModel("disney_plus").content

        assertEquals(DetailStatus.TRIAL, content.status)
        assertEquals(0, content.cumulativeKrw)
        assertEquals(0, content.paymentCount)
        assertTrue(content.recentHistory.isEmpty())
        assertEquals(NextPayment(LocalDate.of(2026, 10, 31), 1), content.nextPayment)
    }

    @Test
    fun `해지한 구독은 다음 결제를 숨긴다`() {
        val content = viewModel("watcha").content

        assertEquals(DetailStatus.CANCELLED, content.status)
        assertNull(content.nextPayment)
        assertFalse(content.hasMoreHistory)
    }

    @Test
    fun `일시정지하면 상태가 바뀌고 다음 결제를 숨긴다`() {
        val viewModel = viewModel("netflix")

        viewModel.onAction(SubscriptionDetailAction.PauseClicked)

        assertEquals(DetailStatus.PAUSED, viewModel.content.status)
        assertNull(viewModel.content.nextPayment)
        assertFalse(viewModel.state.value.isWorking)

        viewModel.onAction(SubscriptionDetailAction.ResumeClicked)
        assertEquals(DetailStatus.ACTIVE, viewModel.content.status)
    }

    @Test
    fun `해지 주소가 있으면 브라우저로 열고 돌아오면 해지했나요 시트`() =
        runTest {
            withCancelUrl("netflix", "netflix.com/cancelplan")
            val viewModel = viewModel("netflix")

            viewModel.event.test {
                viewModel.onAction(SubscriptionDetailAction.CancelClicked)
                assertEquals(SubscriptionDetailEvent.OpenCancelPage("https://netflix.com/cancelplan"), awaitItem())
            }
            assertTrue(viewModel.state.value.isAwaitingCancelReturn)
            assertFalse(viewModel.state.value.isCancelSheetOpen)

            viewModel.onAction(SubscriptionDetailAction.Resumed)

            assertFalse(viewModel.state.value.isAwaitingCancelReturn)
            assertTrue(viewModel.state.value.isCancelSheetOpen)
            assertEquals(SampleData.today, viewModel.state.value.cancelDate)
        }

    @Test
    fun `해지 주소가 없으면 바로 시트`() {
        withCancelUrl("netflix", null)
        val viewModel = viewModel("netflix")

        viewModel.onAction(SubscriptionDetailAction.CancelClicked)

        assertTrue(viewModel.state.value.isCancelSheetOpen)
        assertFalse(viewModel.state.value.isAwaitingCancelReturn)
    }

    @Test
    fun `해지하러 가지 않았으면 화면 복귀에 시트를 띄우지 않는다`() {
        val viewModel = viewModel("netflix")

        viewModel.onAction(SubscriptionDetailAction.Resumed)

        assertFalse(viewModel.state.value.isCancelSheetOpen)
    }

    @Test
    fun `일시정지한 구독의 해지는 주소가 있어도 바로 시트`() {
        withCancelUrl("netflix", "https://netflix.com/cancelplan")
        val viewModel = viewModel("netflix")
        viewModel.onAction(SubscriptionDetailAction.PauseClicked)

        viewModel.onAction(SubscriptionDetailAction.CancelClicked)

        assertTrue(viewModel.state.value.isCancelSheetOpen)
    }

    @Test
    fun `고른 해지일로 해지하고 해지 취소로 되돌린다`() {
        val viewModel = viewModel("netflix")
        viewModel.onAction(SubscriptionDetailAction.CancelClicked)
        viewModel.onAction(SubscriptionDetailAction.CancelDateClicked)
        assertTrue(viewModel.state.value.isCancelDatePickerOpen)

        viewModel.onAction(SubscriptionDetailAction.CancelDatePicked(LocalDate.of(2026, 10, 20)))
        viewModel.onAction(SubscriptionDetailAction.CancelConfirmed)

        assertFalse(viewModel.state.value.isCancelSheetOpen)
        val saved =
            subscriptions.records.value
                .first { it.subscription.id == "netflix" }
                .subscription
        assertEquals(SubscriptionStatus.CANCELLED, saved.status)
        assertEquals(LocalDate.of(2026, 10, 20), saved.cancelledOn)
        assertEquals(DetailStatus.CANCELLED, viewModel.content.status)

        viewModel.onAction(SubscriptionDetailAction.UndoCancelClicked)
        assertEquals(DetailStatus.ACTIVE, viewModel.content.status)
    }

    @Test
    fun `저장에 실패하면 알린다`() =
        runTest {
            val viewModel = viewModel("netflix")
            subscriptions.failure = IllegalStateException("db")

            viewModel.event.test {
                viewModel.onAction(SubscriptionDetailAction.PauseClicked)
                assertEquals(SubscriptionDetailEvent.ActionFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isWorking)
        }

    @Test
    fun `수정 · 전체 이력으로 이동`() =
        runTest {
            val viewModel = viewModel("netflix")

            viewModel.event.test {
                viewModel.onAction(SubscriptionDetailAction.EditClicked)
                assertEquals(SubscriptionDetailEvent.OpenEdit("netflix"), awaitItem())

                viewModel.onAction(SubscriptionDetailAction.ShowAllHistoryClicked)
                assertEquals(SubscriptionDetailEvent.OpenHistory("netflix"), awaitItem())
            }
        }

    @Test
    fun `삭제되면 한 번만 뒤로 간다`() =
        runTest {
            val viewModel = viewModel("netflix")

            viewModel.event.test {
                subscriptions.delete("netflix")
                assertEquals(SubscriptionDetailEvent.NavigateBack, awaitItem())

                settings.appSettings.value = settings.appSettings.value.copy(usdKrwRate = 1_350)
                expectNoEvents()
            }
        }
}
