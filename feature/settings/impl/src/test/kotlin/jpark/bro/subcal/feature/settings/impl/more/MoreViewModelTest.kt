package jpark.bro.subcal.feature.settings.impl.more

import app.cash.turbine.test
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class MoreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()
    private val viewModel by lazy { MoreViewModel(settings) }

    private val state: MoreState get() = viewModel.state.value

    @Test
    fun `알림 설정과 앱 설정 · 버전을 보여준다`() {
        settings.notificationSettings.value = NotificationSettings(paymentEnabled = false)
        settings.appSettings.value = AppSettings(monthStartDay = 25)

        viewModel.onAction(MoreAction.AppVersionLoaded("1.0.0"))

        assertFalse(state.isLoading)
        assertFalse(state.notification.paymentEnabled)
        assertEquals(25, state.appSettings.monthStartDay)
        assertEquals("1.0.0", state.appVersion)
    }

    @Test
    fun `알림 설정을 누르면 알림 설정 화면으로 간다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(MoreAction.NotificationSettingsClicked)

                assertEquals(MoreEvent.OpenNotificationSettings, awaitItem())
            }
        }

    @Test
    fun `월 시작 기준일을 고르면 저장하고 시트를 닫는다`() {
        viewModel.onAction(MoreAction.MonthStartDayClicked)
        assertEquals(MoreSheet.MONTH_START_DAY, state.sheet)

        viewModel.onAction(MoreAction.MonthStartDaySelected(25))

        assertNull(state.sheet)
        assertEquals(25, settings.appSettings.value.monthStartDay)
    }

    @Test
    fun `환율은 지금 값으로 시작하고 숫자만 받아 0보다 클 때 저장한다`() {
        viewModel.onAction(MoreAction.UsdKrwRateClicked)
        assertEquals("1400", state.usdKrwRateInput)

        viewModel.onAction(MoreAction.UsdKrwRateInputChanged("0"))
        assertFalse(state.canSaveUsdKrwRate)
        viewModel.onAction(MoreAction.UsdKrwRateSaveClicked)
        assertEquals(AppSettings.DEFAULT_USD_KRW_RATE, settings.appSettings.value.usdKrwRate)

        viewModel.onAction(MoreAction.UsdKrwRateInputChanged("1,385원1234"))
        assertEquals("138512", state.usdKrwRateInput)
        viewModel.onAction(MoreAction.UsdKrwRateInputChanged("1385"))
        assertTrue(state.canSaveUsdKrwRate)
        viewModel.onAction(MoreAction.UsdKrwRateSaveClicked)

        assertEquals(1_385L, settings.appSettings.value.usdKrwRate)
        assertNull(state.sheet)
    }

    @Test
    fun `기본 결제 수단은 앞뒤 공백을 빼고 저장하고 비우면 지운다`() {
        viewModel.onAction(MoreAction.PaymentMethodClicked)
        viewModel.onAction(MoreAction.PaymentMethodInputChanged("  신한카드 "))
        viewModel.onAction(MoreAction.PaymentMethodSaveClicked)
        assertEquals("신한카드", settings.appSettings.value.defaultPaymentMethod)

        viewModel.onAction(MoreAction.PaymentMethodClicked)
        assertEquals("신한카드", state.paymentMethodInput)
        viewModel.onAction(MoreAction.PaymentMethodInputChanged(" "))
        viewModel.onAction(MoreAction.PaymentMethodSaveClicked)
        assertNull(settings.appSettings.value.defaultPaymentMethod)
    }

    @Test
    fun `화면 모드를 고르면 저장한다`() {
        viewModel.onAction(MoreAction.ThemeModeClicked)
        viewModel.onAction(MoreAction.ThemeModeSelected(AppThemeMode.DARK))

        assertEquals(AppThemeMode.DARK, settings.appSettings.value.themeMode)
        assertNull(state.sheet)
    }

    @Test
    fun `시트를 닫으면 저장하지 않는다`() {
        viewModel.onAction(MoreAction.PaymentMethodClicked)
        viewModel.onAction(MoreAction.PaymentMethodInputChanged("현대카드"))

        viewModel.onAction(MoreAction.SheetDismissed)

        assertNull(state.sheet)
        assertNull(settings.appSettings.value.defaultPaymentMethod)
    }

    @Test
    fun `저장에 실패하면 알리고 값은 그대로다`() =
        runTest {
            settings.failure = IOException("disk")

            viewModel.event.test {
                viewModel.onAction(MoreAction.MonthStartDaySelected(25))

                assertEquals(MoreEvent.SaveFailed, awaitItem())
            }
            assertEquals(1, state.appSettings.monthStartDay)
        }
}
