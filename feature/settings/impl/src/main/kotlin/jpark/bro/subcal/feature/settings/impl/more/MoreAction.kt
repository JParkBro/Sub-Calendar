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

    /** 백업 내보내기 · 불러오기 (product-spec 6장) */
    sealed interface Backup : MoreAction

    data object ExportClicked : Backup

    /** Root가 고른 위치에 파일을 썼는지. 고르지 않고 닫으면 보내지 않는다 */
    data class ExportFileWritten(
        val success: Boolean,
    ) : Backup

    data object ImportClicked : Backup

    /** Root가 고른 파일에서 읽은 내용. 읽지 못했으면 null */
    data class BackupFileRead(
        val json: String?,
    ) : Backup

    data object RestoreConfirmed : Backup
}
