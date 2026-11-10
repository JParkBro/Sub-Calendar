package jpark.bro.subcal.feature.settings.impl.more

import jpark.bro.subcal.core.model.AppThemeMode

internal sealed interface MoreAction {
    /** 설정 값을 고치는 시트를 연다 */
    sealed interface OpenSheet : MoreAction

    /** 시트 안 입력 */
    sealed interface SheetInput : MoreAction

    data object NotificationSettingsClicked : MoreAction

    /** Root가 기기에서 읽은 앱 버전 이름 */
    data class AppVersionLoaded(
        val versionName: String,
    ) : MoreAction

    data object MonthStartDayClicked : OpenSheet

    data class MonthStartDaySelected(
        val day: Int,
    ) : MoreAction

    data object UsdKrwRateClicked : OpenSheet

    data class UsdKrwRateInputChanged(
        val input: String,
    ) : SheetInput

    data object UsdKrwRateSaveClicked : MoreAction

    data object PaymentMethodClicked : OpenSheet

    data class PaymentMethodInputChanged(
        val input: String,
    ) : SheetInput

    data object PaymentMethodSaveClicked : MoreAction

    data object ThemeModeClicked : OpenSheet

    data class ThemeModeSelected(
        val mode: AppThemeMode,
    ) : MoreAction

    data object SheetDismissed : MoreAction
}
