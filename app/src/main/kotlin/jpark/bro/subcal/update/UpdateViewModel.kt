package jpark.bro.subcal.update

import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.requestAppUpdateInfo
import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

/**
 * Play 인앱 업데이트(FLEXIBLE) 판단 (docs/subcal/in-app-update.md).
 * 새 버전 안내와 재시작 모두 사용자가 수락해야 진행한다. Play 호출은 Event로 MainActivity에 넘긴다.
 * 새 버전 안내는 앱을 켤 때만, 하루 한 번 묻는다. 재시작 확인은 앱으로 돌아올 때마다 묻는다.
 */
internal class UpdateViewModel(
    private val appUpdateManager: AppUpdateManager,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<UpdateState, UpdateAction, UpdateEvent>(UpdateState()) {
    private val installStateListener =
        InstallStateUpdatedListener { installState ->
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                setState { copy(prompt = UpdatePrompt.ReadyToInstall) }
            }
        }

    init {
        appUpdateManager.registerListener(installStateListener)
        // ViewModel은 화면 회전에도 남으므로 새 버전 안내는 앱을 켤 때 한 번만 확인한다 (onResume에서는 다시 묻지 않음)
        checkUpdate(offerNewVersion = true)
    }

    override fun onCleared() {
        appUpdateManager.unregisterListener(installStateListener)
        super.onCleared()
    }

    override fun onAction(action: UpdateAction) {
        when (action) {
            UpdateAction.AppResumed -> {
                checkUpdate(offerNewVersion = false)
            }

            // Play 화면에서 취소해도 같은 날 다시 켤 때 묻지 않도록 수락한 날도 기억한다
            UpdateAction.UpdateConfirmed -> {
                val prompt = currentState.prompt
                if (prompt is UpdatePrompt.UpdateAvailable) sendEvent(UpdateEvent.StartUpdateFlow(prompt.info))
                setState { copy(prompt = UpdatePrompt.None) }
                rememberAnsweredToday()
            }

            UpdateAction.UpdateDismissed -> {
                setState { copy(prompt = UpdatePrompt.None) }
                rememberAnsweredToday()
            }

            UpdateAction.InstallConfirmed -> {
                sendEvent(UpdateEvent.CompleteUpdate)
                setState { copy(prompt = UpdatePrompt.None) }
            }

            // 설치 준비 완료를 미루면 다음에 앱으로 돌아올 때 다시 묻는다 (Play 권장)
            UpdateAction.InstallDismissed -> {
                setState { copy(prompt = UpdatePrompt.None) }
            }
        }
    }

    /** 실패는 무시한다. Play에서 설치하지 않은 빌드(디버그 설치)는 여기서 실패하거나 업데이트 없음으로 온다 */
    private fun checkUpdate(offerNewVersion: Boolean) {
        viewModelScope.launch {
            val info = suspendRunCatching { appUpdateManager.requestAppUpdateInfo() }.getOrNull() ?: return@launch
            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> {
                    setState { copy(prompt = UpdatePrompt.ReadyToInstall) }
                }

                offerNewVersion && info.canOfferFlexibleUpdate() && !answeredToday() -> {
                    setState { copy(prompt = UpdatePrompt.UpdateAvailable(info)) }
                }
            }
        }
    }

    /** 다운로드 중(앱을 껐다 켠 경우)에는 새 버전 안내를 띄우지 않는다 */
    private fun AppUpdateInfo.canOfferFlexibleUpdate(): Boolean =
        updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            isFlexibleUpdateAllowed &&
            installStatus() !in InProgressStatuses

    private suspend fun answeredToday(): Boolean =
        settingsRepository.observeUpdatePromptAnsweredOn().first() == LocalDate.now(clock)

    /** 저장에 실패하면 다음에 앱을 켤 때 한 번 더 묻는 것뿐이라 무시한다 */
    private fun rememberAnsweredToday() {
        viewModelScope.launch { settingsRepository.setUpdatePromptAnsweredOn(LocalDate.now(clock)) }
    }

    private companion object {
        val InProgressStatuses = setOf(InstallStatus.PENDING, InstallStatus.DOWNLOADING, InstallStatus.INSTALLING)
    }
}
