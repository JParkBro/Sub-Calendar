package jpark.bro.subcal.feature.settings.impl.notification

import app.cash.turbine.test
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.ReminderMode
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
import java.io.IOException
import java.time.LocalTime

/** product-spec 9장 샘플 (오늘 = 2026.10.30 금) */
class NotificationSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = FakeSubscriptionRepository(SampleData.records)
    private val settings = FakeSettingsRepository()
    private val viewModel by lazy { NotificationSettingsViewModel(subscriptions, settings) }

    private fun item(id: String): SubscriptionReminderItem =
        viewModel.state.value.subscriptions
            .first { it.id == id }

    @Test
    fun `해지한 구독을 뺀 구독을 이름순으로 보여주고 실제 알림 시점을 계산한다`() {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertFalse(state.subscriptions.any { it.id == SampleData.watcha.subscription.id })
        assertEquals(state.subscriptions.map { it.name }.sorted(), state.subscriptions.map { it.name })
        // 기본이면 매월은 전역 설정(1일 전), 매년(구글 원)은 7일 전
        assertEquals(ReminderChoice.DEFAULT, item("youtube_premium").choice)
        assertEquals(1, item("youtube_premium").leadDays)
        assertEquals(7, item("google_one").leadDays)
    }

    @Test
    fun `결제 알림 · 기본 시점 · 시각을 바꾸면 저장한다`() {
        viewModel.onAction(NotificationSettingsAction.PaymentEnabledChanged(false))
        assertFalse(settings.notificationSettings.value.paymentEnabled)

        viewModel.onAction(NotificationSettingsAction.PaymentEnabledChanged(true))
        viewModel.onAction(NotificationSettingsAction.DefaultDaysBeforeSelected(3))
        assertEquals(3, settings.notificationSettings.value.defaultDaysBefore)
        assertEquals(3, item("youtube_premium").leadDays)

        viewModel.onAction(NotificationSettingsAction.TimeClicked)
        assertTrue(viewModel.state.value.isTimeSheetVisible)
        viewModel.onAction(NotificationSettingsAction.TimeSelected(LocalTime.of(20, 0)))
        assertEquals(LocalTime.of(20, 0), settings.notificationSettings.value.time)
        assertFalse(viewModel.state.value.isTimeSheetVisible)
    }

    @Test
    fun `고를 수 있는 시각은 오전 8시부터 오후 9시까지 정각`() {
        assertEquals(LocalTime.of(8, 0), SelectableTimes.first())
        assertEquals(LocalTime.of(21, 0), SelectableTimes.last())
        assertEquals(14, SelectableTimes.size)
        assertEquals(listOf(0, 1, 3, 7), DaysBeforeChoices)
    }

    @Test
    fun `구독을 눌러 시점을 고르면 그 구독에 저장하고 시트를 닫는다`() {
        viewModel.onAction(NotificationSettingsAction.SubscriptionClicked("youtube_premium"))
        assertEquals(
            "youtube_premium",
            viewModel.state.value.editing
                ?.id,
        )

        viewModel.onAction(NotificationSettingsAction.SubscriptionReminderSelected(ReminderChoice.THREE_DAYS))

        assertNull(viewModel.state.value.editing)
        assertEquals(ReminderChoice.THREE_DAYS, item("youtube_premium").choice)
        assertEquals(3, item("youtube_premium").leadDays)

        viewModel.onAction(NotificationSettingsAction.SubscriptionClicked("youtube_premium"))
        viewModel.onAction(NotificationSettingsAction.SubscriptionReminderSelected(ReminderChoice.OFF))
        assertEquals(
            ReminderMode.OFF,
            subscriptions.records.value
                .first { it.subscription.id == "youtube_premium" }
                .subscription.reminderMode,
        )
        assertNull(item("youtube_premium").leadDays)
    }

    @Test
    fun `고르는 중인 구독이 사라지면 시트를 닫는다`() {
        viewModel.onAction(NotificationSettingsAction.SubscriptionClicked("youtube_premium"))

        subscriptions.records.value = subscriptions.records.value.filterNot { it.subscription.id == "youtube_premium" }

        assertNull(viewModel.state.value.editing)
    }

    @Test
    fun `기기 알림 상태를 반영하고 시스템 설정 열기를 요청한다`() =
        runTest {
            viewModel.onAction(NotificationSettingsAction.DeviceNotificationsChecked(false))
            assertFalse(viewModel.state.value.deviceNotificationsEnabled)

            viewModel.event.test {
                viewModel.onAction(NotificationSettingsAction.OpenSystemSettingsClicked)
                assertEquals(NotificationSettingsEvent.OpenSystemSettings, awaitItem())

                viewModel.onAction(NotificationSettingsAction.BackClicked)
                assertEquals(NotificationSettingsEvent.NavigateBack, awaitItem())
            }
        }

    @Test
    fun `저장에 실패하면 알리고 값은 그대로다`() =
        runTest {
            settings.failure = IOException("disk")
            subscriptions.failure = IOException("disk")

            viewModel.event.test {
                viewModel.onAction(NotificationSettingsAction.DefaultDaysBeforeSelected(7))
                assertEquals(NotificationSettingsEvent.SaveFailed, awaitItem())

                viewModel.onAction(NotificationSettingsAction.SubscriptionClicked("youtube_premium"))
                viewModel.onAction(NotificationSettingsAction.SubscriptionReminderSelected(ReminderChoice.OFF))
                assertEquals(NotificationSettingsEvent.SaveFailed, awaitItem())
            }
            assertEquals(NotificationSettings().defaultDaysBefore, viewModel.state.value.settings.defaultDaysBefore)
            assertEquals(ReminderChoice.DEFAULT, item("youtube_premium").choice)
        }
}
