package jpark.bro.subcal.feature.settings.impl.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.feature.settings.impl.R
import jpark.bro.subcal.feature.settings.impl.ui.ChipRow
import jpark.bro.subcal.feature.settings.impl.ui.SheetTitle

/** 알림 시각 · 구독별 알림 시점 바텀시트 */
@Composable
internal fun TimeSheet(
    state: NotificationSettingsState,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    SubCalBottomSheet(onDismissRequest = { onAction(NotificationSettingsAction.TimeSheetDismissed) }) {
        SheetTitle(stringResource(R.string.settings_notification_time))
        ChipRow(
            modifier =
                Modifier.padding(
                    start = AppSpacing.marginMobile,
                    end = AppSpacing.marginMobile,
                    bottom = AppSpacing.xl,
                ),
        ) {
            SelectableTimes.forEach { time ->
                SubCalFilterChip(
                    text = formatReminderTime(time),
                    selected = time == state.settings.time,
                    onClick = { onAction(NotificationSettingsAction.TimeSelected(time)) },
                    modifier = Modifier.testTag("time_${time.hour}"),
                )
            }
        }
    }
}

@Composable
internal fun SubscriptionReminderSheet(
    item: SubscriptionReminderItem,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    SubCalBottomSheet(onDismissRequest = { onAction(NotificationSettingsAction.SubscriptionSheetDismissed) }) {
        SheetTitle(stringResource(R.string.settings_notification_sheet_title, item.name))
        ChipRow(
            modifier =
                Modifier.padding(
                    start = AppSpacing.marginMobile,
                    end = AppSpacing.marginMobile,
                    bottom = AppSpacing.xl,
                ),
        ) {
            ReminderChoice.entries.forEach { choice ->
                SubCalFilterChip(
                    text = reminderChoiceLabel(choice),
                    selected = choice == item.choice,
                    onClick = { onAction(NotificationSettingsAction.SubscriptionReminderSelected(choice)) },
                    modifier = Modifier.testTag("reminder_${choice.name.lowercase()}"),
                )
            }
        }
    }
}
