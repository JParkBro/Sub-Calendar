package jpark.bro.subcal.feature.insight.impl.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBadge
import jpark.bro.subcal.core.designsystem.component.SubCalBadgeTone
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.insight.impl.R
import kotlin.math.absoluteValue

/** 통계 화면 카드 조각 */
@Composable
internal fun AnnualCard(annualKrw: Long) {
    SubCalCard {
        CardBody(spacing = AppSpacing.xs) {
            Text(
                text = stringResource(R.string.insight_stats_annual_title),
                style = AppThemeTokens.typography.labelLg,
                color = AppThemeTokens.colors.textSecondary,
            )
            MoneyText(
                money = Money(annualKrw, Currency.KRW),
                style = AppThemeTokens.typography.headlineLg,
                modifier = Modifier.testTag("annual_total"),
            )
        }
    }
}

@Composable
internal fun SummaryCard(
    title: String,
    value: String,
    caption: String,
    valueColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SubCalCard(modifier = modifier, onClick = onClick) {
        CardBody(spacing = AppSpacing.xs) {
            Text(text = title, style = AppThemeTokens.typography.labelMd, color = AppThemeTokens.colors.textSecondary)
            Text(
                text = value,
                style = AppThemeTokens.typography.headlineSm.copy(fontFeatureSettings = "tnum"),
                color = valueColor,
            )
            Text(text = caption, style = AppThemeTokens.typography.bodySm, color = AppThemeTokens.colors.textTertiary)
        }
    }
}

/** 가성비 분석 입구: 사용 안 함이 있으면 danger 뱃지, 사용 체크가 없으면 켜는 방법 */
@Composable
internal fun ValueCard(
    state: StatsState,
    onClick: () -> Unit,
) {
    SubCalCard {
        SubCalListItem(
            title = stringResource(R.string.insight_stats_value_title),
            subtitle =
                if (state.valueTrackedCount == 0) {
                    stringResource(R.string.insight_stats_value_off)
                } else {
                    stringResource(R.string.insight_stats_value_count, state.valueTrackedCount)
                },
            onClick = onClick,
            trailing = {
                if (state.valueUnusedCount > 0) {
                    SubCalBadge(
                        text = stringResource(R.string.insight_stats_value_unused, state.valueUnusedCount),
                        tone = SubCalBadgeTone.SOON,
                    )
                }
            },
            modifier = Modifier.testTag("value_card"),
        )
    }
}

@Composable
internal fun SectionHeader(
    title: String,
    trailing: String?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = AppThemeTokens.typography.labelMd,
                color = AppThemeTokens.colors.textSecondary,
            )
        }
    }
}

/** 카드 안 여백 + 세로 간격 */
@Composable
internal fun CardBody(
    spacing: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.padding(AppSpacing.marginMobile),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/** 증가 danger "+", 감소 success "−" (theme-guide 6장 2번) */
@Composable
internal fun ChangeText(changeKrw: Long) {
    val colors = AppThemeTokens.colors
    val amount = formatMoney(Money(changeKrw.absoluteValue, Currency.KRW))
    val (text, color) =
        when {
            changeKrw > 0 -> stringResource(R.string.insight_stats_change_up, amount) to colors.danger
            changeKrw < 0 -> stringResource(R.string.insight_stats_change_down, amount) to colors.success
            else -> stringResource(R.string.insight_stats_change_same) to colors.textSecondary
        }
    Text(text = text, style = AppThemeTokens.typography.labelMd.copy(fontFeatureSettings = "tnum"), color = color)
}
