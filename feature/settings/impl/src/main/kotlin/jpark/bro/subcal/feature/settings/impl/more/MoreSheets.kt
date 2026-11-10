package jpark.bro.subcal.feature.settings.impl.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalDangerButton
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBox
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBoxTone
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalTextField
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.core.model.BackupSummary
import jpark.bro.subcal.feature.settings.impl.R
import jpark.bro.subcal.feature.settings.impl.ui.ChipRow
import jpark.bro.subcal.feature.settings.impl.ui.SheetTitle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 더보기 설정 값을 고치는 바텀시트 (product-spec 6장) */
@Composable
internal fun MoreSheetContent(
    state: MoreState,
    sheet: MoreSheet,
    onAction: (MoreAction) -> Unit,
) {
    SubCalBottomSheet(onDismissRequest = { onAction(MoreAction.SheetDismissed) }) {
        when (sheet) {
            MoreSheet.MONTH_START_DAY -> {
                MonthStartDaySheet(
                    selected = state.appSettings.monthStartDay,
                    onAction = onAction,
                )
            }

            MoreSheet.USD_KRW_RATE -> {
                UsdKrwRateSheet(state = state, onAction = onAction)
            }

            MoreSheet.PAYMENT_METHOD -> {
                PaymentMethodSheet(input = state.paymentMethodInput, onAction = onAction)
            }

            MoreSheet.THEME_MODE -> {
                ThemeModeSheet(selected = state.appSettings.themeMode, onAction = onAction)
            }

            MoreSheet.RESTORE_CONFIRM -> {
                state.restorePreview?.let { RestoreConfirmSheet(summary = it, onAction = onAction) }
            }

            MoreSheet.DELETE_ALL_CONFIRM -> {
                DeleteAllSheet(input = state.deleteAllInput, onAction = onAction)
            }
        }
    }
}

@Composable
private fun MonthStartDaySheet(
    selected: Int,
    onAction: (MoreAction) -> Unit,
) {
    SheetBody(
        title = stringResource(R.string.settings_more_month_start_day),
        hint = stringResource(R.string.settings_more_month_start_day_hint),
    ) {
        ChipRow {
            AppSettings.MonthStartDays.forEach { day ->
                SubCalFilterChip(
                    text = stringResource(R.string.settings_more_day, day),
                    selected = day == selected,
                    onClick = { onAction(MoreAction.MonthStartDaySelected(day)) },
                    modifier = Modifier.testTag("month_start_$day"),
                )
            }
        }
    }
}

@Composable
private fun UsdKrwRateSheet(
    state: MoreState,
    onAction: (MoreAction) -> Unit,
) {
    SheetBody(
        title = stringResource(R.string.settings_more_usd_krw_rate),
        hint = stringResource(R.string.settings_more_usd_krw_rate_hint),
    ) {
        SubCalTextField(
            value = state.usdKrwRateInput,
            onValueChange = { onAction(MoreAction.UsdKrwRateInputChanged(it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().testTag("usd_rate_field"),
        )
        SubCalPrimaryButton(
            text = stringResource(R.string.settings_more_save),
            onClick = { onAction(MoreAction.UsdKrwRateSaveClicked) },
            enabled = state.canSaveUsdKrwRate,
            modifier = Modifier.fillMaxWidth().testTag("usd_rate_save"),
        )
    }
}

@Composable
private fun PaymentMethodSheet(
    input: String,
    onAction: (MoreAction) -> Unit,
) {
    SheetBody(
        title = stringResource(R.string.settings_more_payment_method),
        hint = stringResource(R.string.settings_more_payment_method_hint),
    ) {
        SubCalTextField(
            value = input,
            onValueChange = { onAction(MoreAction.PaymentMethodInputChanged(it)) },
            placeholder = stringResource(R.string.settings_more_payment_method_placeholder),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().testTag("payment_method_field"),
        )
        SubCalPrimaryButton(
            text = stringResource(R.string.settings_more_save),
            onClick = { onAction(MoreAction.PaymentMethodSaveClicked) },
            modifier = Modifier.fillMaxWidth().testTag("payment_method_save"),
        )
    }
}

@Composable
private fun ThemeModeSheet(
    selected: AppThemeMode,
    onAction: (MoreAction) -> Unit,
) {
    SheetBody(title = stringResource(R.string.settings_more_theme_mode), hint = null) {
        ChipRow {
            AppThemeMode.entries.forEach { mode ->
                SubCalFilterChip(
                    text = themeModeLabel(mode),
                    selected = mode == selected,
                    onClick = { onAction(MoreAction.ThemeModeSelected(mode)) },
                    modifier = Modifier.testTag("theme_${mode.name.lowercase()}"),
                )
            }
        }
    }
}

@Composable
private fun RestoreConfirmSheet(
    summary: BackupSummary,
    onAction: (MoreAction) -> Unit,
) {
    val exportedAt =
        Instant
            .ofEpochMilli(summary.exportedAt)
            .atZone(ZoneId.systemDefault())
            .format(ExportedAtFormatter)
    SheetBody(
        title = stringResource(R.string.settings_more_restore_title),
        hint = stringResource(R.string.settings_more_restore_summary, exportedAt, summary.subscriptionCount),
    ) {
        SubCalInfoBox(text = stringResource(R.string.settings_more_restore_warning), tone = SubCalInfoBoxTone.WARNING)
        SubCalDangerButton(
            text = stringResource(R.string.settings_more_restore_confirm),
            onClick = { onAction(MoreAction.RestoreConfirmed) },
            filled = true,
            modifier = Modifier.fillMaxWidth().testTag("restore_confirm"),
        )
        SubCalSecondaryButton(
            text = stringResource(R.string.settings_more_cancel),
            onClick = { onAction(MoreAction.SheetDismissed) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** 되돌릴 수 없으므로 확인 문구("삭제")를 직접 입력해야 지운다 (product-spec 6장) */
@Composable
private fun DeleteAllSheet(
    input: String,
    onAction: (MoreAction) -> Unit,
) {
    val keyword = stringResource(R.string.settings_more_delete_all_keyword)
    SheetBody(
        title = stringResource(R.string.settings_more_delete_all_title),
        hint = stringResource(R.string.settings_more_delete_all_input_hint, keyword),
    ) {
        SubCalInfoBox(
            text = stringResource(R.string.settings_more_delete_all_warning),
            tone = SubCalInfoBoxTone.WARNING,
        )
        SubCalTextField(
            value = input,
            onValueChange = { onAction(MoreAction.DeleteAllInputChanged(it)) },
            placeholder = keyword,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().testTag("delete_all_field"),
        )
        SubCalDangerButton(
            text = stringResource(R.string.settings_more_delete_all_confirm),
            onClick = { onAction(MoreAction.DeleteAllConfirmed) },
            enabled = input.trim() == keyword,
            filled = true,
            modifier = Modifier.fillMaxWidth().testTag("delete_all_confirm"),
        )
    }
}

private val ExportedAtFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

@Composable
private fun SheetBody(
    title: String,
    hint: String?,
    content: @Composable () -> Unit,
) {
    SheetTitle(title)
    Column(
        modifier =
            Modifier.padding(
                start = AppSpacing.marginMobile,
                end = AppSpacing.marginMobile,
                bottom = AppSpacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        if (hint != null) {
            Text(text = hint, style = AppThemeTokens.typography.bodySm, color = AppThemeTokens.colors.textSecondary)
        }
        content()
    }
}

@Composable
internal fun themeModeLabel(mode: AppThemeMode): String =
    stringResource(
        when (mode) {
            AppThemeMode.LIGHT -> R.string.settings_more_theme_light
            AppThemeMode.DARK -> R.string.settings_more_theme_dark
            AppThemeMode.SYSTEM -> R.string.settings_more_theme_system
        },
    )
