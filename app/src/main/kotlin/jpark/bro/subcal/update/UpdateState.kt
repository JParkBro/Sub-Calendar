package jpark.bro.subcal.update

import com.google.android.play.core.appupdate.AppUpdateInfo

internal data class UpdateState(
    val prompt: UpdatePrompt = UpdatePrompt.None,
)

/** 지금 띄울 업데이트 확인 시트. 둘이 동시에 뜨지 않도록 하나로 둔다 */
internal sealed interface UpdatePrompt {
    data object None : UpdatePrompt

    /** 새 버전이 있다. 수락하면 Play 업데이트 화면을 연다 */
    data class UpdateAvailable(
        val info: AppUpdateInfo,
    ) : UpdatePrompt

    /** 백그라운드 다운로드가 끝났다. 수락하면 재시작해 설치한다 */
    data object ReadyToInstall : UpdatePrompt
}
