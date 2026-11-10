package jpark.bro.subcal.feature.insight.impl.yearreport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.feature.insight.api.YearReportKey
import jpark.bro.subcal.feature.insight.impl.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import jpark.bro.subcal.core.designsystem.R as DesignR

private val DotSize = 8.dp
private val DotActiveWidth = 20.dp

@Composable
internal fun YearReportRoot(
    key: YearReportKey,
    onBack: () -> Unit,
    viewModel: YearReportViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val shareLayer = rememberGraphicsLayer()
    val shareFailed = stringResource(R.string.insight_year_report_share_failed)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            YearReportEvent.NavigateBack -> {
                onBack()
            }

            YearReportEvent.ShareImage -> {
                if (!context.shareYearReportImage(shareLayer, state.year)) toastHostState.showSnackbar(shareFailed)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // 보고 있는 카드를 공유 이미지 크기로 미리 그려 둔다 (화면에는 보이지 않음)
        state.content?.let { content ->
            ShareImageCapture(layer = shareLayer) {
                YearReportShareImage(year = state.year, page = state.page, content = content)
            }
        }
        YearReportScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 연말 리포트 (product-spec 4-11): 지난해 · 올해 칩, 카드 4장 스와이프, 이미지로 공유 */
@Composable
private fun YearReportScreen(
    state: YearReportState,
    onAction: (YearReportAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("year_report"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.insight_year_report_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.insight_year_report_back),
                    onClick = { onAction(YearReportAction.BackClicked) },
                )
            },
        )
        if (state.isLoading) return@Column
        YearChips(state = state, onAction = onAction)
        val content = state.content
        if (content == null) {
            EmptyCard(year = state.year)
        } else {
            ReportPager(state = state, content = content, onAction = onAction)
            ShareBar(onAction = onAction)
        }
    }
}

@Composable
private fun YearChips(
    state: YearReportState,
    onAction: (YearReportAction) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        state.years.forEach { year ->
            SubCalFilterChip(
                text = stringResource(R.string.insight_year_report_year_chip, year),
                selected = year == state.year,
                onClick = { onAction(YearReportAction.YearSelected(year)) },
                modifier = Modifier.testTag("year_chip_$year"),
            )
        }
    }
}

@Composable
private fun EmptyCard(year: Int) {
    SubCalCard(modifier = Modifier.padding(AppSpacing.marginMobile).testTag("year_report_empty")) {
        Text(
            text = stringResource(R.string.insight_year_report_empty, year),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

/** 카드 4장 스와이프 + 점 표시. 연도를 바꾸면 첫 카드로 돌아간다 */
@Composable
private fun ColumnScope.ReportPager(
    state: YearReportState,
    content: YearReportContent,
    onAction: (YearReportAction) -> Unit,
) {
    val pages = YearReportPage.entries
    key(state.year) {
        val pagerState = rememberPagerState(pageCount = { pages.size })
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { onAction(YearReportAction.PageChanged(pages[it])) }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = AppSpacing.marginMobile),
            pageSpacing = AppSpacing.md,
            verticalAlignment = Alignment.Top,
        ) { index ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                YearReportCard(
                    year = state.year,
                    page = pages[index],
                    content = content,
                    modifier = Modifier.testTag("year_report_page_$index"),
                )
                Text(
                    text = stringResource(R.string.insight_year_report_note),
                    style = AppThemeTokens.typography.bodySm,
                    color = AppThemeTokens.colors.textTertiary,
                )
            }
        }
        PageDots(current = pagerState.currentPage, count = pages.size)
    }
}

@Composable
private fun PageDots(
    current: Int,
    count: Int,
) {
    val description = stringResource(R.string.insight_year_report_page, current + 1, count)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.sm)
                .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs, Alignment.CenterHorizontally),
    ) {
        repeat(count) { index ->
            val active = index == current
            Box(
                modifier =
                    Modifier
                        .height(DotSize)
                        .width(if (active) DotActiveWidth else DotSize)
                        .clip(AppShapes.full)
                        .background(if (active) AppThemeTokens.colors.primary else AppThemeTokens.colors.fill),
            )
        }
    }
}

@Composable
private fun ShareBar(onAction: (YearReportAction) -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(AppThemeTokens.colors.surface)
                .padding(AppSpacing.marginMobile),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        SubCalPrimaryButton(
            text = stringResource(R.string.insight_year_report_share),
            icon = DesignR.drawable.ic_share,
            onClick = { onAction(YearReportAction.ShareClicked) },
            modifier = Modifier.fillMaxWidth().testTag("year_report_share"),
        )
        Text(
            text = stringResource(R.string.insight_year_report_share_hint),
            style = AppThemeTokens.typography.bodySm,
            color = AppThemeTokens.colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ScreenPreviews
@Composable
private fun YearReportPreview() {
    AppTheme { YearReportScreen(state = previewYearReportState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun YearReportEmptyPreview() {
    AppTheme {
        YearReportScreen(
            state = YearReportState(isLoading = false, year = 2025, years = listOf(2025, 2026)),
            onAction = {},
        )
    }
}

@ComponentPreviews
@Composable
private fun YearReportCardsPreview() {
    AppTheme {
        Column(
            modifier = Modifier.background(AppThemeTokens.colors.bg).padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            val content = previewYearReportContent()
            YearReportPage.entries.forEach { page -> YearReportCard(year = 2026, page = page, content = content) }
        }
    }
}
