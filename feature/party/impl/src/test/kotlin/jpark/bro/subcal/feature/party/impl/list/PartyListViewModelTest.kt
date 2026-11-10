package jpark.bro.subcal.feature.party.impl.list

import app.cash.turbine.test
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.testing.FakePartyRepository
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** product-spec 9장 샘플 (오늘 = 2026.10.30) */
class PartyListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val party = FakePartyRepository(subscriptions, SampleData.partySettlements)
    private val viewModel by lazy {
        PartyListViewModel(subscriptions, party, FakeSettingsRepository(), SampleData.clock)
    }

    @Test
    fun `파티가 있는 구독만 내 몫과 이번 달 미정산 수를 보여준다`() {
        assertEquals(
            listOf(PartyListItem("youtube_premium", "유튜브 프리미엄", "PINK", 4, Money(3_725, Currency.KRW), 1)),
            viewModel.state.value.items,
        )
    }

    @Test
    fun `파티가 없으면 비어 있다`() {
        subscriptions.records.value = SampleData.records.map { it.copy(partyMembers = emptyList()) }

        assertTrue(
            viewModel.state.value.items
                .isEmpty(),
        )
    }

    @Test
    fun `누르면 그 구독의 파티 정산으로 간다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(PartyListAction.PartyClicked("youtube_premium"))
                assertEquals(PartyListEvent.OpenParty("youtube_premium"), awaitItem())
            }
        }
}
