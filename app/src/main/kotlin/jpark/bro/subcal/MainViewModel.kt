package jpark.bro.subcal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.navigation.startKeyFor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** 앱 전체 화면 모드와 첫 화면 */
internal class MainViewModel(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    /** 첫 화면 (module-map 4-3). 저장된 온보딩 완료 여부를 읽기 전에는 null이라 아무것도 그리지 않는다 */
    val startKey: StateFlow<NavKey?> =
        flow { emit(startKeyFor(settingsRepository.observeOnboardingDone().first())) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val themeMode: StateFlow<AppThemeMode> =
        settingsRepository
            .observeAppSettings()
            .map { it.themeMode }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AppThemeMode.SYSTEM)
}
