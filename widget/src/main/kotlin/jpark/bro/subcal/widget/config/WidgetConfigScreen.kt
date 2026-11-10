package jpark.bro.subcal.widget.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.widget.R
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun WidgetConfigRoot(
    viewModel: WidgetConfigViewModel,
    onClose: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.widget_config_save_failed)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            WidgetConfigEvent.Close -> onClose()
            WidgetConfigEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        WidgetConfigScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 위젯 설정 (product-spec 7장): 미리보기 + 배경(테마 · 다크 · 화이트) */
@Composable
private fun WidgetConfigScreen(
    state: WidgetConfigState,
    onAction: (WidgetConfigAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("widget_config"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.widget_config_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_close,
                    contentDescription = stringResource(R.string.widget_config_close),
                    onClick = { onAction(WidgetConfigAction.CloseClicked) },
                )
            },
        )
        if (state.isLoading) return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Text(
                text = stringResource(R.string.widget_config_preview_hint),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
            )
            state.display?.let {
                WidgetPreviewCard(
                    display = it,
                    size = state.size,
                    background = state.background,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Text(
                text = stringResource(R.string.widget_config_background),
                style = AppThemeTokens.typography.labelMd,
                color = AppThemeTokens.colors.textSecondary,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                WidgetBackground.entries.forEach { background ->
                    SubCalFilterChip(
                        text = stringResource(background.labelRes),
                        selected = background == state.background,
                        onClick = { onAction(WidgetConfigAction.BackgroundSelected(background)) },
                        modifier = Modifier.testTag("background_${background.name.lowercase()}"),
                    )
                }
            }
        }
        SubCalPrimaryButton(
            text = stringResource(R.string.widget_config_save),
            onClick = { onAction(WidgetConfigAction.SaveClicked) },
            enabled = !state.isSaving,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.marginMobile)
                    .testTag("widget_config_save"),
        )
    }
}

private val WidgetBackground.labelRes: Int
    get() =
        when (this) {
            WidgetBackground.THEME -> R.string.widget_config_background_theme
            WidgetBackground.DARK -> R.string.widget_config_background_dark
            WidgetBackground.WHITE -> R.string.widget_config_background_white
        }

@ScreenPreviews
@Composable
private fun WidgetConfigPreview() {
    AppTheme {
        WidgetConfigScreen(
            state =
                WidgetConfigState(
                    widgetId = 1,
                    size = WidgetSize.MEDIUM,
                    isLoading = false,
                    background = WidgetBackground.DARK,
                    display = previewWidgetDisplay(),
                ),
            onAction = {},
        )
    }
}
