package jpark.bro.subcal.feature.party.impl.party

import app.cash.turbine.test
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.testing.FakePartyRepository
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.FakeSubscriptionRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import jpark.bro.subcal.feature.party.api.PartyKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/** product-spec 9장 유튜브 프리미엄 파티 (오늘 = 2026.10.30): 10월 민지 · 준호 받음, 서연 미정산 */
class PartyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val party = FakePartyRepository(subscriptions, SampleData.partySettlements)
    private val settings = FakeSettingsRepository()

    private fun viewModel(id: String = "youtube_premium") =
        PartyViewModel(PartyKey(id), subscriptions, party, settings, SampleData.clock)

    private val PartyViewModel.content: PartyContent get() = requireNotNull(state.value.content)

    private fun krw(amount: Long) = Money(amount, Currency.KRW)

    @Test
    fun `4인 균등 분담이면 내 몫 3,725원이고 10월 정산은 2명 받음`() {
        val content = viewModel().content

        assertEquals(4, content.partySize)
        assertEquals(krw(3_725), content.myShare)
        assertEquals(listOf("민지", "준호", "서연"), content.members.map { it.name })
        assertEquals(YearMonth.of(2026, 10), content.bills.first().month)
        assertEquals(LocalDate.of(2026, 10, 14), content.bills.first().firstPaymentDate)
        assertEquals(2, content.bills.first().settledCount)
        assertEquals(
            listOf("서연"),
            content.bills
                .first()
                .unsettled
                .map { it.name },
        )
        assertEquals(4, content.bills.size)
    }

    @Test
    fun `받음을 체크하면 고른 기간에 저장한다`() {
        val viewModel = viewModel()
        viewModel.onAction(PartyAction.PeriodSelected(YearMonth.of(2026, 9)))

        viewModel.onAction(PartyAction.SettledToggled("youtube-서연", settled = true))

        assertEquals(
            YearMonth.of(2026, 9),
            viewModel.state.value.selectedBill
                ?.month,
        )
        assertEquals(
            1,
            viewModel.state.value.selectedBill
                ?.settledCount,
        )
        assertEquals(
            2,
            viewModel.content.bills
                .first()
                .settledCount,
        )
    }

    @Test
    fun `미정산 멤버 요청은 그 사람 몫으로, 모두 요청은 미정산 전원으로 공유한다`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(PartyAction.RequestClicked("youtube-서연"))
                assertEquals(
                    PartyEvent.ShareRequest("유튜브 프리미엄", pastMonth = null, listOf(RequestLine("서연", krw(3_725)))),
                    awaitItem(),
                )

                viewModel.onAction(PartyAction.PeriodSelected(YearMonth.of(2026, 9)))
                viewModel.onAction(PartyAction.RequestAllClicked)
                val request = awaitItem() as PartyEvent.ShareRequest
                assertEquals(9, request.pastMonth)
                assertEquals(listOf("민지", "준호", "서연"), request.lines.map { it.name })

                // 이미 받은 멤버에게는 보내지 않는다
                viewModel.onAction(PartyAction.PeriodSelected(YearMonth.of(2026, 10)))
                viewModel.onAction(PartyAction.RequestClicked("youtube-민지"))
                expectNoEvents()
            }
        }

    @Test
    fun `멤버를 추가 · 이름 바꾸기 · 빼면 몫이 다시 계산된다`() {
        val viewModel = viewModel()

        viewModel.onAction(PartyAction.AddMemberClicked)
        viewModel.onAction(PartyAction.MemberNameChanged("  지수  "))
        viewModel.onAction(PartyAction.MemberSaveClicked)
        assertNull(viewModel.state.value.sheet)
        assertEquals(5, viewModel.content.partySize)
        assertEquals(krw(2_980), viewModel.content.myShare)

        viewModel.onAction(PartyAction.MemberClicked("youtube-민지"))
        assertEquals(PartySheet.MemberName("youtube-민지", "민지"), viewModel.state.value.sheet)
        viewModel.onAction(PartyAction.MemberNameChanged("민지언니"))
        viewModel.onAction(PartyAction.MemberSaveClicked)
        assertEquals(
            "민지언니",
            viewModel.content.members
                .first()
                .name,
        )

        viewModel.onAction(PartyAction.MemberClicked("youtube-준호"))
        viewModel.onAction(PartyAction.MemberDeleteClicked)
        assertEquals(listOf("민지언니", "서연", "지수"), viewModel.content.members.map { it.name })
    }

    @Test
    fun `이름이 비었으면 저장하지 않고 10자에서 자른다`() {
        val viewModel = viewModel()
        viewModel.onAction(PartyAction.AddMemberClicked)

        viewModel.onAction(PartyAction.MemberNameChanged("   "))
        viewModel.onAction(PartyAction.MemberSaveClicked)
        assertTrue(viewModel.state.value.sheet is PartySheet.MemberName)

        viewModel.onAction(PartyAction.MemberNameChanged("열한글자가넘는이름이다"))
        assertEquals("열한글자가넘는이름이", (viewModel.state.value.sheet as PartySheet.MemberName).input)
    }

    @Test
    fun `직접 입력은 지금 몫으로 채워 열고 저장하면 내 몫이 나머지다`() {
        val viewModel = viewModel()

        viewModel.onAction(PartyAction.SplitClicked)
        assertEquals(
            PartySheet.Split(
                PartySplitMode.EQUAL,
                mapOf("youtube-민지" to "3725", "youtube-준호" to "3725", "youtube-서연" to "3725"),
            ),
            viewModel.state.value.sheet,
        )
        viewModel.onAction(PartyAction.SplitModeSelected(PartySplitMode.CUSTOM))
        viewModel.onAction(PartyAction.ShareInputChanged("youtube-민지", "5000"))
        viewModel.onAction(PartyAction.ShareInputChanged("youtube-서연", ""))
        viewModel.onAction(PartyAction.SplitSaveClicked)

        assertNull(viewModel.state.value.sheet)
        assertEquals(PartySplitMode.CUSTOM, viewModel.content.splitMode)
        // 14,900 − (5,000 + 3,725 + 0)
        assertEquals(krw(6_175), viewModel.content.myShare)
    }

    @Test
    fun `직접 입력 합이 구독 금액보다 크면 저장하지 않고 알린다`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(PartyAction.SplitClicked)
            viewModel.onAction(PartyAction.SplitModeSelected(PartySplitMode.CUSTOM))
            viewModel.onAction(PartyAction.ShareInputChanged("youtube-민지", "10000"))

            viewModel.event.test {
                viewModel.onAction(PartyAction.SplitSaveClicked)
                assertEquals(PartyEvent.SplitOverTotal, awaitItem())
            }
            assertTrue(viewModel.state.value.sheet is PartySheet.Split)
            assertEquals(PartySplitMode.EQUAL, viewModel.content.splitMode)
        }

    @Test
    fun `파티가 없으면 멤버 · 정산이 비고 내 몫은 전체다`() {
        val content = viewModel("netflix").content

        assertTrue(content.members.isEmpty())
        assertTrue(content.bills.isEmpty())
        assertEquals(krw(17_000), content.myShare)
        assertTrue(content.canAddMember)
    }

    @Test
    fun `저장에 실패하면 알린다`() =
        runTest {
            party.failure = IllegalStateException("db")
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(PartyAction.SettledToggled("youtube-서연", settled = true))

                assertEquals(PartyEvent.ActionFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isWorking)
        }

    @Test
    fun `구독이 삭제되면 뒤로 간다`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                subscriptions.records.value = SampleData.records.filterNot { it.subscription.id == "youtube_premium" }

                assertEquals(PartyEvent.NavigateBack, awaitItem())
            }
        }
}
