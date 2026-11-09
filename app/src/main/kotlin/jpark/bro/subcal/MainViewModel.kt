package jpark.bro.subcal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.model.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** 앱 전체 화면 모드 */
internal class MainViewModel(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val themeMode: StateFlow<AppThemeMode> =
        settingsRepository
            .observeAppSettings()
            .map { it.themeMode }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AppThemeMode.SYSTEM)
}
