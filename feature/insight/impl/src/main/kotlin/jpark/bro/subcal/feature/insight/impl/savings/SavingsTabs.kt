package jpark.bro.subcal.feature.insight.impl.savings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBadge
import jpark.bro.subcal.core.designsystem.component.SubCalBadgeTone
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalOutlineButton
import jpark.bro.subcal.core.designsystem.component.SubCalSection
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.insight.impl.R

/** 해지 고민함 탭: 연간 합계 · 목록 · 중복 · 연간 결제 비교 */
@Composable
internal fun ConsiderTab(
    state: SavingsState,
    onAction: (SavingsAction) -> Unit,
) {
    SummaryCard(
        label = stringResource(R.string.insight_savings_consider_summary, state.considerItems.size),
        money = Money(state.considerAnnualKrw, Currency.KRW),
        text = { stringResource(R.string.insight_savings_consider_annual, it) },
        tone = MoneyTone.HIGHLIGHT,
    )
    SubCalSection(title = stringResource(R.string.insight_savings_consider_list)) {
        if (state.considerItems.isEmpty()) {
            SubCalListItem(title = stringResource(R.string.insight_savings_consider_empty))
        }
        state.considerItems.forEachIndexed { index, item ->
            if (index > 0) SubCalDivider()
            SubCalListItem(
                title = item.name,
                subtitle = considerReason(item),
                onClick = { onAction(SavingsAction.SubscriptionClicked(item.id)) },
                showChevron = false,
                leading = { SubCalServiceIcon(name = item.name, colorKey = item.colorKey) },
                trailing = {
                    SubCalOutlineButton(
                        text = stringResource(R.string.insight_savings_cancel_go),
                        onClick = { onAction(SavingsAction.CancelPageClicked(item.id)) },
                        size = SubCalButtonSize.SMALL,
                    )
                },
                modifier = Modifier.testTag("consider_${item.id}"),
            )
        }
    }
    state.duplicates.forEach { DuplicateCard(it) }
    state.annualOptions.forEach { AnnualOptionCard(it) }
}

/** 해지 고민 이유: 이번 달 사용 횟수(사용 체크) → 중복 → 월 환산 (product-spec 4-8) */
@Composable
private fun considerReason(item: ConsiderItem): String =
    when {
        item.useCount == 0 -> {
            stringResource(R.string.insight_savings_consider_unused)
        }

        item.useCount != null -> {
            stringResource(R.string.insight_savings_consider_usage, item.useCount)
        }

        item.duplicateWith.isNotEmpty() -> {
            stringResource(R.string.insight_savings_consider_duplicate, item.duplicateWith.joinToString(", "))
        }

        else -> {
            stringResource(R.string.insight_savings_consider_monthly, formatMoney(Money(item.monthlyKrw, Currency.KRW)))
        }
    }

/** 아낀 돈 탭: 지금까지 아낀 돈 + 해지한 구독별, 큰 순 */
@Composable
internal fun SavedTab(
    state: SavingsState,
    onAction: (SavingsAction) -> Unit,
) {
    SummaryCard(
        label = stringResource(R.string.insight_savings_saved_total_label),
        money = Money(state.savedTotalKrw, Currency.KRW),
        text = { stringResource(R.string.insight_savings_saved_total, it) },
        tone = MoneyTone.POSITIVE,
    )
    SubCalSection(title = stringResource(R.string.insight_savings_saved_list)) {
        if (state.savedItems.isEmpty()) {
            SubCalListItem(title = stringResource(R.string.insight_savings_saved_empty))
        }
        state.savedItems.forEachIndexed { index, item ->
            if (index > 0) SubCalDivider()
            SubCalListItem(
                title = item.name,
                subtitle =
                    stringResource(
                        R.string.insight_savings_saved_cancelled_on,
                        item.cancelledOn.year,
                        item.cancelledOn.monthValue,
                        item.cancelledOn.dayOfMonth,
                    ),
                onClick = { onAction(SavingsAction.SubscriptionClicked(item.id)) },
                showChevron = false,
                leading = { SubCalServiceIcon(name = item.name, colorKey = item.colorKey, dimmed = true) },
                trailing = { MoneyText(money = Money(item.savedKrw, Currency.KRW), tone = MoneyTone.POSITIVE) },
                modifier = Modifier.testTag("saved_${item.id}"),
            )
        }
    }
}

/** 화면 위 큰 금액 카드 */
@Composable
private fun SummaryCard(
    label: String,
    money: Money,
    text: @Composable (String) -> String,
    tone: MoneyTone,
) {
    SubCalCard {
        Column(
            modifier = Modifier.padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(text = label, style = AppThemeTokens.typography.labelMd, color = AppThemeTokens.colors.textSecondary)
            Text(
                text = text(formatMoney(money)),
                style = AppThemeTokens.typography.headlineLg.copy(fontFeatureSettings = "tnum"),
                color =
                    if (tone ==
                        MoneyTone.POSITIVE
                    ) {
                        AppThemeTokens.colors.success
                    } else {
                        AppThemeTokens.colors.primary
                    },
                modifier = Modifier.testTag("savings_summary"),
            )
        }
    }
}

/** 겹치는 구독 (product-spec 2-9) */
@Composable
private fun DuplicateCard(item: DuplicateItem) {
    Column(
        modifier =
            Modifier
                .border(width = 1.dp, color = AppThemeTokens.colors.warning, shape = AppShapes.md)
                .padding(AppSpacing.marginMobile)
                .testTag("duplicate_${item.category.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.insight_savings_duplicate_title),
                style = AppThemeTokens.typography.labelLg,
                color = AppThemeTokens.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            SubCalBadge(text = stringResource(R.string.insight_savings_duplicate_badge), tone = SubCalBadgeTone.SOON)
        }
        Text(
            text =
                stringResource(
                    R.string.insight_savings_duplicate_summary,
                    categoryLabel(item.category),
                    item.names.size,
                    formatMoney(Money(item.monthlyKrw, Currency.KRW)),
                ),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textPrimary,
        )
        Text(
            text = item.names.joinToString(" · "),
            style = AppThemeTokens.typography.bodySm,
            color = AppThemeTokens.colors.textSecondary,
        )
    }
}

/** 연간 결제 비교 (product-spec 2-9) */
@Composable
private fun AnnualOptionCard(item: AnnualOptionItem) {
    SubCalCard(modifier = Modifier.testTag("annual_${item.id}")) {
        Column(
            modifier = Modifier.padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.insight_savings_annual_title),
                style = AppThemeTokens.typography.labelMd,
                color = AppThemeTokens.colors.primary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = AppThemeTokens.typography.labelLg,
                        color = AppThemeTokens.colors.textPrimary,
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.insight_savings_annual_compare,
                                formatMoney(item.monthlyAmount),
                                formatMoney(item.annualAmount),
                            ),
                        style = AppThemeTokens.typography.bodySm,
                        color = AppThemeTokens.colors.textSecondary,
                    )
                }
                Text(
                    text =
                        stringResource(
                            R.string.insight_savings_annual_saving,
                            formatMoney(Money(item.yearlySavingKrw, Currency.KRW)),
                        ),
                    style = AppThemeTokens.typography.headlineSm.copy(fontFeatureSettings = "tnum"),
                    color = AppThemeTokens.colors.success,
                )
            }
        }
    }
}
