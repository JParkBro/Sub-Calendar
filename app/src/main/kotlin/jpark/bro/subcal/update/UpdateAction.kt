package jpark.bro.subcal.update

internal sealed interface UpdateAction {
    /** 앱으로 돌아왔다 (onResume). 다운로드가 끝났는지 다시 확인한다 */
    data object AppResumed : UpdateAction

    data object UpdateConfirmed : UpdateAction

    data object UpdateDismissed : UpdateAction

    data object InstallConfirmed : UpdateAction

    data object InstallDismissed : UpdateAction
}
