package jpark.bro.subcal.feature.settings.impl.notification

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBox
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBoxTone
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalSection
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalSwitch
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.billingCycleLabel
import jpark.bro.subcal.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun NotificationSettingsRoot(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.settings_notification_save_failed)

    // 시스템 설정에서 알림을 켜고 돌아오면 바로 반영한다
    LifecycleResumeEffect(Unit) {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        viewModel.onAction(NotificationSettingsAction.DeviceNotificationsChecked(enabled))
        onPauseOrDispose {}
    }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            NotificationSettingsEvent.NavigateBack -> onBack()
            NotificationSettingsEvent.OpenSystemSettings -> context.openAppNotificationSettings()
            NotificationSettingsEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        NotificationSettingsScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

private fun Context.openAppNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
    )
}

/** 알림 설정 (product-spec 6장, 1차 범위) */
@Composable
private fun NotificationSettingsScreen(
    state: NotificationSettingsState,
    onAction: (NotificationSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("notification_settings"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.settings_notification_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_notification_back),
                    onClick = { onAction(NotificationSettingsAction.BackClicked) },
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
            if (!state.deviceNotificationsEnabled) {
                DeviceOffCard(onAction = onAction)
            }
            PaymentCard(state = state, onAction = onAction)
            if (state.settings.paymentEnabled) {
                DefaultDaysSection(selected = state.settings.defaultDaysBefore, onAction = onAction)
                SubscriptionSection(items = state.subscriptions, onAction = onAction)
            }
        }
    }

    if (state.isTimeSheetVisible) {
        TimeSheet(state = state, onAction = onAction)
    }
    state.editing?.let { SubscriptionReminderSheet(item = it, onAction = onAction) }
}

@Composable
private fun DeviceOffCard(onAction: (NotificationSettingsAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        SubCalInfoBox(
            text = stringResource(R.string.settings_notification_device_off),
            tone = SubCalInfoBoxTone.WARNING,
        )
        SubCalSecondaryButton(
            text = stringResource(R.string.settings_notification_open_system),
            onClick = { onAction(NotificationSettingsAction.OpenSystemSettingsClicked) },
            size = SubCalButtonSize.SMALL,
            modifier = Modifier.fillMaxWidth().testTag("open_system_settings"),
        )
    }
}

/** 결제 알림 스위치 + 알림 시각 */
@Composable
private fun PaymentCard(
    state: NotificationSettingsState,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    val settings = state.settings
    SubCalCard {
        SubCalListItem(
            title = stringResource(R.string.settings_notification_payment),
            subtitle =
                stringResource(
                    R.string.settings_notification_payment_summary,
                    daysBeforeLabel(settings.defaultDaysBefore),
                    formatReminderTime(settings.time),
                ),
            trailing = {
                SubCalSwitch(
                    checked = settings.paymentEnabled,
                    onCheckedChange = { onAction(NotificationSettingsAction.PaymentEnabledChanged(it)) },
                    modifier = Modifier.testTag("payment_switch"),
                )
            },
        )
        if (settings.paymentEnabled) {
            SubCalDivider()
            SubCalListItem(
                title = stringResource(R.string.settings_notification_time),
                subtitle = stringResource(R.string.settings_notification_time_hint),
                onClick = { onAction(NotificationSettingsAction.TimeClicked) },
                trailing = {
                    Text(
                        text = formatReminderTime(settings.time),
                        style = AppThemeTokens.typography.labelLg,
                        color = AppThemeTokens.colors.primary,
                    )
                },
                modifier = Modifier.testTag("time_row"),
            )
        }
    }
}

@Composable
private fun DefaultDaysSection(
    selected: Int,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        SectionLabel(stringResource(R.string.settings_notification_default_days))
        ChipRow {
            DaysBeforeChoices.forEach { days ->
                SubCalFilterChip(
                    text = daysBeforeLabel(days),
                    selected = days == selected,
                    onClick = { onAction(NotificationSettingsAction.DefaultDaysBeforeSelected(days)) },
                    modifier = Modifier.testTag("default_days_$days"),
                )
            }
        }
    }
}

@Composable
private fun SubscriptionSection(
    items: List<SubscriptionReminderItem>,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        SubCalSection(title = stringResource(R.string.settings_notification_per_subscription)) {
            if (items.isEmpty()) {
                SubCalListItem(title = stringResource(R.string.settings_notification_empty))
            }
            items.forEachIndexed { index, item ->
                if (index > 0) SubCalDivider()
                SubCalListItem(
                    title = item.name,
                    subtitle = billingCycleLabel(item.cycle, item.firstBillingDate),
                    onClick = { onAction(NotificationSettingsAction.SubscriptionClicked(item.id)) },
                    leading = { SubCalServiceIcon(name = item.name, colorKey = item.colorKey) },
                    trailing = {
                        Text(
                            text = item.valueLabel(),
                            style = AppThemeTokens.typography.labelMd,
                            color =
                                if (item.leadDays == null) {
                                    AppThemeTokens.colors.textTertiary
                                } else {
                                    AppThemeTokens.colors.textSecondary
                                },
                        )
                    },
                    modifier = Modifier.testTag("subscription_${item.id}"),
                )
            }
        }
        SubCalInfoBox(text = stringResource(R.string.settings_notification_yearly_hint))
    }
}

@ScreenPreviews
@Composable
private fun NotificationSettingsPreview() {
    AppTheme {
        NotificationSettingsScreen(
            state =
                NotificationSettingsState(
                    isLoading = false,
                    deviceNotificationsEnabled = false,
                    subscriptions =
                        listOf(
                            previewItem(
                                "netflix",
                                "넷플릭스",
                                "RED",
                                CycleUnit.MONTH,
                                "2026-01-14",
                                ReminderChoice.DEFAULT,
                                1,
                            ),
                            previewItem(
                                "google-one",
                                "구글 원",
                                "BLUE",
                                CycleUnit.YEAR,
                                "2025-11-20",
                                ReminderChoice.DEFAULT,
                                7,
                            ),
                            previewItem(
                                "coupang",
                                "쿠팡 와우",
                                "ORANGE",
                                CycleUnit.MONTH,
                                "2026-01-03",
                                ReminderChoice.OFF,
                                null,
                            ),
                            previewItem(
                                "youtube",
                                "유튜브 프리미엄",
                                "RED",
                                CycleUnit.MONTH,
                                "2026-01-07",
                                ReminderChoice.THREE_DAYS,
                                3,
                            ),
                        ),
                ),
            onAction = {},
        )
    }
}

@Suppress("LongParameterList") // 샘플 데이터
private fun previewItem(
    id: String,
    name: String,
    colorKey: String,
    unit: CycleUnit,
    first: String,
    choice: ReminderChoice,
    leadDays: Int?,
) = SubscriptionReminderItem(id, name, colorKey, BillingCycle(unit), LocalDate.parse(first), choice, leadDays)
