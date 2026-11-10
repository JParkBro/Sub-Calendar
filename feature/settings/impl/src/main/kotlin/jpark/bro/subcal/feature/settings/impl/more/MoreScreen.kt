package jpark.bro.subcal.feature.settings.impl.more

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalSection
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.formatMoney
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
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.settings_more_save_failed)

    LaunchedEffect(Unit) { viewModel.onAction(MoreAction.AppVersionLoaded(context.versionName())) }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            MoreEvent.OpenNotificationSettings -> onNavigate(NotificationSettingsKey)
            MoreEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        MoreScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

private fun Context.versionName(): String {
    val info =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
    return info.versionName.orEmpty()
}

/** 더보기 탭 (product-spec 6장): 개인용 단계의 로컬 메뉴 */
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
            SettingsSection(state = state, onAction = onAction)
            SubCalSection(title = stringResource(R.string.settings_more_section_info)) {
                SubCalListItem(
                    title = stringResource(R.string.settings_more_app_version),
                    trailing = { ValueText(state.appVersion) },
                    modifier = Modifier.testTag("more_app_version"),
                )
            }
        }
    }

    state.sheet?.let { MoreSheetContent(state = state, sheet = it, onAction = onAction) }
}

/** 월 시작 기준일 · 달러 환율 · 기본 결제 수단 · 화면 모드 */
@Composable
private fun SettingsSection(
    state: MoreState,
    onAction: (MoreAction) -> Unit,
) {
    val settings = state.appSettings
    SubCalSection(title = stringResource(R.string.settings_more_section_settings)) {
        SubCalListItem(
            title = stringResource(R.string.settings_more_month_start_day),
            onClick = { onAction(MoreAction.MonthStartDayClicked) },
            trailing = { ValueText(stringResource(R.string.settings_more_day, settings.monthStartDay)) },
            modifier = Modifier.testTag("more_month_start_day"),
        )
        SubCalDivider()
        SubCalListItem(
            title = stringResource(R.string.settings_more_usd_krw_rate),
            onClick = { onAction(MoreAction.UsdKrwRateClicked) },
            trailing = {
                ValueText(
                    stringResource(
                        R.string.settings_more_usd_krw_rate_value,
                        formatMoney(Money(settings.usdKrwRate, Currency.KRW)),
                    ),
                )
            },
            modifier = Modifier.testTag("more_usd_krw_rate"),
        )
        SubCalDivider()
        SubCalListItem(
            title = stringResource(R.string.settings_more_payment_method),
            onClick = { onAction(MoreAction.PaymentMethodClicked) },
            trailing = {
                ValueText(settings.defaultPaymentMethod ?: stringResource(R.string.settings_more_payment_method_none))
            },
            modifier = Modifier.testTag("more_payment_method"),
        )
        SubCalDivider()
        SubCalListItem(
            title = stringResource(R.string.settings_more_theme_mode),
            onClick = { onAction(MoreAction.ThemeModeClicked) },
            trailing = { ValueText(themeModeLabel(settings.themeMode)) },
            modifier = Modifier.testTag("more_theme_mode"),
        )
    }
}

@Composable
private fun ValueText(text: String) {
    Text(text = text, style = AppThemeTokens.typography.bodyMd, color = AppThemeTokens.colors.textSecondary)
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
    AppTheme { MoreScreen(state = MoreState(isLoading = false, appVersion = "1.0.0"), onAction = {}) }
}
