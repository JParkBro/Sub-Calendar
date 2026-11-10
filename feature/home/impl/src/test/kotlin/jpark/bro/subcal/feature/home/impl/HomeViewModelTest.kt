package jpark.bro.subcal.feature.home.impl

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

/** product-spec 9장 기대 값 (오늘 = 2026.10.30) */
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val settings = FakeSettingsRepository()
    private val viewModel by lazy { HomeViewModel(subscriptions, settings, SampleData.clock) }

    @Test
    fun `이번 달 카드는 10월 구독료 · 지난달 대비 · 월 평균 · 연간 환산`() {
        val summary = viewModel.state.value.summary

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(10, summary.month)
        assertNull(summary.range)
        assertEquals(72_490, summary.totalKrw)
        assertEquals(10_900, summary.changeKrw)
        assertEquals(76_190, summary.monthlyAverageKrw)
        assertEquals(914_280, summary.annualKrw)
    }

    @Test
    fun `월 시작 기준일이 25일이면 10월 25일 ~ 11월 24일`() {
        settings.appSettings.value = AppSettings(monthStartDay = 25)

        val summary = viewModel.state.value.summary

        assertEquals(10, summary.month)
        assertEquals(LocalDate.of(2026, 10, 25)..LocalDate.of(2026, 11, 24), summary.range)
        // 멜론 10/25 · 스포티파이 10/30 · 넷플릭스 · 쿠팡 · 구글 원 · 유튜브 · 티빙 (체험 2건 제외)
        assertEquals(116_890, summary.totalKrw)
    }

    @Test
    fun `다가오는 결제 3개`() {
        val upcoming = viewModel.state.value.upcoming

        assertEquals(listOf("스포티파이", "넷플릭스 프리미엄", "쿠팡 와우"), upcoming.map { it.payment.subscription.name })
        assertEquals(listOf(0, 3, 6), upcoming.map { it.daysUntil })
    }

    @Test
    fun `카테고리 칩은 이용 중 구독만 센다`() {
        assertEquals(
            listOf(
                CategoryCount(Category.VIDEO, 3),
                CategoryCount(Category.MUSIC, 2),
                CategoryCount(Category.SHOPPING, 1),
                CategoryCount(Category.CLOUD, 1),
            ),
            viewModel.state.value.categories,
        )
    }

    @Test
    fun `구독이 없으면 빈 상태`() {
        subscriptions.records.value = emptyList()

        val state = viewModel.state.value
        assertTrue(state.isEmpty)
        assertEquals(0, state.summary.totalKrw)
        assertTrue(state.upcoming.isEmpty())
    }

    @Test
    fun `구독을 추가하면 다시 계산한다`() {
        subscriptions.records.value = emptyList()
        assertTrue(viewModel.state.value.isEmpty)

        subscriptions.records.value = listOf(SampleData.coupangWow)

        assertFalse(viewModel.state.value.isEmpty)
        assertEquals(7_890, viewModel.state.value.summary.monthlyAverageKrw)
    }

    @Test
    fun `누르는 곳마다 이동할 화면을 알린다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(HomeAction.UpcomingClicked("netflix"))
                assertEquals(HomeEvent.OpenDetail("netflix"), awaitItem())

                viewModel.onAction(HomeAction.SeeAllClicked)
                assertEquals(HomeEvent.OpenCalendar, awaitItem())

                viewModel.onAction(HomeAction.CategoryClicked(Category.VIDEO))
                assertEquals(HomeEvent.OpenList(Category.VIDEO), awaitItem())

                viewModel.onAction(HomeAction.AddClicked)
                assertEquals(HomeEvent.OpenServicePicker, awaitItem())

                viewModel.onAction(HomeAction.QuickAddClicked(QuickAdd.NETFLIX))
                assertEquals(HomeEvent.OpenEdit("netflix"), awaitItem())

                viewModel.onAction(HomeAction.QuickAddClicked(QuickAdd.MANUAL))
                assertEquals(HomeEvent.OpenEdit(null), awaitItem())
            }
        }

    @Test
    fun `온보딩에서 등록했으면 결제일 확인 안내를 보여주고 누르면 목록으로 가며 닫는다`() =
        runTest {
            settings.billingDateHintPending.value = true
            assertTrue(viewModel.state.value.showBillingDateHint)

            viewModel.event.test {
                viewModel.onAction(HomeAction.BillingDateHintClicked)
                assertEquals(HomeEvent.OpenList(category = null), awaitItem())
            }
            assertFalse(settings.billingDateHintPending.value)
            assertFalse(viewModel.state.value.showBillingDateHint)
        }

    @Test
    fun `결제일 확인 안내를 닫는다`() {
        settings.billingDateHintPending.value = true

        viewModel.onAction(HomeAction.BillingDateHintDismissed)

        assertFalse(viewModel.state.value.showBillingDateHint)
    }

    @Test
    fun `구독이 없으면 결제일 확인 안내도 없다`() {
        settings.billingDateHintPending.value = true
        subscriptions.records.value = emptyList()

        assertFalse(viewModel.state.value.showBillingDateHint)
    }

    /** 디즈니+ 첫 결제일(10/31)이 지났는데 결과를 고르지 않은 11/2 */
    private fun afterDisneyFirstBilling() =
        HomeViewModel(
            subscriptions,
            settings,
            java.time.Clock.fixed(
                LocalDate
                    .of(2026, 11, 2)
                    .atTime(12, 0)
                    .atZone(SampleData.clock.zone)
                    .toInstant(),
                SampleData.clock.zone,
            ),
        )

    @Test
    fun `종료 3일 이내 체험은 디즈니+ 1건이고 누르면 무료체험으로 간다`() =
        runTest {
            assertEquals(1, viewModel.state.value.trialsEndingSoon)
            assertTrue(
                viewModel.state.value.unresolvedTrials
                    .isEmpty(),
            )

            viewModel.event.test {
                viewModel.onAction(HomeAction.TrialWarningClicked)

                assertEquals(HomeEvent.OpenTrial, awaitItem())
            }
        }

    @Test
    fun `첫 결제일이 지난 체험은 결제됐나요에 나오고 계속 쓸래요를 고르면 사라진다`() {
        val viewModel = afterDisneyFirstBilling()
        assertEquals(
            listOf("disney_plus"),
            viewModel.state.value.unresolvedTrials
                .map { it.id },
        )

        viewModel.onAction(HomeAction.UnresolvedKeepClicked("disney_plus"))

        assertTrue(
            viewModel.state.value.unresolvedTrials
                .isEmpty(),
        )
    }

    @Test
    fun `결제됐나요에서 해지했어요를 고르면 첫 결제일로 해지한다`() {
        val viewModel = afterDisneyFirstBilling()

        viewModel.onAction(HomeAction.UnresolvedCancelledClicked("disney_plus"))

        val disney =
            subscriptions.records.value
                .first { it.subscription.id == "disney_plus" }
                .subscription
        assertEquals(LocalDate.of(2026, 10, 31), disney.cancelledOn)
        assertTrue(
            viewModel.state.value.unresolvedTrials
                .isEmpty(),
        )
    }
}
