package jpark.bro.subcal.update

import com.google.android.play.core.appupdate.AppUpdateInfo

/** Play 호출은 Activity가 필요하므로 MainActivity가 받아서 실행한다 */
internal sealed interface UpdateEvent {
    /** appUpdateManager.startUpdateFlowForResult(FLEXIBLE) */
    data class StartUpdateFlow(
        val info: AppUpdateInfo,
    ) : UpdateEvent

    /** appUpdateManager.completeUpdate(). 앱이 재시작된다 */
    data object CompleteUpdate : UpdateEvent
}
