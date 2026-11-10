package jpark.bro.subcal.feature.settings.impl.more

internal sealed interface MoreEvent {
    data object OpenNotificationSettings : MoreEvent

    data object OpenTrial : MoreEvent

    data object OpenParty : MoreEvent

    data object OpenWidgetSettings : MoreEvent

    data class OpenYearReport(
        val year: Int,
    ) : MoreEvent

    data object SaveFailed : MoreEvent

    /** 시스템 파일 저장 창을 열어 [json]을 [fileName]으로 저장한다 */
    data class SaveBackupFile(
        val fileName: String,
        val json: String,
    ) : MoreEvent

    /** 시스템 파일 선택 창을 연다 */
    data object OpenBackupFile : MoreEvent

    data class ShowBackupMessage(
        val message: BackupMessage,
    ) : MoreEvent

    /** 백업을 불러왔다: app이 홈으로 보낸다 */
    data object DataRestored : MoreEvent

    /** 모든 데이터를 지웠다: 떠 있는 알림을 지우고 app이 온보딩으로 보낸다 */
    data object DataDeleted : MoreEvent
}

internal enum class BackupMessage {
    EXPORTED,
    EXPORT_FAILED,
    INVALID_FILE,
    RESTORE_FAILED,
    DELETE_FAILED,
}
