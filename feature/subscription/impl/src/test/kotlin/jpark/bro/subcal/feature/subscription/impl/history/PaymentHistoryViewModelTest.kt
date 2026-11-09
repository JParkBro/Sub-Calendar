package jpark.bro.subcal.feature.subscription.impl.history

import jpark.bro.subcal.core.domain.billing.PaymentHistoryEntry
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PaymentHistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `첫 결제부터 오늘까지 전체 이력을 최신순으로`() {
        val viewModel =
            PaymentHistoryViewModel("netflix", FakeSubscriptionRepository(SampleData.records), SampleData.clock)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("넷플릭스 프리미엄", state.name)
        assertEquals(28, state.entries.filterIsInstance<PaymentHistoryEntry.Paid>().size)
        assertEquals(LocalDate.of(2024, 7, 2), state.entries.last().date)
    }
}
