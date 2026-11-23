package jpark.bro.subcal.update

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import jpark.bro.subcal.core.testing.FakeSettingsRepository
import jpark.bro.subcal.core.testing.MainDispatcherRule
import jpark.bro.subcal.core.testing.SampleData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// SubCalApplication은 Koin을 시작하므로 테스트마다 다시 만들면 충돌한다
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class UpdateViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val appUpdateManager = FakeAppUpdateManager(context)
    private val settings = FakeSettingsRepository()

    /** 오늘 = 2026.10.30 (product-spec 9장) */
    private fun newViewModel() = UpdateViewModel(appUpdateManager, settings, SampleData.clock)

    @Test
    fun `새 버전이 없으면 아무것도 묻지 않는다`() {
        val viewModel = newViewModel()

        assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
    }

    @Test
    fun `새 버전이 있으면 앱을 켤 때 업데이트를 묻는다`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)

        val viewModel = newViewModel()

        assertTrue(viewModel.state.value.prompt is UpdatePrompt.UpdateAvailable)
    }

    @Test
    fun `업데이트를 수락하면 시트를 닫고 Play 업데이트 화면을 연다`() =
        runTest {
            appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
            val viewModel = newViewModel()

            viewModel.event.test {
                viewModel.onAction(UpdateAction.UpdateConfirmed)

                assertTrue(awaitItem() is UpdateEvent.StartUpdateFlow)
                assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
            }
        }

    @Test
    fun `나중에를 고르면 시트만 닫고 Play 화면을 열지 않는다`() =
        runTest {
            appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
            val viewModel = newViewModel()

            viewModel.event.test {
                viewModel.onAction(UpdateAction.UpdateDismissed)

                assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
                expectNoEvents()
            }
        }

    @Test
    fun `나중에를 고르면 같은 날 앱을 다시 켜도 묻지 않는다`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
        newViewModel().onAction(UpdateAction.UpdateDismissed)
        assertEquals(SampleData.today, settings.updatePromptAnsweredOn.value)

        val reopened = newViewModel()

        assertEquals(UpdatePrompt.None, reopened.state.value.prompt)
    }

    @Test
    fun `전날 미뤘으면 다음 날 앱을 켤 때 다시 묻는다`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
        settings.updatePromptAnsweredOn.value = SampleData.today.minusDays(1)

        val viewModel = newViewModel()

        assertTrue(viewModel.state.value.prompt is UpdatePrompt.UpdateAvailable)
    }

    @Test
    fun `업데이트를 수락한 날도 기억해 Play 화면에서 취소해도 그날은 다시 묻지 않는다`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
        newViewModel().onAction(UpdateAction.UpdateConfirmed)

        assertEquals(SampleData.today, settings.updatePromptAnsweredOn.value)
        assertEquals(UpdatePrompt.None, newViewModel().state.value.prompt)
    }

    @Test
    fun `다운로드 중에 앱을 다시 켜면 새 버전 안내를 띄우지 않는다`() {
        startDownload()
        settings.updatePromptAnsweredOn.value = null

        val reopened = newViewModel()

        assertEquals(UpdatePrompt.None, reopened.state.value.prompt)
    }

    @Test
    fun `앱으로 돌아왔을 때는 새 버전 안내를 다시 띄우지 않는다`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
        val viewModel = newViewModel()
        viewModel.onAction(UpdateAction.UpdateDismissed)

        viewModel.onAction(UpdateAction.AppResumed)

        assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
    }

    @Test
    fun `다운로드가 끝나면 재시작을 묻는다`() {
        val viewModel = startDownload()

        appUpdateManager.downloadCompletes()

        assertEquals(UpdatePrompt.ReadyToInstall, viewModel.state.value.prompt)
    }

    @Test
    fun `재시작을 수락하면 시트를 닫고 설치를 완료한다`() =
        runTest {
            val viewModel = startDownload()
            appUpdateManager.downloadCompletes()

            viewModel.event.test {
                skipItems(1) // startDownload()에서 보낸 StartUpdateFlow
                viewModel.onAction(UpdateAction.InstallConfirmed)

                assertEquals(UpdateEvent.CompleteUpdate, awaitItem())
                assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
            }
        }

    @Test
    fun `재시작을 미루면 앱으로 돌아올 때 다시 묻는다`() {
        val viewModel = startDownload()
        appUpdateManager.downloadCompletes()
        viewModel.onAction(UpdateAction.InstallDismissed)
        assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)

        viewModel.onAction(UpdateAction.AppResumed)

        assertEquals(UpdatePrompt.ReadyToInstall, viewModel.state.value.prompt)
    }

    /** 새 버전 안내 수락 → Play 화면에서 동의 → 다운로드 시작까지 */
    private fun startDownload(): UpdateViewModel {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
        val viewModel = newViewModel()
        viewModel.onAction(UpdateAction.UpdateConfirmed)
        // 실제로는 MainActivity가 Event를 받아 Play 화면을 연다
        appUpdateManager.startUpdateFlowForResult(
            appUpdateManager.appUpdateInfo.result,
            { _, _, _, _, _, _, _ -> },
            AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
            0,
        )
        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        return viewModel
    }

    private companion object {
        const val NEW_VERSION_CODE = 4
    }
}
