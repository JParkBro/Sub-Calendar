package jpark.bro.subcal.feature.settings.impl.more

import app.cash.turbine.test
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class MoreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()
    private val viewModel by lazy { MoreViewModel(settings) }

    @Test
    fun `알림 메뉴 설명에 쓸 알림 설정을 보여준다`() {
        settings.notificationSettings.value = NotificationSettings(paymentEnabled = false)

        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.notification.paymentEnabled)
    }

    @Test
    fun `알림 설정을 누르면 알림 설정 화면으로 간다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(MoreAction.NotificationSettingsClicked)

                assertEquals(MoreEvent.OpenNotificationSettings, awaitItem())
            }
        }
}
