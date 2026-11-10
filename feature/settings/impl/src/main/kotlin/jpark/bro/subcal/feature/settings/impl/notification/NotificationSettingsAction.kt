package jpark.bro.subcal.feature.settings.impl.notification

import java.time.LocalTime

internal sealed interface NotificationSettingsAction {
    data object BackClicked : NotificationSettingsAction

    /** 화면에 돌아올 때마다 Root가 기기 알림이 켜져 있는지 알려준다 */
    data class DeviceNotificationsChecked(
        val enabled: Boolean,
    ) : NotificationSettingsAction

    data object OpenSystemSettingsClicked : NotificationSettingsAction

    data class PaymentEnabledChanged(
        val enabled: Boolean,
    ) : NotificationSettingsAction

    data class DefaultDaysBeforeSelected(
        val daysBefore: Int,
    ) : NotificationSettingsAction

    data object TimeClicked : NotificationSettingsAction

    data class TimeSelected(
        val time: LocalTime,
    ) : NotificationSettingsAction

    data object TimeSheetDismissed : NotificationSettingsAction

    data class SubscriptionClicked(
        val subscriptionId: String,
    ) : NotificationSettingsAction

    data class SubscriptionReminderSelected(
        val choice: ReminderChoice,
    ) : NotificationSettingsAction

    data object SubscriptionSheetDismissed : NotificationSettingsAction
}
