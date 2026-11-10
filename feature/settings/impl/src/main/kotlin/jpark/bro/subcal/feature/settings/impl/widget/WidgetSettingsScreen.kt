package jpark.bro.subcal.feature.settings.impl.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBox
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetConfig
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.feature.settings.impl.R
import jpark.bro.subcal.feature.settings.impl.ui.SheetTitle
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun WidgetSettingsRoot(
    onBack: () -> Unit,
    viewModel: WidgetSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val addFailed = stringResource(R.string.settings_widget_add_failed)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            WidgetSettingsEvent.NavigateBack -> onBack()
            WidgetSettingsEvent.AddFailed -> toastHostState.showSnackbar(addFailed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        WidgetSettingsScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 위젯 설정: 놓인 위젯 목록, 없으면 추가 방법 안내 (widget/CLAUDE.md "설정") */
@Composable
private fun WidgetSettingsScreen(
    state: WidgetSettingsState,
    onAction: (WidgetSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("widget_settings"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.settings_widget_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_notification_back),
                    onClick = { onAction(WidgetSettingsAction.BackClicked) },
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            if (state.widgets.isEmpty()) {
                SubCalInfoBox(text = stringResource(R.string.settings_widget_empty))
            } else {
                SubCalCard {
                    state.widgets.forEachIndexed { index, widget ->
                        if (index > 0) SubCalDivider()
                        SubCalListItem(
                            title = stringResource(widget.size.labelRes),
                            subtitle = stringResource(widget.background.labelRes),
                            onClick = { onAction(WidgetSettingsAction.WidgetClicked(widget.widgetId)) },
                            modifier = Modifier.testTag("widget_${widget.widgetId}"),
                        )
                    }
                }
            }
            if (state.canAddWidget) {
                SubCalSecondaryButton(
                    text = stringResource(R.string.settings_widget_add),
                    onClick = { onAction(WidgetSettingsAction.AddClicked) },
                    modifier = Modifier.fillMaxWidth().testTag("widget_add"),
                )
            }
        }
    }

    if (state.isSizeSheetOpen) {
        SubCalBottomSheet(onDismissRequest = { onAction(WidgetSettingsAction.SizeSheetDismissed) }) {
            SheetTitle(stringResource(R.string.settings_widget_size_title))
            Column(modifier = Modifier.padding(bottom = AppSpacing.xl)) {
                WidgetSize.entries.forEach { size ->
                    SubCalListItem(
                        title = stringResource(size.labelRes),
                        subtitle = stringResource(size.descriptionRes),
                        onClick = { onAction(WidgetSettingsAction.SizeSelected(size)) },
                        modifier = Modifier.testTag("widget_size_${size.name.lowercase()}"),
                    )
                }
            }
        }
    }
}

private val WidgetSize.labelRes: Int
    get() =
        when (this) {
            WidgetSize.SMALL -> R.string.settings_widget_small
            WidgetSize.MEDIUM -> R.string.settings_widget_medium
        }

private val WidgetSize.descriptionRes: Int
    get() =
        when (this) {
            WidgetSize.SMALL -> R.string.settings_widget_small_description
            WidgetSize.MEDIUM -> R.string.settings_widget_medium_description
        }

private val WidgetBackground.labelRes: Int
    get() =
        when (this) {
            WidgetBackground.THEME -> R.string.settings_widget_background_theme
            WidgetBackground.DARK -> R.string.settings_widget_background_dark
            WidgetBackground.WHITE -> R.string.settings_widget_background_white
        }

@ScreenPreviews
@Composable
private fun WidgetSettingsPreview() {
    AppTheme {
        WidgetSettingsScreen(
            state =
                WidgetSettingsState(
                    isLoading = false,
                    widgets =
                        listOf(
                            WidgetConfig(1, WidgetSize.SMALL),
                            WidgetConfig(2, WidgetSize.MEDIUM, WidgetBackground.DARK),
                        ),
                    canAddWidget = true,
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun WidgetSettingsEmptyPreview() {
    AppTheme {
        WidgetSettingsScreen(
            state = WidgetSettingsState(isLoading = false, canAddWidget = true),
            onAction = {},
        )
    }
}
