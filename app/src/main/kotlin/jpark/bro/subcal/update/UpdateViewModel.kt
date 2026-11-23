package jpark.bro.subcal.update

import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.requestAppUpdateInfo
import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.launch

/**
 * Play 인앱 업데이트(FLEXIBLE) 판단 (docs/subcal/in-app-update.md).
 * 새 버전 안내와 재시작 모두 사용자가 수락해야 진행한다. Play 호출은 Event로 MainActivity에 넘긴다.
 */
internal class UpdateViewModel(
    private val appUpdateManager: AppUpdateManager,
) : MviViewModel<UpdateState, UpdateAction, UpdateEvent>(UpdateState()) {
    private val installStateListener =
        InstallStateUpdatedListener { installState ->
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                setState { copy(prompt = UpdatePrompt.ReadyToInstall) }
            }
        }

    init {
        appUpdateManager.registerListener(installStateListener)
        // ViewModel은 화면 회전에도 남으므로 새 버전 안내는 앱을 켤 때 한 번만 확인한다
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

            UpdateAction.UpdateConfirmed -> {
                val prompt = currentState.prompt
                if (prompt is UpdatePrompt.UpdateAvailable) sendEvent(UpdateEvent.StartUpdateFlow(prompt.info))
                setState { copy(prompt = UpdatePrompt.None) }
            }

            UpdateAction.InstallConfirmed -> {
                sendEvent(UpdateEvent.CompleteUpdate)
                setState { copy(prompt = UpdatePrompt.None) }
            }

            // 설치 준비 완료를 미루면 다음에 앱으로 돌아올 때 다시 묻는다 (Play 권장)
            UpdateAction.UpdateDismissed, UpdateAction.InstallDismissed -> {
                setState { copy(prompt = UpdatePrompt.None) }
            }
        }
    }

    /** 실패는 무시한다. Play에서 설치하지 않은 빌드(디버그 설치)는 여기서 실패하거나 업데이트 없음으로 온다 */
    private fun checkUpdate(offerNewVersion: Boolean) {
        viewModelScope.launch {
            val info = suspendRunCatching { appUpdateManager.requestAppUpdateInfo() }.getOrNull() ?: return@launch
            val downloaded = info.installStatus() == InstallStatus.DOWNLOADED
            val available =
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && info.isFlexibleUpdateAllowed
            when {
                downloaded -> setState { copy(prompt = UpdatePrompt.ReadyToInstall) }
                offerNewVersion && available -> setState { copy(prompt = UpdatePrompt.UpdateAvailable(info)) }
            }
        }
    }
}
