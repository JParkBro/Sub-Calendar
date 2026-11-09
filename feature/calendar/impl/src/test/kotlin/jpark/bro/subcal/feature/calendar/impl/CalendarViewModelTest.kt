package jpark.bro.subcal.feature.calendar.impl

import app.cash.turbine.test
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.SubscriptionStatus
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
import java.time.YearMonth

/** product-spec 9장 샘플 (오늘 = 2026.10.30 금) */
class CalendarViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val settings = FakeSettingsRepository()
    private val viewModel by lazy { CalendarViewModel(subscriptions, settings, SampleData.clock) }

    private fun CalendarState.day(dayOfMonth: Int): CalendarDay =
        weeks.flatten().filterNotNull().first { it.date.dayOfMonth == dayOfMonth }

    @Test
    fun `이번 달은 10월 6건 72,490원`() {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(YearMonth.of(2026, 10), state.month)
        assertEquals(6, state.paymentCount)
        assertEquals(72_490, state.totalKrw)
    }

    @Test
    fun `월 시작 기준일과 무관하게 달력 월로 센다`() {
        settings.appSettings.value = AppSettings(monthStartDay = 25)

        assertEquals(72_490, viewModel.state.value.totalKrw)
    }

    @Test
    fun `10월 그리드는 목요일 시작 5주이고 오늘 · 결제 · 체험 종료를 표시한다`() {
        val state = viewModel.state.value

        assertEquals(5, state.weeks.size)
        // 2026.10.01은 목요일: 일 ~ 수 4칸이 비어 있다
        assertEquals(listOf(null, null, null, null), state.weeks.first().take(4))
        assertTrue(state.day(30).isToday)
        assertEquals(listOf(DayIcon("스포티파이", "GREEN")), state.day(30).icons)
        // 디즈니+ 체험 종료일은 결제가 아니라 마커
        assertTrue(state.day(31).hasTrialEnd)
        assertEquals(0, state.day(31).paymentCount)
    }

    @Test
    fun `다음 달 11월은 7건 116,890원이고 밀리의 서재 체험 종료`() {
        viewModel.onAction(CalendarAction.NextMonthClicked)

        val state = viewModel.state.value
        assertEquals(YearMonth.of(2026, 11), state.month)
        assertEquals(7, state.paymentCount)
        assertEquals(116_890, state.totalKrw)
        assertTrue(state.day(12).hasTrialEnd)
        assertEquals(1, state.day(9).paymentCount)
    }

    @Test
    fun `하루에 결제가 3건이면 아이콘 2개와 +1`() {
        subscriptions.records.value =
            listOf(SampleData.netflix, SampleData.melon, SampleData.tving).map {
                it.copy(subscription = it.subscription.copy(firstBillingDate = LocalDate.of(2026, 1, 5)))
            }

        val day = viewModel.state.value.day(5)

        assertEquals(3, day.paymentCount)
        assertEquals(2, day.icons.size)
        assertEquals(1, day.moreCount)
    }

    @Test
    fun `이동은 지난 24개월 ~ 앞으로 12개월까지`() {
        repeat(12) { viewModel.onAction(CalendarAction.NextMonthClicked) }
        assertEquals(YearMonth.of(2027, 10), viewModel.state.value.month)
        assertFalse(viewModel.state.value.canGoNext)

        viewModel.onAction(CalendarAction.NextMonthClicked)
        assertEquals(YearMonth.of(2027, 10), viewModel.state.value.month)

        repeat(40) { viewModel.onAction(CalendarAction.PreviousMonthClicked) }
        assertEquals(YearMonth.of(2024, 10), viewModel.state.value.month)
        assertFalse(viewModel.state.value.canGoPrevious)
    }

    @Test
    fun `결제가 있는 날을 누르면 그날 결제 시트`() {
        viewModel.onAction(CalendarAction.DayClicked(LocalDate.of(2026, 10, 25)))

        val selected = requireNotNull(viewModel.state.value.selected)
        assertEquals(LocalDate.of(2026, 10, 25), selected.date)
        assertEquals(listOf("멜론"), selected.payments.map { it.payment.subscription.name })
        assertEquals(10_900, selected.totalKrw)
        assertEquals(-5, selected.payments.single().daysUntil)

        viewModel.onAction(CalendarAction.SheetDismissed)
        assertNull(viewModel.state.value.selected)
    }

    @Test
    fun `결제가 없는 날은 아무 일도 없다`() {
        viewModel.onAction(CalendarAction.DayClicked(LocalDate.of(2026, 10, 31)))

        assertNull(viewModel.state.value.selected)
    }

    @Test
    fun `달을 바꾸면 고른 날을 지운다`() {
        viewModel.onAction(CalendarAction.DayClicked(LocalDate.of(2026, 10, 2)))
        viewModel.onAction(CalendarAction.NextMonthClicked)

        assertNull(viewModel.state.value.selected)
    }

    @Test
    fun `시트의 결제를 누르면 상세`() =
        runTest {
            viewModel.onAction(CalendarAction.DayClicked(LocalDate.of(2026, 10, 2)))

            viewModel.event.test {
                viewModel.onAction(CalendarAction.PaymentClicked("netflix"))
                assertEquals(CalendarEvent.OpenDetail("netflix"), awaitItem())
            }
            assertNull(viewModel.state.value.selected)
        }

    @Test
    fun `일시정지한 체험은 종료일 표시가 없다`() {
        subscriptions.records.value =
            SampleData.records.map {
                if (it == SampleData.disneyPlus) {
                    it.copy(subscription = it.subscription.copy(status = SubscriptionStatus.PAUSED))
                } else {
                    it
                }
            }

        assertFalse(
            viewModel.state.value
                .day(31)
                .hasTrialEnd,
        )
    }

    @Test
    fun `다른 달의 날짜는 고르지 않는다`() {
        viewModel.onAction(CalendarAction.DayClicked(LocalDate.of(2026, 11, 2)))

        assertNull(viewModel.state.value.selected)
        viewModel.onAction(CalendarAction.NextMonthClicked)
        assertNull(viewModel.state.value.selected)
    }
}
