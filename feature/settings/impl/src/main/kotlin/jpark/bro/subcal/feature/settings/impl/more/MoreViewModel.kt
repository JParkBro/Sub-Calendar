package jpark.bro.subcal.feature.settings.impl.more

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.BackupRepository
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class MoreViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val clock: Clock,
) : MviViewModel<MoreState, MoreAction, MoreEvent>(MoreState()) {
    /** 확인을 기다리는 백업 내용. 크기가 커서 State에 두지 않는다 */
    private var pendingRestoreJson: String? = null

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

            MoreAction.TrialClicked -> {
                sendEvent(MoreEvent.OpenTrial)
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
                pendingRestoreJson = null
                setState { copy(sheet = null, restorePreview = null) }
            }

            is MoreAction.Data -> {
                onDataAction(action)
            }
        }
    }

    private fun onDataAction(action: MoreAction.Data) {
        when (action) {
            MoreAction.ExportClicked -> {
                export()
            }

            is MoreAction.ExportFileWritten -> {
                showBackupMessage(
                    if (action.success) BackupMessage.EXPORTED else BackupMessage.EXPORT_FAILED,
                )
            }

            MoreAction.ImportClicked -> {
                sendEvent(MoreEvent.OpenBackupFile)
            }

            is MoreAction.BackupFileRead -> {
                inspect(action.json)
            }

            MoreAction.RestoreConfirmed -> {
                restore()
            }

            MoreAction.DeleteAllConfirmed -> {
                deleteAll()
            }
        }
    }

    private fun deleteAll() {
        setState { copy(sheet = null, deleteAllInput = "", isBackupBusy = true) }
        viewModelScope.launch {
            backupRepository
                .deleteAll()
                .onSuccess { sendEvent(MoreEvent.DataDeleted) }
                .onFailure { showBackupMessage(BackupMessage.DELETE_FAILED) }
            setState { copy(isBackupBusy = false) }
        }
    }

    private fun export() {
        if (currentState.isBackupBusy) return
        setState { copy(isBackupBusy = true) }
        viewModelScope.launch {
            backupRepository
                .export(appVersion = currentState.appVersion)
                .onSuccess { json -> sendEvent(MoreEvent.SaveBackupFile(backupFileName(LocalDate.now(clock)), json)) }
                .onFailure { showBackupMessage(BackupMessage.EXPORT_FAILED) }
            setState { copy(isBackupBusy = false) }
        }
    }

    /** 파일을 검사해 맞으면 확인 시트를 연다 */
    private fun inspect(json: String?) {
        if (json == null) {
            showBackupMessage(BackupMessage.INVALID_FILE)
            return
        }
        viewModelScope.launch {
            backupRepository
                .inspect(json)
                .onSuccess { summary ->
                    pendingRestoreJson = json
                    setState { copy(sheet = MoreSheet.RESTORE_CONFIRM, restorePreview = summary) }
                }.onFailure { showBackupMessage(BackupMessage.INVALID_FILE) }
        }
    }

    private fun restore() {
        val json = pendingRestoreJson ?: return
        pendingRestoreJson = null
        setState { copy(sheet = null, restorePreview = null, isBackupBusy = true) }
        viewModelScope.launch {
            backupRepository
                .restore(json)
                .onSuccess { sendEvent(MoreEvent.DataRestored) }
                .onFailure { showBackupMessage(BackupMessage.RESTORE_FAILED) }
            setState { copy(isBackupBusy = false) }
        }
    }

    private fun showBackupMessage(message: BackupMessage) {
        sendEvent(MoreEvent.ShowBackupMessage(message))
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

private val BackupFileDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

/** 백업 파일 이름: subcal-backup-yyyyMMdd.json (product-spec 6장) */
internal fun backupFileName(date: LocalDate): String = "subcal-backup-${date.format(BackupFileDate)}.json"
