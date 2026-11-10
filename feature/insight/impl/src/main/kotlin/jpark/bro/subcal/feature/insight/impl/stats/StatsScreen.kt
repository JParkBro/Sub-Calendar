package jpark.bro.subcal.feature.insight.impl.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBarChart
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalChartBar
import jpark.bro.subcal.core.designsystem.component.SubCalChartSlice
import jpark.bro.subcal.core.designsystem.component.SubCalDonutChart
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.component.chartOtherColor
import jpark.bro.subcal.core.designsystem.component.chartSeriesColor
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.domain.insight.CategoryShare
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.insight.api.SavingsKey
import jpark.bro.subcal.feature.insight.impl.R
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue

@Composable
internal fun StatsRoot(
    onNavigate: (NavKey) -> Unit,
    onSwitchTab: (NavKey) -> Unit,
    viewModel: StatsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is StatsEvent.OpenList -> onSwitchTab(SubscriptionListKey(event.category))
            is StatsEvent.OpenSavings -> onNavigate(SavingsKey(event.tab))
        }
    }
    StatsScreen(state = state, onAction = viewModel::onAction)
}

/** 통계 탭 (product-spec 4-6) */
@Composable
private fun StatsScreen(
    state: StatsState,
    onAction: (StatsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("stats"),
    ) {
        SubCalTopBar(title = stringResource(R.string.insight_stats_title))
        if (state.isLoading) return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = AppSpacing.marginMobile, end = AppSpacing.marginMobile, bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter),
        ) {
            PeriodLabel(state)
            CategorySection(state = state, onAction = onAction)
            TrendSection(state)
            AnnualCard(state.annualKrw)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.gutterCompact)) {
                SummaryCard(
                    title = stringResource(R.string.insight_stats_consider_title),
                    value = stringResource(R.string.insight_stats_consider_count, state.considerCancelCount),
                    caption =
                        stringResource(
                            R.string.insight_stats_consider_annual,
                            formatMoney(Money(state.considerCancelAnnualKrw, Currency.KRW)),
                        ),
                    valueColor =
                        if (state.considerCancelCount > 0) {
                            AppThemeTokens.colors.danger
                        } else {
                            AppThemeTokens.colors.textPrimary
                        },
                    onClick = { onAction(StatsAction.ConsiderCardClicked) },
                    modifier = Modifier.weight(1f).testTag("consider_card"),
                )
                SummaryCard(
                    title = stringResource(R.string.insight_stats_saved_title),
                    value = formatMoney(Money(state.savedKrw, Currency.KRW)),
                    caption = stringResource(R.string.insight_stats_saved_hint),
                    valueColor = AppThemeTokens.colors.success,
                    onClick = { onAction(StatsAction.SavedCardClicked) },
                    modifier = Modifier.weight(1f).testTag("saved_card"),
                )
            }
        }
    }
}

/** "10월 기준" + 월 시작 기준일이 1이 아니면 기간 */
@Composable
private fun PeriodLabel(state: StatsState) {
    val start = state.periodStart
    val end = state.periodEndInclusive
    val text =
        if (start != null && end != null) {
            stringResource(R.string.insight_stats_month, state.month.monthValue) + " · " +
                stringResource(
                    R.string.insight_stats_period_range,
                    start.monthValue,
                    start.dayOfMonth,
                    end.monthValue,
                    end.dayOfMonth,
                )
        } else {
            stringResource(R.string.insight_stats_month, state.month.monthValue)
        }
    Text(text = text, style = AppThemeTokens.typography.labelMd, color = AppThemeTokens.colors.textSecondary)
}

/** 카테고리별 비중: 도넛 + 범례(이름 · 비율). 항목을 누르면 목록 탭 */
@Composable
private fun CategorySection(
    state: StatsState,
    onAction: (StatsAction) -> Unit,
) {
    val colors = AppThemeTokens.colors
    val sliceColors = categoryColors(state.categories)
    SubCalCard {
        CardBody(spacing = AppSpacing.md) {
            SectionHeader(
                title = stringResource(R.string.insight_stats_category_title),
                trailing =
                    stringResource(
                        R.string.insight_stats_category_total,
                        formatMoney(Money(state.monthlyAverageKrw, Currency.KRW)),
                    ),
            )
            if (state.categories.isEmpty()) {
                Text(
                    text = stringResource(R.string.insight_stats_category_empty),
                    style = AppThemeTokens.typography.bodyMd,
                    color = colors.textSecondary,
                )
                return@CardBody
            }
            val description =
                state.categories
                    .map { share ->
                        categoryLabel(share.category) + " " +
                            stringResource(R.string.insight_stats_category_percent, share.percent)
                    }.joinToString()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                SubCalDonutChart(
                    slices =
                        state.categories.mapIndexed { i, share ->
                            SubCalChartSlice(share.monthlyKrw.toFloat(), sliceColors[i])
                        },
                    description = stringResource(R.string.insight_stats_category_description, description),
                    size = 120.dp,
                    thickness = 18.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.insight_stats_category_center_label),
                            style = AppThemeTokens.typography.labelSm,
                            color = colors.textSecondary,
                        )
                        Text(
                            text = stringResource(R.string.insight_stats_category_center_count, state.activeCount),
                            style = AppThemeTokens.typography.headlineSm,
                            color = colors.textPrimary,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    state.categories.forEachIndexed { i, share ->
                        LegendRow(
                            share = share,
                            color = sliceColors[i],
                            onClick = { onAction(StatsAction.CategoryClicked(share.category)) },
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.insight_stats_category_hint),
                style = AppThemeTokens.typography.bodySm,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun LegendRow(
    share: CategoryShare,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = AppSpacing.sm)
                .testTag("legend_${share.category.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(
            text = categoryLabel(share.category),
            style = AppThemeTokens.typography.labelLg,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.insight_stats_category_percent, share.percent),
            style = AppThemeTokens.typography.labelLg.copy(fontFeatureSettings = "tnum"),
            color = AppThemeTokens.colors.textPrimary,
        )
    }
}

/** 최근 12개월: 이번 달 금액 + 지난달 대비 + 막대 */
@Composable
private fun TrendSection(state: StatsState) {
    val thisMonth = formatMoney(Money(state.thisPeriodKrw, Currency.KRW))
    SubCalCard {
        CardBody(spacing = AppSpacing.sm) {
            SectionHeader(title = stringResource(R.string.insight_stats_trend_title), trailing = null)
            Text(
                text = stringResource(R.string.insight_stats_trend_month_spend, state.month.monthValue),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
            )
            MoneyText(
                money = Money(state.thisPeriodKrw, Currency.KRW),
                style = AppThemeTokens.typography.headlineLg,
                tone = MoneyTone.HIGHLIGHT,
                modifier = Modifier.testTag("trend_total"),
            )
            ChangeText(state.changeKrw)
            SubCalBarChart(
                bars =
                    state.periods.mapIndexed { i, period ->
                        SubCalChartBar(
                            value = period.totalKrw.toFloat(),
                            label =
                                stringResource(
                                    R.string.insight_stats_trend_month_label,
                                    period.period.month.monthValue,
                                ),
                            highlighted = i == state.periods.lastIndex,
                        )
                    },
                description = stringResource(R.string.insight_stats_trend_description, thisMonth),
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }
    }
}

/** 차트 색: 큰 순서대로 계열 색, 기타는 회색 (theme-guide 5장) */
@Composable
private fun categoryColors(categories: List<CategoryShare>): List<Color> {
    var seriesIndex = 0
    return categories.map { share ->
        if (share.category == Category.ETC) chartOtherColor() else chartSeriesColor(seriesIndex++)
    }
}

@ScreenPreviews
@Composable
private fun StatsPreview() {
    AppTheme { StatsScreen(state = previewStatsState(), onAction = {}) }
}
