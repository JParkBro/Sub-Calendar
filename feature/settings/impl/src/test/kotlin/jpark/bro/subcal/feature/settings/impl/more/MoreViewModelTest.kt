package jpark.bro.subcal.feature.settings.impl.more

import app.cash.turbine.test
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.testing.FakeBackupRepository
import jpark.bro.subcal.core.testing.FakeSettingsRepository
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

class MoreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()
    private val backup = FakeBackupRepository()
    private val viewModel by lazy { MoreViewModel(settings, backup, SampleData.clock) }

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

                viewModel.onAction(MoreAction.TrialClicked)
                assertEquals(MoreEvent.OpenTrial, awaitItem())

                viewModel.onAction(MoreAction.PartyClicked)
                assertEquals(MoreEvent.OpenParty, awaitItem())

                viewModel.onAction(MoreAction.WidgetClicked)
                assertEquals(MoreEvent.OpenWidgetSettings, awaitItem())

                viewModel.onAction(MoreAction.YearReportClicked)
                assertEquals(MoreEvent.OpenYearReport(2026), awaitItem())
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

    @Test
    fun `내보내기는 오늘 날짜 파일 이름으로 저장 창을 열고 저장 결과를 알린다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(MoreAction.ExportClicked)
                assertEquals(MoreEvent.SaveBackupFile("subcal-backup-20261030.json", backup.validJson), awaitItem())
                assertFalse(state.isBackupBusy)

                viewModel.onAction(MoreAction.ExportFileWritten(success = true))
                assertEquals(MoreEvent.ShowBackupMessage(BackupMessage.EXPORTED), awaitItem())

                viewModel.onAction(MoreAction.ExportFileWritten(success = false))
                assertEquals(MoreEvent.ShowBackupMessage(BackupMessage.EXPORT_FAILED), awaitItem())
            }
        }

    @Test
    fun `내보낼 내용을 만들지 못하면 실패를 알린다`() =
        runTest {
            backup.failure = IOException("db")

            viewModel.event.test {
                viewModel.onAction(MoreAction.ExportClicked)

                assertEquals(MoreEvent.ShowBackupMessage(BackupMessage.EXPORT_FAILED), awaitItem())
            }
        }

    @Test
    fun `불러오기는 파일을 검사해 확인 시트를 열고 확인하면 바꾼 뒤 알린다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(MoreAction.ImportClicked)
                assertEquals(MoreEvent.OpenBackupFile, awaitItem())

                viewModel.onAction(MoreAction.BackupFileRead(backup.validJson))
                assertEquals(MoreSheet.RESTORE_CONFIRM, state.sheet)
                assertEquals(backup.summary, state.restorePreview)
                assertNull(backup.restored)

                viewModel.onAction(MoreAction.RestoreConfirmed)
                assertEquals(MoreEvent.DataRestored, awaitItem())
                assertEquals(backup.validJson, backup.restored)
                assertNull(state.sheet)
            }
        }

    @Test
    fun `백업 파일이 아니거나 읽지 못하면 알리고 시트를 열지 않는다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(MoreAction.BackupFileRead("{}"))
                assertEquals(MoreEvent.ShowBackupMessage(BackupMessage.INVALID_FILE), awaitItem())

                viewModel.onAction(MoreAction.BackupFileRead(null))
                assertEquals(MoreEvent.ShowBackupMessage(BackupMessage.INVALID_FILE), awaitItem())
            }
            assertNull(state.sheet)
        }

    @Test
    fun `확인 시트를 닫으면 불러오지 않는다`() {
        viewModel.onAction(MoreAction.BackupFileRead(backup.validJson))

        viewModel.onAction(MoreAction.SheetDismissed)
        viewModel.onAction(MoreAction.RestoreConfirmed)

        assertNull(backup.restored)
        assertNull(state.restorePreview)
    }

    @Test
    fun `불러오기에 실패하면 알리고 그대로 있는다`() =
        runTest {
            viewModel.onAction(MoreAction.BackupFileRead(backup.validJson))
            backup.failure = IOException("db")

            viewModel.event.test {
                viewModel.onAction(MoreAction.RestoreConfirmed)

                assertEquals(MoreEvent.ShowBackupMessage(BackupMessage.RESTORE_FAILED), awaitItem())
            }
            assertFalse(state.isBackupBusy)
        }

    @Test
    fun `모든 데이터 삭제는 빈 확인 문구로 시트를 열고 확인하면 지운 뒤 알린다`() =
        runTest {
            viewModel.onAction(MoreAction.DeleteAllClicked)
            assertEquals(MoreSheet.DELETE_ALL_CONFIRM, state.sheet)
            assertEquals("", state.deleteAllInput)

            viewModel.onAction(MoreAction.DeleteAllInputChanged("삭제"))
            assertEquals("삭제", state.deleteAllInput)

            viewModel.event.test {
                viewModel.onAction(MoreAction.DeleteAllConfirmed)

                assertEquals(MoreEvent.DataDeleted, awaitItem())
            }
            assertTrue(backup.deleted)
            assertNull(state.sheet)
        }

    @Test
    fun `모든 데이터를 지우지 못하면 알린다`() =
        runTest {
            backup.failure = IOException("db")

            viewModel.event.test {
                viewModel.onAction(MoreAction.DeleteAllConfirmed)

                assertEquals(MoreEvent.ShowBackupMessage(BackupMessage.DELETE_FAILED), awaitItem())
            }
            assertFalse(state.isBackupBusy)
        }
}
