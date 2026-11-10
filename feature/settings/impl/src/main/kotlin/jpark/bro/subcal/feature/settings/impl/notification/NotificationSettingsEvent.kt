package jpark.bro.subcal.feature.settings.impl.notification

internal sealed interface NotificationSettingsEvent {
    data object NavigateBack : NotificationSettingsEvent

    /** 시스템의 이 앱 알림 설정 화면 */
    data object OpenSystemSettings : NotificationSettingsEvent

    data object SaveFailed : NotificationSettingsEvent
}
