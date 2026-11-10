package jpark.bro.subcal.feature.settings.impl.more

import jpark.bro.subcal.core.model.AppThemeMode

internal sealed interface MoreAction {
    /** 설정 값을 고치는 시트를 연다 */
    sealed interface OpenSheet : MoreAction

    /** 시트 안 입력 */
    sealed interface SheetInput : MoreAction

    /** 다른 화면으로 가는 메뉴 */
    sealed interface Navigate : MoreAction

    data object NotificationSettingsClicked : Navigate

    /** 관리 → 무료체험 */
    data object TrialClicked : Navigate

    /** 관리 → 파티 관리 */
    data object PartyClicked : Navigate

    /** 관리 → 연말 리포트 (올해) */
    data object YearReportClicked : Navigate

    /** 위젯 설정 */
    data object WidgetClicked : Navigate

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

    /** 백업 내보내기 · 불러오기 · 모든 데이터 삭제 (product-spec 6장) */
    sealed interface Data : MoreAction

    data object ExportClicked : Data

    /** Root가 고른 위치에 파일을 썼는지. 고르지 않고 닫으면 보내지 않는다 */
    data class ExportFileWritten(
        val success: Boolean,
    ) : Data

    data object ImportClicked : Data

    /** Root가 고른 파일에서 읽은 내용. 읽지 못했으면 null */
    data class BackupFileRead(
        val json: String?,
    ) : Data

    data object RestoreConfirmed : Data

    data object DeleteAllClicked : OpenSheet

    /** 확인 문구 입력 */
    data class DeleteAllInputChanged(
        val input: String,
    ) : SheetInput

    /** 확인 문구를 맞게 넣어야 화면이 보낸다 */
    data object DeleteAllConfirmed : Data
}
