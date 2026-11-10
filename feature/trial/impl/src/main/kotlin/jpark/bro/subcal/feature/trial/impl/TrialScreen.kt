package jpark.bro.subcal.feature.trial.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBadge
import jpark.bro.subcal.core.designsystem.component.SubCalBadgeTone
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDdayText
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalProgressBar
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalSection
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionEditKey
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun TrialRoot(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: TrialViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val failed = stringResource(R.string.trial_action_failed)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            TrialEvent.NavigateBack -> onBack()
            TrialEvent.OpenAddTrial -> onNavigate(SubscriptionEditKey(id = null, startAsTrial = true))
            is TrialEvent.OpenDetail -> onNavigate(SubscriptionDetailKey(event.subscriptionId))
            TrialEvent.ActionFailed -> toastHostState.showSnackbar(failed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        TrialScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 무료체험 (product-spec 4-7) */
@Composable
private fun TrialScreen(
    state: TrialState,
    onAction: (TrialAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("trial"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.trial_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.trial_back),
                    onClick = { onAction(TrialAction.BackClicked) },
                )
            },
            actions = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_add,
                    contentDescription = stringResource(R.string.trial_add),
                    onClick = { onAction(TrialAction.AddClicked) },
                    modifier = Modifier.testTag("trial_add"),
                )
            },
        )
        if (state.isLoading) return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = AppSpacing.marginMobile, end = AppSpacing.marginMobile, bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.trial_active_title),
                    style = AppThemeTokens.typography.headlineSm,
                    color = AppThemeTokens.colors.textPrimary,
                )
                SubCalBadge(
                    text = stringResource(R.string.trial_active_count, state.active.size),
                    tone = SubCalBadgeTone.CATEGORY,
                )
            }
            if (state.active.isEmpty()) {
                Text(
                    text = stringResource(R.string.trial_active_empty),
                    style = AppThemeTokens.typography.bodyMd,
                    color = AppThemeTokens.colors.textSecondary,
                )
            }
            state.active.forEach { ActiveTrialCard(trial = it, isWorking = state.isWorking, onAction = onAction) }
            EndedSection(ended = state.ended, onAction = onAction)
        }
    }
}

/** 진행 중인 체험 카드: 종료 1일 이내는 danger 테두리 */
@Composable
private fun ActiveTrialCard(
    trial: ActiveTrial,
    isWorking: Boolean,
    onAction: (TrialAction) -> Unit,
) {
    val colors = AppThemeTokens.colors
    SubCalCard(
        modifier =
            Modifier
                .then(if (trial.isUrgent) Modifier.border(2.dp, colors.danger, AppShapes.md) else Modifier)
                .testTag("active_${trial.id}"),
        onClick = { onAction(TrialAction.TrialClicked(trial.id)) },
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                SubCalServiceIcon(name = trial.name, colorKey = trial.colorKey)
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = trial.name, style = AppThemeTokens.typography.labelLg, color = colors.textPrimary)
                    Text(
                        text =
                            stringResource(
                                R.string.trial_active_charge,
                                trial.firstBillingDate.monthValue,
                                trial.firstBillingDate.dayOfMonth,
                                formatMoney(trial.amount),
                            ),
                        style = AppThemeTokens.typography.bodySm,
                        color = colors.textSecondary,
                    )
                }
                SubCalDdayText(daysUntil = trial.daysUntil)
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = stringResource(R.string.trial_active_days, trial.elapsedDays, trial.totalDays),
                    style = AppThemeTokens.typography.labelSm,
                    color = colors.textTertiary,
                    modifier = Modifier.align(Alignment.End),
                )
                SubCalProgressBar(progress = trial.progress)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                SubCalSecondaryButton(
                    text = stringResource(R.string.trial_cancelled),
                    onClick = { onAction(TrialAction.CancelledClicked(trial.id)) },
                    enabled = !isWorking,
                    size = SubCalButtonSize.SMALL,
                    modifier = Modifier.weight(1f).testTag("cancelled_${trial.id}"),
                )
                SubCalPrimaryButton(
                    text = stringResource(R.string.trial_keep),
                    onClick = { onAction(TrialAction.KeepClicked(trial.id)) },
                    enabled = !isWorking,
                    size = SubCalButtonSize.SMALL,
                    modifier = Modifier.weight(1f).testTag("keep_${trial.id}"),
                )
            }
        }
    }
}

/** 지난 체험: 결과 칩 (product-spec 4-7) */
@Composable
private fun EndedSection(
    ended: List<EndedTrial>,
    onAction: (TrialAction) -> Unit,
) {
    SubCalSection(title = stringResource(R.string.trial_ended_title)) {
        if (ended.isEmpty()) {
            SubCalListItem(title = stringResource(R.string.trial_ended_empty))
        }
        ended.forEachIndexed { index, trial ->
            if (index > 0) SubCalDivider()
            SubCalListItem(
                title = trial.name,
                subtitle = stringResource(R.string.trial_ended_on, trial.endedOn.monthValue, trial.endedOn.dayOfMonth),
                onClick = { onAction(TrialAction.TrialClicked(trial.id)) },
                showChevron = false,
                leading = { SubCalServiceIcon(name = trial.name, colorKey = trial.colorKey, dimmed = trial.cancelled) },
                trailing = {
                    if (trial.cancelled) {
                        SubCalBadge(
                            text =
                                stringResource(
                                    R.string.trial_ended_saved,
                                    formatMoney(Money(trial.savedKrw, Currency.KRW)),
                                ),
                            tone = SubCalBadgeTone.SUCCESS,
                        )
                    } else {
                        SubCalBadge(text = stringResource(R.string.trial_ended_kept), tone = SubCalBadgeTone.CYCLE)
                    }
                },
                modifier = Modifier.testTag("ended_${trial.id}"),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun TrialPreview() {
    AppTheme { TrialScreen(state = previewTrialState(), onAction = {}) }
}
