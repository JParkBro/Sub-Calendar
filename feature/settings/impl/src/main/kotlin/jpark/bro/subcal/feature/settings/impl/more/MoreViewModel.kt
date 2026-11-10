package jpark.bro.subcal.feature.settings.impl.more

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class MoreViewModel(
    settingsRepository: SettingsRepository,
) : MviViewModel<MoreState, MoreAction, MoreEvent>(MoreState()) {
    init {
        settingsRepository
            .observeNotificationSettings()
            .onEach { settings -> setState { copy(isLoading = false, notification = settings) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: MoreAction) {
        when (action) {
            MoreAction.NotificationSettingsClicked -> sendEvent(MoreEvent.OpenNotificationSettings)
        }
    }
}
