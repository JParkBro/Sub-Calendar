package jpark.bro.subcal.feature.insight.impl.yearreport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBarChart
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalChartBar
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.insight.impl.R
import jpark.bro.subcal.core.designsystem.R as DesignR

private val ChartHeight = 120.dp
private val LongestIconSize = 56.dp
private val BadgeIconSize = 16.dp

/** 리포트 카드 한 장: 결산 뱃지 · [page] 내용 · 앱 이름. 화면의 스와이프 카드와 공유 이미지가 같이 쓴다 */
@Composable
internal fun YearReportCard(
    year: Int,
    page: YearReportPage,
    content: YearReportContent,
    modifier: Modifier = Modifier,
) {
    SubCalCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            YearBadge(year)
            when (page) {
                YearReportPage.TOTAL -> TotalPage(year, content)
                YearReportPage.CATEGORY -> CategoryPage(content)
                YearReportPage.LONGEST -> LongestPage(content)
                YearReportPage.SAVED -> SavedPage(year, content)
            }
            SubCalDivider()
            AppNameFooter()
        }
    }
}

@Composable
private fun YearBadge(year: Int) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            Modifier
                .clip(AppShapes.full)
                .background(colors.primarySoft)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_auto_awesome),
            contentDescription = null,
            tint = colors.onPrimarySoft,
            modifier = Modifier.size(BadgeIconSize),
        )
        Text(
            text = stringResource(R.string.insight_year_report_badge, year),
            style = AppThemeTokens.typography.labelMd,
            color = colors.onPrimarySoft,
        )
    }
}

/** 올해 총 지출 · 달별 막대(가장 많이 쓴 달 강조) · 월 평균 */
@Composable
private fun ColumnScope.TotalPage(
    year: Int,
    content: YearReportContent,
) {
    val maxIndex = content.monthlyKrw.indices.maxByOrNull { content.monthlyKrw[it] } ?: -1
    PageTitle(stringResource(R.string.insight_year_report_total_title, year))
    MoneyText(
        money = Money(content.totalKrw, Currency.KRW),
        style = AppThemeTokens.typography.displayHeroMobile,
        tone = MoneyTone.HIGHLIGHT,
    )
    SubCalBarChart(
        bars =
            content.monthlyKrw.mapIndexed { index, krw ->
                SubCalChartBar(
                    value = krw.toFloat(),
                    label = stringResource(R.string.insight_year_report_month_label, index + 1),
                    highlighted = index == maxIndex && krw > 0,
                )
            },
        description = stringResource(R.string.insight_year_report_chart),
        height = ChartHeight,
    )
    Detail(
        stringResource(
            R.string.insight_year_report_monthly_average,
            formatMoney(Money(content.monthlyAverageKrw, Currency.KRW)),
        ),
    )
}

/** 카테고리 1위: 이름 · 금액 · 비율 · 구독 수 */
@Composable
private fun ColumnScope.CategoryPage(content: YearReportContent) {
    PageTitle(stringResource(R.string.insight_year_report_category_title))
    val top = content.topCategory
    if (top == null) {
        Headline(stringResource(R.string.insight_year_report_no_payment))
        return
    }
    Headline(categoryLabel(top.category))
    MoneyText(
        money = Money(top.totalKrw, Currency.KRW),
        style = AppThemeTokens.typography.headlineMd,
        tone = MoneyTone.HIGHLIGHT,
    )
    Detail(stringResource(R.string.insight_year_report_category_detail, top.percent, top.subscriptionCount))
}

/** 가장 오래 쓴 구독: 아이콘 · 이름 · n년째 · 첫 결제 달 */
@Composable
private fun ColumnScope.LongestPage(content: YearReportContent) {
    PageTitle(stringResource(R.string.insight_year_report_longest_title))
    val longest = content.longest
    if (longest == null) {
        Headline(stringResource(R.string.insight_year_report_no_payment))
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        SubCalServiceIcon(name = longest.name, colorKey = longest.colorKey, size = LongestIconSize)
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Headline(longest.name)
            Text(
                text = stringResource(R.string.insight_year_report_longest_years, longest.yearCount),
                style = AppThemeTokens.typography.labelLg,
                color = AppThemeTokens.colors.primary,
            )
        }
    }
    Detail(
        stringResource(R.string.insight_year_report_longest_since, longest.since.year, longest.since.monthValue),
    )
}

/** 올해 해지로 아낀 돈 */
@Composable
private fun ColumnScope.SavedPage(
    year: Int,
    content: YearReportContent,
) {
    PageTitle(stringResource(R.string.insight_year_report_saved_title, year))
    if (content.savedKrw == 0L) {
        Headline(stringResource(R.string.insight_year_report_saved_none))
        Detail(stringResource(R.string.insight_year_report_saved_none_hint))
        return
    }
    MoneyText(
        money = Money(content.savedKrw, Currency.KRW),
        style = AppThemeTokens.typography.displayHeroMobile,
        tone = MoneyTone.POSITIVE,
    )
    Detail(stringResource(R.string.insight_year_report_saved_detail, content.savedCount))
}

@Composable
private fun PageTitle(text: String) {
    Text(text = text, style = AppThemeTokens.typography.labelLg, color = AppThemeTokens.colors.textSecondary)
}

@Composable
private fun Headline(text: String) {
    Text(text = text, style = AppThemeTokens.typography.headlineMd, color = AppThemeTokens.colors.textPrimary)
}

@Composable
private fun Detail(text: String) {
    Text(text = text, style = AppThemeTokens.typography.bodyMd, color = AppThemeTokens.colors.textSecondary)
}

/** 공유 이미지에 앱 이름을 남긴다 (product-spec 4-11) */
@Composable
private fun AppNameFooter() {
    val context = LocalContext.current
    val appName = remember(context) { context.applicationInfo.loadLabel(context.packageManager).toString() }
    Text(
        text = appName,
        style = AppThemeTokens.typography.labelMd,
        color = AppThemeTokens.colors.textTertiary,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}
