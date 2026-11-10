package jpark.bro.subcal.feature.insight.impl.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalSegment
import jpark.bro.subcal.core.designsystem.component.SubCalSegmentedControl
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.feature.insight.api.SavingsKey
import jpark.bro.subcal.feature.insight.api.SavingsTab
import jpark.bro.subcal.feature.insight.impl.R
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun SavingsRoot(
    key: SavingsKey,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: SavingsViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val toastHostState = remember { SnackbarHostState() }
    val openFailed = stringResource(R.string.insight_savings_open_failed)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            SavingsEvent.NavigateBack -> {
                onBack()
            }

            is SavingsEvent.OpenDetail -> {
                onNavigate(SubscriptionDetailKey(event.subscriptionId))
            }

            is SavingsEvent.OpenCancelPage -> {
                runCatching { uriHandler.openUri(event.url) }.onFailure { toastHostState.showSnackbar(openFailed) }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SavingsScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 아낀 돈 · 해지 고민 (product-spec 4-8) */
@Composable
private fun SavingsScreen(
    state: SavingsState,
    onAction: (SavingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("savings"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.insight_savings_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.insight_savings_back),
                    onClick = { onAction(SavingsAction.BackClicked) },
                )
            },
        )
        val tabs = SavingsTab.entries
        SubCalSegmentedControl(
            segments =
                tabs.map {
                    SubCalSegment(
                        stringResource(
                            when (it) {
                                SavingsTab.CONSIDER -> R.string.insight_savings_tab_consider
                                SavingsTab.SAVED -> R.string.insight_savings_tab_saved
                            },
                        ),
                    )
                },
            selectedIndex = tabs.indexOf(state.tab),
            onSelect = { onAction(SavingsAction.TabSelected(tabs[it])) },
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile).testTag("savings_tabs"),
        )
        if (state.isLoading) return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = AppSpacing.marginMobile,
                        end = AppSpacing.marginMobile,
                        top = AppSpacing.md,
                        bottom = AppSpacing.xl,
                    ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter),
        ) {
            when (state.tab) {
                SavingsTab.CONSIDER -> ConsiderTab(state = state, onAction = onAction)
                SavingsTab.SAVED -> SavedTab(state = state, onAction = onAction)
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun SavingsConsiderPreview() {
    AppTheme { SavingsScreen(state = previewSavingsState(SavingsTab.CONSIDER), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun SavingsSavedPreview() {
    AppTheme { SavingsScreen(state = previewSavingsState(SavingsTab.SAVED), onAction = {}) }
}
