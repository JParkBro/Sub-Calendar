package jpark.bro.subcal.feature.subscription.impl.edit

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppFixedColors
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDangerButton
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalSwitch
import jpark.bro.subcal.core.designsystem.component.SubCalTextField
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.subscription.impl.R
import java.time.LocalDate

/** 라벨 + 입력 한 덩어리 */
@Composable
internal fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(text = label, style = AppThemeTokens.typography.labelMd, color = AppThemeTokens.colors.textSecondary)
        content()
        caption?.let {
            Text(text = it, style = AppThemeTokens.typography.bodySm, color = AppThemeTokens.colors.textTertiary)
        }
    }
}

/** 칩 여러 줄 (한 줄이 넘치면 내려간다) */
@Composable
private fun ChipGroup(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FormField(label = label, modifier = modifier) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) { content() }
    }
}

@Composable
internal fun PlanChips(
    state: SubscriptionEditState,
    onAction: (SubscriptionEditAction) -> Unit,
) {
    FormField(
        label = stringResource(R.string.subscription_edit_plan),
        caption = state.pricesAsOf?.let { stringResource(R.string.subscription_edit_prices_as_of, it) },
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            state.plans.forEachIndexed { index, plan ->
                SubCalFilterChip(
                    text = stringResource(R.string.subscription_edit_plan_chip, plan.name, formatMoney(plan.amount)),
                    selected = index == state.selectedPlanIndex,
                    onClick = { onAction(SubscriptionEditAction.PlanSelected(index)) },
                    modifier = Modifier.testTag("plan_$index"),
                )
            }
        }
    }
}

@Composable
internal fun CycleSection(
    state: SubscriptionEditState,
    onAction: (SubscriptionEditAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        ChipGroup(label = stringResource(R.string.subscription_edit_cycle)) {
            CycleOption.entries.forEach { option ->
                SubCalFilterChip(
                    text = stringResource(option.labelRes),
                    selected = option == state.cycleOption,
                    onClick = { onAction(SubscriptionEditAction.CycleOptionSelected(option)) },
                    modifier = Modifier.testTag("cycle_${option.name.lowercase()}"),
                )
            }
        }
        if (state.cycleOption == CycleOption.CUSTOM) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                SubCalTextField(
                    value = state.customIntervalInput,
                    onValueChange = { onAction(SubscriptionEditAction.CustomIntervalChanged(it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(72.dp).testTag("custom_interval"),
                )
                CycleUnit.entries.forEach { unit ->
                    SubCalFilterChip(
                        text = stringResource(unit.labelRes),
                        selected = unit == state.customUnit,
                        onClick = { onAction(SubscriptionEditAction.CustomUnitSelected(unit)) },
                    )
                }
            }
            Text(
                text = stringResource(R.string.subscription_edit_custom_interval_hint),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textTertiary,
            )
        }
    }
}

/** 첫 결제일 · 무료체험 (체험이면 첫 결제일 라벨이 "체험 종료일") */
@Composable
internal fun DateSection(
    state: SubscriptionEditState,
    onAction: (SubscriptionEditAction) -> Unit,
) {
    val isTrial = state.trialStartedOn != null
    SubCalCard {
        SubCalListItem(
            title =
                stringResource(
                    if (isTrial) R.string.subscription_edit_trial_end else R.string.subscription_edit_first_billing,
                ),
            subtitle =
                state.pastPaymentCount
                    .takeIf { it > 0 }
                    ?.let { stringResource(R.string.subscription_edit_past_payments, it) },
            onClick = { onAction(SubscriptionEditAction.DateClicked(DateTarget.FIRST_BILLING)) },
            showChevron = false,
            trailing = { DateText(state.firstBillingDate) },
            modifier = Modifier.testTag("first_billing_date"),
        )
        SubCalDivider(inset = AppSpacing.marginMobile)
        SubCalListItem(
            title = stringResource(R.string.subscription_edit_trial),
            subtitle = stringResource(R.string.subscription_edit_trial_hint),
            showChevron = false,
            trailing = {
                SubCalSwitch(
                    checked = isTrial,
                    onCheckedChange = { onAction(SubscriptionEditAction.TrialToggled(it)) },
                    modifier = Modifier.testTag("trial_switch"),
                )
            },
        )
        state.trialStartedOn?.let { start ->
            SubCalDivider(inset = AppSpacing.marginMobile)
            SubCalListItem(
                title = stringResource(R.string.subscription_edit_trial_start),
                subtitle =
                    if (start.isBefore(state.firstBillingDate)) {
                        null
                    } else {
                        stringResource(R.string.subscription_edit_trial_invalid)
                    },
                onClick = { onAction(SubscriptionEditAction.DateClicked(DateTarget.TRIAL_START)) },
                showChevron = false,
                trailing = { DateText(start) },
            )
        }
    }
}

@Composable
private fun DateText(date: LocalDate) {
    Text(
        text = stringResource(R.string.subscription_edit_date, date.year, date.monthValue, date.dayOfMonth),
        style = AppThemeTokens.typography.labelLg.copy(fontFeatureSettings = "tnum"),
        color = AppThemeTokens.colors.textPrimary,
    )
}

@Composable
internal fun PaymentMethodSection(
    state: SubscriptionEditState,
    onAction: (SubscriptionEditAction) -> Unit,
) {
    FormField(label = stringResource(R.string.subscription_edit_payment_method)) {
        SubCalTextField(
            value = state.paymentMethod,
            onValueChange = { onAction(SubscriptionEditAction.PaymentMethodChanged(it)) },
            placeholder = stringResource(R.string.subscription_edit_payment_method_placeholder),
            modifier = Modifier.fillMaxWidth().testTag("payment_method_field"),
        )
        if (state.paymentMethodSuggestions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                state.paymentMethodSuggestions.forEach { method ->
                    SubCalFilterChip(
                        text = method,
                        selected = false,
                        onClick = { onAction(SubscriptionEditAction.PaymentMethodChanged(method)) },
                        role = Role.Button,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CategoryChips(
    selected: Category,
    onAction: (SubscriptionEditAction) -> Unit,
) {
    ChipGroup(label = stringResource(R.string.subscription_edit_category)) {
        Category.entries.forEach { category ->
            SubCalFilterChip(
                text = categoryLabel(category),
                selected = category == selected,
                onClick = { onAction(SubscriptionEditAction.CategorySelected(category)) },
            )
        }
    }
}

@Composable
internal fun ReminderChips(
    selected: ReminderOption,
    onAction: (SubscriptionEditAction) -> Unit,
) {
    ChipGroup(label = stringResource(R.string.subscription_edit_reminder)) {
        ReminderOption.entries.forEach { option ->
            SubCalFilterChip(
                text = stringResource(option.labelRes),
                selected = option == selected,
                onClick = { onAction(SubscriptionEditAction.ReminderSelected(option)) },
            )
        }
    }
}

/** 아이콘 색: 지금 이름의 이니셜로 서비스 색 10종을 보여준다 (theme-guide 5장) */
@Composable
internal fun ColorSwatches(
    name: String,
    selected: String,
    onAction: (SubscriptionEditAction) -> Unit,
) {
    ChipGroup(label = stringResource(R.string.subscription_edit_color)) {
        AppFixedColors.serviceColorKeys.forEachIndexed { index, colorKey ->
            val isSelected = colorKey == selected
            val description = stringResource(R.string.subscription_edit_color_item, index + 1)
            Box(
                modifier =
                    Modifier
                        .clip(AppShapes.full)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) AppThemeTokens.colors.primary else AppThemeTokens.colors.bg,
                            shape = AppShapes.full,
                        ).selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onAction(SubscriptionEditAction.ColorSelected(colorKey)) },
                        ).padding(3.dp)
                        .semantics { contentDescription = description }
                        .testTag("color_$colorKey"),
            ) {
                SubCalServiceIcon(name = name, colorKey = colorKey, size = 36.dp)
            }
        }
    }
}

@get:StringRes
private val CycleOption.labelRes: Int
    get() =
        when (this) {
            CycleOption.MONTHLY -> R.string.subscription_edit_cycle_monthly
            CycleOption.YEARLY -> R.string.subscription_edit_cycle_yearly
            CycleOption.WEEKLY -> R.string.subscription_edit_cycle_weekly
            CycleOption.CUSTOM -> R.string.subscription_edit_cycle_custom
        }

@get:StringRes
private val CycleUnit.labelRes: Int
    get() =
        when (this) {
            CycleUnit.DAY -> R.string.subscription_edit_unit_day
            CycleUnit.WEEK -> R.string.subscription_edit_unit_week
            CycleUnit.MONTH -> R.string.subscription_edit_unit_month
            CycleUnit.YEAR -> R.string.subscription_edit_unit_year
        }

@get:StringRes
private val ReminderOption.labelRes: Int
    get() =
        when (this) {
            ReminderOption.DEFAULT -> R.string.subscription_edit_reminder_default
            ReminderOption.OFF -> R.string.subscription_edit_reminder_off
            ReminderOption.SAME_DAY -> R.string.subscription_edit_reminder_same_day
            ReminderOption.ONE_DAY -> R.string.subscription_edit_reminder_one_day
            ReminderOption.THREE_DAYS -> R.string.subscription_edit_reminder_three_days
            ReminderOption.SEVEN_DAYS -> R.string.subscription_edit_reminder_seven_days
        }
