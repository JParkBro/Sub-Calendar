package jpark.bro.subcal.feature.settings.impl.notification

import java.time.LocalTime

internal sealed interface NotificationSettingsAction {
    data object BackClicked : NotificationSettingsAction

    /** 화면에 돌아올 때마다 Root가 기기 알림이 켜져 있는지 알려준다 */
    data class DeviceNotificationsChecked(
        val enabled: Boolean,
    ) : NotificationSettingsAction

    data object OpenSystemSettingsClicked : NotificationSettingsAction

    /** 알림 종류 스위치 */
    sealed interface Toggle : NotificationSettingsAction {
        val enabled: Boolean
    }

    data class PaymentEnabledChanged(
        override val enabled: Boolean,
    ) : Toggle

    /** 무료체험 종료 알림 (product-spec 2-7) */
    data class TrialEnabledChanged(
        override val enabled: Boolean,
    ) : Toggle

    /** 파티 정산 알림 (product-spec 4-9) */
    data class PartyEnabledChanged(
        override val enabled: Boolean,
    ) : Toggle

    /** 월초 요약 (product-spec 6장) */
    data class MonthlySummaryChanged(
        override val enabled: Boolean,
    ) : Toggle

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
