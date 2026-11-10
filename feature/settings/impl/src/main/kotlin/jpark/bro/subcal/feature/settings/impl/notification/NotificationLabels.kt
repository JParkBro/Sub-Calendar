package jpark.bro.subcal.feature.settings.impl.notification

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.feature.settings.impl.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)

/** 알림 시각: 오전 9:00 */
internal fun formatReminderTime(time: LocalTime): String = time.format(TimeFormatter)

/** 알림 시점: 당일 · n일 전 */
@Composable
internal fun daysBeforeLabel(daysBefore: Int): String =
    if (daysBefore == 0) {
        stringResource(R.string.settings_notification_same_day)
    } else {
        stringResource(R.string.settings_notification_days_before, daysBefore)
    }

/** 구독별 선택지: 기본 · 끔 · 당일 · n일 전 */
@Composable
internal fun reminderChoiceLabel(choice: ReminderChoice): String =
    when (choice) {
        ReminderChoice.DEFAULT -> stringResource(R.string.settings_notification_default)

        ReminderChoice.OFF -> stringResource(R.string.settings_notification_off)

        ReminderChoice.SAME_DAY,
        ReminderChoice.ONE_DAY,
        ReminderChoice.THREE_DAYS,
        ReminderChoice.SEVEN_DAYS,
        -> daysBeforeLabel(requireNotNull(choice.daysBefore))
    }

/** 구독 행 오른쪽 값: 기본이면 실제 시점도 함께 "기본 (7일 전)" */
@Composable
internal fun SubscriptionReminderItem.valueLabel(): String =
    when {
        leadDays == null -> {
            stringResource(R.string.settings_notification_off)
        }

        choice == ReminderChoice.DEFAULT -> {
            stringResource(R.string.settings_notification_default_with_days, daysBeforeLabel(leadDays))
        }

        else -> {
            daysBeforeLabel(leadDays)
        }
    }
