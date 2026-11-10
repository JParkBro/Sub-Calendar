package jpark.bro.subcal.feature.settings.impl.notification

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.domain.reminder.reminderLeadDays
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class NotificationSettingsViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository,
) : MviViewModel<NotificationSettingsState, NotificationSettingsAction, NotificationSettingsEvent>(
        NotificationSettingsState(),
    ) {
    /** 저장하지 않는 화면 값 */
    private val ui = MutableStateFlow(UiValues())

    init {
        combine(
            subscriptionRepository.observeRecords(),
            settingsRepository.observeNotificationSettings(),
            ui,
        ) { records, settings, ui ->
            val items = records.toReminderItems(settings)
            setState {
                copy(
                    isLoading = false,
                    deviceNotificationsEnabled = ui.deviceNotificationsEnabled,
                    settings = settings,
                    subscriptions = items,
                    isTimeSheetVisible = ui.isTimeSheetVisible,
                    // 고르는 사이 구독이 삭제 · 해지되면 시트를 닫는다
                    editing = ui.editingId?.let { id -> items.firstOrNull { it.id == id } },
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: NotificationSettingsAction) {
        when (action) {
            NotificationSettingsAction.BackClicked -> {
                sendEvent(NotificationSettingsEvent.NavigateBack)
            }

            is NotificationSettingsAction.DeviceNotificationsChecked -> {
                ui.update { it.copy(deviceNotificationsEnabled = action.enabled) }
            }

            NotificationSettingsAction.OpenSystemSettingsClicked -> {
                sendEvent(NotificationSettingsEvent.OpenSystemSettings)
            }

            is NotificationSettingsAction.PaymentEnabledChanged -> {
                saveSettings { copy(paymentEnabled = action.enabled) }
            }

            is NotificationSettingsAction.DefaultDaysBeforeSelected -> {
                saveSettings { copy(defaultDaysBefore = action.daysBefore) }
            }

            NotificationSettingsAction.TimeClicked -> {
                ui.update { it.copy(isTimeSheetVisible = true) }
            }

            is NotificationSettingsAction.TimeSelected -> {
                ui.update { it.copy(isTimeSheetVisible = false) }
                saveSettings { copy(time = action.time) }
            }

            NotificationSettingsAction.TimeSheetDismissed -> {
                ui.update { it.copy(isTimeSheetVisible = false) }
            }

            is NotificationSettingsAction.SubscriptionClicked -> {
                ui.update { it.copy(editingId = action.subscriptionId) }
            }

            is NotificationSettingsAction.SubscriptionReminderSelected -> {
                val id = currentState.editing?.id ?: return
                ui.update { it.copy(editingId = null) }
                viewModelScope.launch {
                    subscriptionRepository
                        .setReminder(id, action.choice.mode, action.choice.daysBefore)
                        .onFailure { sendEvent(NotificationSettingsEvent.SaveFailed) }
                }
            }

            NotificationSettingsAction.SubscriptionSheetDismissed -> {
                ui.update { it.copy(editingId = null) }
            }
        }
    }

    private fun saveSettings(change: NotificationSettings.() -> NotificationSettings) {
        val updated = currentState.settings.change()
        viewModelScope.launch {
            settingsRepository
                .setNotificationSettings(updated)
                .onFailure { sendEvent(NotificationSettingsEvent.SaveFailed) }
        }
    }

    private data class UiValues(
        val deviceNotificationsEnabled: Boolean = true,
        val isTimeSheetVisible: Boolean = false,
        val editingId: String? = null,
    )
}

private fun List<SubscriptionRecord>.toReminderItems(settings: NotificationSettings): List<SubscriptionReminderItem> =
    map { it.subscription }
        .filter { it.status != SubscriptionStatus.CANCELLED }
        .sortedBy { it.name }
        .map { subscription ->
            SubscriptionReminderItem(
                id = subscription.id,
                name = subscription.name,
                colorKey = subscription.colorKey,
                cycle = subscription.cycle,
                firstBillingDate = subscription.firstBillingDate,
                choice = ReminderChoice.of(subscription),
                leadDays = subscription.reminderLeadDays(settings),
            )
        }
