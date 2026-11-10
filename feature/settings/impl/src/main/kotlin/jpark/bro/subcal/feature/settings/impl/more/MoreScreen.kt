package jpark.bro.subcal.feature.settings.impl.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.feature.settings.api.NotificationSettingsKey
import jpark.bro.subcal.feature.settings.impl.R
import jpark.bro.subcal.feature.settings.impl.notification.daysBeforeLabel
import jpark.bro.subcal.feature.settings.impl.notification.formatReminderTime
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun MoreRoot(
    onNavigate: (NavKey) -> Unit,
    viewModel: MoreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            MoreEvent.OpenNotificationSettings -> onNavigate(NotificationSettingsKey)
        }
    }
    MoreScreen(state = state, onAction = viewModel::onAction)
}

/** 더보기 탭 (product-spec 6장) */
@Composable
private fun MoreScreen(
    state: MoreState,
    onAction: (MoreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("more"),
    ) {
        SubCalTopBar(title = stringResource(R.string.settings_more_title))
        if (state.isLoading) return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = AppSpacing.marginMobile, end = AppSpacing.marginMobile, bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            SubCalCard {
                SubCalListItem(
                    title = stringResource(R.string.settings_more_notification),
                    subtitle = notificationSummary(state.notification),
                    onClick = { onAction(MoreAction.NotificationSettingsClicked) },
                    leading = {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_notifications),
                            contentDescription = null,
                            tint = AppThemeTokens.colors.primary,
                        )
                    },
                    modifier = Modifier.testTag("more_notification"),
                )
            }
        }
    }
}

@Composable
private fun notificationSummary(settings: NotificationSettings): String =
    if (settings.paymentEnabled) {
        stringResource(
            R.string.settings_more_notification_summary,
            daysBeforeLabel(settings.defaultDaysBefore),
            formatReminderTime(settings.time),
        )
    } else {
        stringResource(R.string.settings_more_notification_off)
    }

@ScreenPreviews
@Composable
private fun MorePreview() {
    AppTheme { MoreScreen(state = MoreState(isLoading = false), onAction = {}) }
}
