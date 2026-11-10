package jpark.bro.subcal.feature.insight.impl.value

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBox
import jpark.bro.subcal.core.designsystem.component.SubCalOutlineButton
import jpark.bro.subcal.core.designsystem.component.SubCalProgressBar
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.insight.impl.R
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun ValueAnalysisRoot(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: ValueAnalysisViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val added = stringResource(R.string.insight_value_consider_added)
    val failed = stringResource(R.string.insight_value_action_failed)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            ValueAnalysisEvent.NavigateBack -> onBack()
            is ValueAnalysisEvent.OpenDetail -> onNavigate(SubscriptionDetailKey(event.subscriptionId))
            ValueAnalysisEvent.AddedToConsider -> toastHostState.showSnackbar(added)
            ValueAnalysisEvent.ActionFailed -> toastHostState.showSnackbar(failed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        ValueAnalysisScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 가성비 분석 (product-spec 4-10) */
@Composable
private fun ValueAnalysisScreen(
    state: ValueAnalysisState,
    onAction: (ValueAnalysisAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("value_analysis"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.insight_value_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.insight_value_back),
                    onClick = { onAction(ValueAnalysisAction.BackClicked) },
                )
            },
        )
        if (state.isLoading) return@Column
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding =
                PaddingValues(
                    start = AppSpacing.marginMobile,
                    end = AppSpacing.marginMobile,
                    top = AppSpacing.md,
                    bottom = AppSpacing.xl,
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item(key = "header") { Header(state) }
            if (state.items.isEmpty()) {
                item(key = "empty") { EmptyCard() }
            } else {
                item(key = "tip") { SubCalInfoBox(text = stringResource(R.string.insight_value_tip)) }
                items(state.items, key = { "item_${it.id}" }) { item ->
                    ValueItemCard(item = item, onClick = { onAction(ValueAnalysisAction.SubscriptionClicked(item.id)) })
                }
                items(state.considerSuggestions, key = { "suggest_${it.id}" }) { suggestion ->
                    ConsiderSuggestionCard(
                        suggestion = suggestion,
                        enabled = !state.isWorking,
                        onAdd = { onAction(ValueAnalysisAction.AddToConsiderClicked(suggestion.id)) },
                    )
                }
                item(key = "footer") { SubCalInfoBox(text = stringResource(R.string.insight_value_footer)) }
            }
        }
    }
}

@Composable
private fun Header(state: ValueAnalysisState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.insight_value_header),
            style = AppThemeTokens.typography.labelLg,
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.insight_value_month, state.month.monthValue),
            style = AppThemeTokens.typography.labelMd,
            color = AppThemeTokens.colors.textTertiary,
        )
    }
}

/** 사용 체크를 켠 이용 중 구독이 없을 때 */
@Composable
private fun EmptyCard() {
    SubCalCard {
        Column(
            modifier = Modifier.padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.insight_value_empty_title),
                style = AppThemeTokens.typography.labelLg,
                color = AppThemeTokens.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.insight_value_empty_body),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
            )
        }
    }
}

/** 아이콘 · 이름 · "n회 사용" + 1회당 비용(사용 안 함은 danger) + 막대 */
@Composable
private fun ValueItemCard(
    item: ValueItem,
    onClick: () -> Unit,
) {
    val colors = AppThemeTokens.colors
    val cost = item.costPerUseKrw
    SubCalCard(onClick = onClick, modifier = Modifier.testTag("value_${item.id}")) {
        Column(
            modifier = Modifier.padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                SubCalServiceIcon(name = item.name, colorKey = item.colorKey)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(text = item.name, style = AppThemeTokens.typography.labelLg, color = colors.textPrimary)
                    Text(
                        text =
                            stringResource(
                                if (item.isParty) {
                                    R.string.insight_value_use_count_party
                                } else {
                                    R.string.insight_value_use_count
                                },
                                item.useCount,
                            ),
                        style = AppThemeTokens.typography.bodySm.copy(fontFeatureSettings = "tnum"),
                        color = if (cost == null) colors.danger else colors.textSecondary,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    if (cost == null) {
                        Text(
                            text = stringResource(R.string.insight_value_unused),
                            style = AppThemeTokens.typography.labelLg,
                            color = colors.danger,
                        )
                        Text(
                            text =
                                stringResource(
                                    R.string.insight_value_monthly,
                                    formatMoney(Money(item.monthlyKrw, Currency.KRW)),
                                ),
                            style = AppThemeTokens.typography.bodySm.copy(fontFeatureSettings = "tnum"),
                            color = colors.textTertiary,
                            textAlign = TextAlign.End,
                        )
                    } else {
                        MoneyText(money = Money(cost, Currency.KRW), style = AppThemeTokens.typography.labelLg)
                        Text(
                            text = stringResource(R.string.insight_value_per_use),
                            style = AppThemeTokens.typography.bodySm,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
            SubCalProgressBar(
                progress = item.barFraction,
                height = AppSpacing.sm,
                color = if (cost == null) colors.danger else null,
            )
        }
    }
}

/** "해지 고민함에 담을까요?" (product-spec 4-10) */
@Composable
private fun ConsiderSuggestionCard(
    suggestion: ConsiderSuggestion,
    enabled: Boolean,
    onAdd: () -> Unit,
) {
    SubCalCard(modifier = Modifier.testTag("suggest_${suggestion.id}")) {
        Row(
            modifier = Modifier.padding(AppSpacing.marginMobile),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = stringResource(R.string.insight_value_suggest_title),
                    style = AppThemeTokens.typography.labelLg,
                    color = AppThemeTokens.colors.textPrimary,
                )
                Text(
                    text =
                        stringResource(
                            R.string.insight_value_suggest_body,
                            suggestion.name,
                            formatMoney(Money(suggestion.monthlyKrw, Currency.KRW)),
                        ),
                    style = AppThemeTokens.typography.bodySm,
                    color = AppThemeTokens.colors.textSecondary,
                )
            }
            SubCalOutlineButton(
                text = stringResource(R.string.insight_value_suggest_add),
                onClick = onAdd,
                enabled = enabled,
                size = SubCalButtonSize.SMALL,
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun ValueAnalysisPreview() {
    AppTheme { ValueAnalysisScreen(state = previewValueAnalysisState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun ValueAnalysisEmptyPreview() {
    AppTheme { ValueAnalysisScreen(state = ValueAnalysisState(isLoading = false), onAction = {}) }
}
