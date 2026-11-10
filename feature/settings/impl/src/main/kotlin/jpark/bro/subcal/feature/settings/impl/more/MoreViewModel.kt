package jpark.bro.subcal.feature.settings.impl.more

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

internal class MoreViewModel(
    private val settingsRepository: SettingsRepository,
) : MviViewModel<MoreState, MoreAction, MoreEvent>(MoreState()) {
    init {
        combine(
            settingsRepository.observeNotificationSettings(),
            settingsRepository.observeAppSettings(),
        ) { notification, appSettings ->
            setState { copy(isLoading = false, notification = notification, appSettings = appSettings) }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: MoreAction) {
        when (action) {
            MoreAction.NotificationSettingsClicked -> {
                sendEvent(MoreEvent.OpenNotificationSettings)
            }

            is MoreAction.AppVersionLoaded -> {
                setState { copy(appVersion = action.versionName) }
            }

            is MoreAction.OpenSheet -> {
                setState { openSheet(action) }
            }

            is MoreAction.SheetInput -> {
                setState { reduce(action) }
            }

            is MoreAction.MonthStartDaySelected -> {
                saveAppSettings { copy(monthStartDay = action.day) }
            }

            MoreAction.UsdKrwRateSaveClicked -> {
                val rate = currentState.usdKrwRateInput.toLongOrNull()?.takeIf { it > 0 } ?: return
                saveAppSettings { copy(usdKrwRate = rate) }
            }

            MoreAction.PaymentMethodSaveClicked -> {
                val method = currentState.paymentMethodInput.trim().ifEmpty { null }
                saveAppSettings { copy(defaultPaymentMethod = method) }
            }

            is MoreAction.ThemeModeSelected -> {
                saveAppSettings { copy(themeMode = action.mode) }
            }

            MoreAction.SheetDismissed -> {
                setState { copy(sheet = null) }
            }
        }
    }

    /** 시트를 닫고 저장한다. 실패하면 값은 그대로 두고 알린다 */
    private fun saveAppSettings(change: AppSettings.() -> AppSettings) {
        val updated = currentState.appSettings.change()
        setState { copy(sheet = null) }
        viewModelScope.launch {
            settingsRepository
                .setAppSettings(updated)
                .onFailure { sendEvent(MoreEvent.SaveFailed) }
        }
    }
}
