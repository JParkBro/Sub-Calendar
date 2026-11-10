package jpark.bro.subcal.feature.subscription.impl.detail

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.data.repository.UsageRepository
import jpark.bro.subcal.core.ui.MviViewModel
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class SubscriptionDetailViewModel(
    key: SubscriptionDetailKey,
    private val subscriptionRepository: SubscriptionRepository,
    private val usageRepository: UsageRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<SubscriptionDetailState, SubscriptionDetailAction, SubscriptionDetailEvent>(
        SubscriptionDetailState(),
    ) {
    private val id = key.id
    private val today: LocalDate get() = LocalDate.now(clock)

    /** 해지 페이지 주소. 브라우저로 열 때만 쓰고 화면에는 있는지만 보여준다 */
    private var cancelUrl: String? = null

    /** 삭제돼서 이미 뒤로 보냈다 */
    private var isClosed = false

    init {
        combine(
            subscriptionRepository.observeRecord(id),
            settingsRepository.observeAppSettings(),
            usageRepository.observeUsageLogs(),
        ) { record, settings, usageLogs ->
            if (record == null) {
                if (!isClosed) {
                    isClosed = true
                    sendEvent(SubscriptionDetailEvent.NavigateBack)
                }
                return@combine
            }
            cancelUrl = record.subscription.cancelUrl
            val today = today
            setState {
                copy(
                    isLoading = false,
                    today = today,
                    content = buildDetailContent(record, today, settings, usageLogs),
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: SubscriptionDetailAction) {
        when (action) {
            SubscriptionDetailAction.EditClicked -> {
                sendEvent(SubscriptionDetailEvent.OpenEdit(id))
            }

            SubscriptionDetailAction.PauseClicked -> {
                write { subscriptionRepository.pause(id) }
            }

            SubscriptionDetailAction.ResumeClicked -> {
                write { subscriptionRepository.resume(id) }
            }

            SubscriptionDetailAction.CancelClicked -> {
                onCancelClicked()
            }

            SubscriptionDetailAction.Resumed -> {
                onResumed()
            }

            is SubscriptionDetailAction.CancelSheet -> {
                onCancelSheetAction(action)
            }

            SubscriptionDetailAction.UndoCancelClicked -> {
                write { subscriptionRepository.undoCancel(id) }
            }

            SubscriptionDetailAction.ShowAllHistoryClicked -> {
                sendEvent(SubscriptionDetailEvent.OpenHistory(id))
            }

            is SubscriptionDetailAction.Menu -> {
                onMenuAction(action)
            }

            is SubscriptionDetailAction.Usage -> {
                onUsageAction(action)
            }
        }
    }

    private fun onUsageAction(action: SubscriptionDetailAction.Usage) {
        when (action) {
            SubscriptionDetailAction.UsageAddClicked -> {
                addUsage(today)
            }

            SubscriptionDetailAction.UsageAddLongClicked -> {
                setState { copy(isUsageDatePickerOpen = true) }
            }

            is SubscriptionDetailAction.UsageDatePicked -> {
                setState { copy(isUsageDatePickerOpen = false) }
                addUsage(action.date)
            }

            SubscriptionDetailAction.UsageDatePickerDismissed -> {
                setState { copy(isUsageDatePickerOpen = false) }
            }

            is SubscriptionDetailAction.UsageUndoClicked -> {
                viewModelScope.launch {
                    usageRepository
                        .remove(
                            action.usageLogId,
                        ).onFailure { sendEvent(SubscriptionDetailEvent.ActionFailed) }
                }
            }
        }
    }

    private fun addUsage(date: LocalDate) {
        viewModelScope.launch {
            usageRepository
                .add(id, date)
                .onSuccess { sendEvent(SubscriptionDetailEvent.UsageRecorded(it)) }
                .onFailure { sendEvent(SubscriptionDetailEvent.ActionFailed) }
        }
    }

    private fun onCancelSheetAction(action: SubscriptionDetailAction.CancelSheet) {
        when (action) {
            SubscriptionDetailAction.CancelDateClicked -> {
                setState { copy(isCancelDatePickerOpen = true) }
            }

            is SubscriptionDetailAction.CancelDatePicked -> {
                setState { copy(cancelDate = action.date, isCancelDatePickerOpen = false) }
            }

            SubscriptionDetailAction.CancelDatePickerDismissed -> {
                setState { copy(isCancelDatePickerOpen = false) }
            }

            SubscriptionDetailAction.CancelConfirmed -> {
                onCancelConfirmed()
            }

            SubscriptionDetailAction.CancelSheetDismissed -> {
                setState { copy(isCancelSheetOpen = false) }
            }
        }
    }

    private fun onMenuAction(action: SubscriptionDetailAction.Menu) {
        when (action) {
            SubscriptionDetailAction.MenuClicked -> {
                setState { copy(isMenuOpen = true) }
            }

            SubscriptionDetailAction.MenuDismissed -> {
                setState { copy(isMenuOpen = false) }
            }

            SubscriptionDetailAction.ConsiderCancelToggled -> {
                setState { copy(isMenuOpen = false) }
                val considerCancel = currentState.content?.considerCancel ?: return
                write { subscriptionRepository.setConsiderCancel(id, !considerCancel) }
            }
        }
    }

    /** 이용 중이고 해지 주소가 있으면 브라우저로, 아니면 바로 해지로 표시 시트 (product-spec 2-6) */
    private fun onCancelClicked() {
        val url = cancelUrl?.trim()?.takeIf(String::isNotEmpty)
        val status = currentState.content?.status
        if (url != null && (status == DetailStatus.ACTIVE || status == DetailStatus.TRIAL)) {
            setState { copy(isAwaitingCancelReturn = true) }
            sendEvent(SubscriptionDetailEvent.OpenCancelPage(if ("://" in url) url else "https://$url"))
        } else {
            openCancelSheet()
        }
    }

    /** 해지 페이지에서 돌아오면 "해지했나요?" */
    private fun onResumed() {
        if (!currentState.isAwaitingCancelReturn) return
        setState { copy(isAwaitingCancelReturn = false) }
        openCancelSheet()
    }

    private fun openCancelSheet() {
        setState { copy(isCancelSheetOpen = true, cancelDate = today) }
    }

    private fun onCancelConfirmed() {
        val date = currentState.cancelDate
        setState { copy(isCancelSheetOpen = false) }
        write { subscriptionRepository.cancel(id, date) }
    }

    private fun write(block: suspend () -> Result<Unit>) {
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            block().onFailure { sendEvent(SubscriptionDetailEvent.ActionFailed) }
            setState { copy(isWorking = false) }
        }
    }
}
