package jpark.bro.subcal.feature.subscription.impl.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalDangerButton
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalOutlineButton
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.SubCalDatePickerDialog
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import jpark.bro.subcal.feature.subscription.impl.R
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun SubscriptionDetailRoot(
    key: SubscriptionDetailKey,
    onOpenEdit: (id: String) -> Unit,
    onOpenHistory: (id: String) -> Unit,
    onOpenParty: (id: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SubscriptionDetailViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    val actionFailed = stringResource(R.string.subscription_detail_action_failed)
    val openFailed = stringResource(R.string.subscription_detail_open_failed)
    val usageRecorded = stringResource(R.string.subscription_detail_usage_recorded)
    val undo = stringResource(R.string.subscription_detail_usage_undo)
    val scope = rememberCoroutineScope()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            SubscriptionDetailEvent.NavigateBack -> {
                onBack()
            }

            is SubscriptionDetailEvent.OpenEdit -> {
                onOpenEdit(event.id)
            }

            is SubscriptionDetailEvent.OpenHistory -> {
                onOpenHistory(event.id)
            }

            is SubscriptionDetailEvent.OpenParty -> {
                onOpenParty(event.id)
            }

            is SubscriptionDetailEvent.OpenCancelPage -> {
                // 브라우저가 없거나 주소가 잘못돼도 돌아오면 "해지했나요?"로 표시할 수 있다
                runCatching { uriHandler.openUri(event.url) }.onFailure { toastHostState.showSnackbar(openFailed) }
            }

            is SubscriptionDetailEvent.UsageRecorded -> {
                // 토스트가 떠 있는 동안에도 다음 이벤트를 받도록 따로 띄운다. 새 토스트가 오면 이전 것은 닫힌다
                toastHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    val result =
                        toastHostState.showSnackbar(
                            usageRecorded,
                            actionLabel = undo,
                            duration = SnackbarDuration.Long,
                        )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onAction(SubscriptionDetailAction.UsageUndoClicked(event.usageLogId))
                    }
                }
            }

            SubscriptionDetailEvent.ActionFailed -> {
                toastHostState.showSnackbar(actionFailed)
            }
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onAction(SubscriptionDetailAction.Resumed) }

    Box(modifier = Modifier.fillMaxSize()) {
        SubscriptionDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 구독 상세 (product-spec 4-4) */
@Composable
private fun SubscriptionDetailScreen(
    state: SubscriptionDetailState,
    onAction: (SubscriptionDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("subscription_detail"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.subscription_detail_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.subscription_detail_back),
                    onClick = onBack,
                )
            },
            actions = {
                if (state.content != null) {
                    SubCalSecondaryButton(
                        text = stringResource(R.string.subscription_detail_edit),
                        onClick = { onAction(SubscriptionDetailAction.EditClicked) },
                        size = SubCalButtonSize.SMALL,
                        modifier = Modifier.testTag("edit_button"),
                    )
                    DetailMenu(
                        isOpen = state.isMenuOpen,
                        considerCancel = state.content.considerCancel,
                        showConsiderCancel = state.content.status != DetailStatus.CANCELLED,
                        onAction = onAction,
                    )
                }
            },
        )
        val content = state.content ?: return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DetailHeaderCard(content)
            StatCards(content)
            content.nextPayment?.let { NextPaymentCard(next = it, isTrial = content.status == DetailStatus.TRIAL) }
            content.usage?.let { UsageCard(usage = it, onAction = onAction) }
            content.party?.let { PartyCard(party = it, onClick = { onAction(SubscriptionDetailAction.PartyClicked) }) }
            HistoryCard(
                entries = content.recentHistory,
                onShowAll =
                    {
                        onAction(
                            SubscriptionDetailAction.ShowAllHistoryClicked,
                        )
                    }.takeIf { content.hasMoreHistory },
            )
            content.memo?.takeIf(String::isNotBlank)?.let { MemoCard(it) }
        }
        BottomButtons(status = content.status, enabled = !state.isWorking, onAction = onAction)
    }

    if (state.isCancelSheetOpen) {
        CancelSheet(state = state, onAction = onAction)
    }
    if (state.isUsageDatePickerOpen) {
        SubCalDatePickerDialog(
            initial = state.today,
            onConfirm = { onAction(SubscriptionDetailAction.UsageDatePicked(it)) },
            onDismiss = { onAction(SubscriptionDetailAction.UsageDatePickerDismissed) },
            maxDate = state.today,
        )
    }
    if (state.isCancelDatePickerOpen) {
        SubCalDatePickerDialog(
            initial = state.cancelDate,
            onConfirm = { onAction(SubscriptionDetailAction.CancelDatePicked(it)) },
            onDismiss = { onAction(SubscriptionDetailAction.CancelDatePickerDismissed) },
            maxDate = state.today,
        )
    }
}

/** 이용 중: 일시정지 · 해지하러 가기 / 일시정지: 다시 시작 · 해지 / 해지: 해지 취소 */
@Composable
private fun BottomButtons(
    status: DetailStatus,
    enabled: Boolean,
    onAction: (SubscriptionDetailAction) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(AppThemeTokens.colors.surface)
                .padding(AppSpacing.marginMobile),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        val buttonModifier = Modifier.weight(1f)
        when (status) {
            DetailStatus.ACTIVE, DetailStatus.TRIAL -> {
                SubCalSecondaryButton(
                    text = stringResource(R.string.subscription_detail_pause),
                    onClick = { onAction(SubscriptionDetailAction.PauseClicked) },
                    enabled = enabled,
                    modifier = buttonModifier.testTag("pause_button"),
                )
                SubCalOutlineButton(
                    text = stringResource(R.string.subscription_detail_go_cancel),
                    onClick = { onAction(SubscriptionDetailAction.CancelClicked) },
                    enabled = enabled,
                    modifier = buttonModifier.testTag("cancel_button"),
                )
            }

            DetailStatus.PAUSED -> {
                SubCalPrimaryButton(
                    text = stringResource(R.string.subscription_detail_resume),
                    onClick = { onAction(SubscriptionDetailAction.ResumeClicked) },
                    enabled = enabled,
                    modifier = buttonModifier.testTag("resume_button"),
                )
                SubCalOutlineButton(
                    text = stringResource(R.string.subscription_detail_cancel),
                    onClick = { onAction(SubscriptionDetailAction.CancelClicked) },
                    enabled = enabled,
                    modifier = buttonModifier.testTag("cancel_button"),
                )
            }

            DetailStatus.CANCELLED -> {
                SubCalSecondaryButton(
                    text = stringResource(R.string.subscription_detail_undo_cancel),
                    onClick = { onAction(SubscriptionDetailAction.UndoCancelClicked) },
                    enabled = enabled,
                    modifier = buttonModifier.testTag("undo_cancel_button"),
                )
            }
        }
    }
}

/** "해지했나요?": 해지일(기본 오늘, 오늘까지) + 해지로 표시 (product-spec 2-6) */
@Composable
private fun CancelSheet(
    state: SubscriptionDetailState,
    onAction: (SubscriptionDetailAction) -> Unit,
) {
    SubCalBottomSheet(onDismissRequest = { onAction(SubscriptionDetailAction.CancelSheetDismissed) }) {
        Text(
            text = stringResource(R.string.subscription_detail_cancel_title),
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
        )
        Text(
            text = stringResource(R.string.subscription_detail_cancel_message),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile),
        )
        SubCalListItem(
            title = stringResource(R.string.subscription_detail_cancel_date),
            onClick = { onAction(SubscriptionDetailAction.CancelDateClicked) },
            showChevron = false,
            trailing = {
                Text(
                    text = fullDate(state.cancelDate),
                    style = AppThemeTokens.typography.labelLg.copy(fontFeatureSettings = "tnum"),
                    color = AppThemeTokens.colors.textPrimary,
                )
            },
            modifier = Modifier.testTag("cancel_date"),
        )
        Column(
            modifier =
                Modifier.padding(
                    start = AppSpacing.marginMobile,
                    end = AppSpacing.marginMobile,
                    bottom = AppSpacing.xl,
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            SubCalDangerButton(
                text = stringResource(R.string.subscription_detail_cancel_confirm),
                onClick = { onAction(SubscriptionDetailAction.CancelConfirmed) },
                filled = true,
                modifier = Modifier.fillMaxWidth().testTag("cancel_confirm"),
            )
            SubCalSecondaryButton(
                text = stringResource(R.string.subscription_detail_cancel_not_yet),
                onClick = { onAction(SubscriptionDetailAction.CancelSheetDismissed) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun SubscriptionDetailPreview() {
    AppTheme { SubscriptionDetailScreen(state = previewDetailState(), onAction = {}, onBack = {}) }
}

@ScreenPreviews
@Composable
private fun SubscriptionDetailPausedPreview() {
    AppTheme { SubscriptionDetailScreen(state = previewDetailState(paused = true), onAction = {}, onBack = {}) }
}
