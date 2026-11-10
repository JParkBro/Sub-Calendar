package jpark.bro.subcal.feature.home.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalFab
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.PaymentRow
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatKrwInManwon
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.calendar.api.CalendarKey
import jpark.bro.subcal.feature.insight.api.YearReportKey
import jpark.bro.subcal.feature.subscription.api.ServicePickerKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionEditKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import jpark.bro.subcal.feature.trial.api.TrialKey
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue
import jpark.bro.subcal.core.designsystem.R as DesignR

/** 마지막 카드가 FAB에 가리지 않게 (FAB 56dp + 여백) */
private val FabClearance = 88.dp

/** [onSwitchTab]은 탭 키로 그 탭을 연다 (카테고리 필터 · 캘린더) */
@Composable
internal fun HomeRoot(
    onNavigate: (NavKey) -> Unit,
    onSwitchTab: (NavKey) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val actionFailed = stringResource(R.string.home_action_failed)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is HomeEvent.OpenDetail -> onNavigate(SubscriptionDetailKey(event.subscriptionId))
            HomeEvent.OpenCalendar -> onSwitchTab(CalendarKey)
            is HomeEvent.OpenList -> onSwitchTab(SubscriptionListKey(event.category))
            HomeEvent.OpenServicePicker -> onNavigate(ServicePickerKey)
            is HomeEvent.OpenEdit -> onNavigate(SubscriptionEditKey(presetId = event.presetId))
            HomeEvent.OpenTrial -> onNavigate(TrialKey)
            is HomeEvent.OpenYearReport -> onNavigate(YearReportKey(event.year))
            HomeEvent.ActionFailed -> toastHostState.showSnackbar(actionFailed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 홈 (product-spec 4-1) */
@Composable
private fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("home"),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SubCalTopBar(title = stringResource(R.string.home_title))
            if (!state.isLoading) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = AppSpacing.marginMobile,
                                end = AppSpacing.marginMobile,
                                bottom = FabClearance,
                            ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
                ) {
                    if (state.showBillingDateHint) BillingDateHintCard(onAction = onAction)
                    MonthCard(state.summary)
                    if (state.trialsEndingSoon >
                        0
                    ) {
                        TrialWarningCard(count = state.trialsEndingSoon, onAction = onAction)
                    }
                    state.unresolvedTrials.forEach {
                        UnresolvedTrialCard(trial = it, isWorking = state.isWorking, onAction = onAction)
                    }
                    state.yearReportBanner?.let { YearReportBannerCard(banner = it, onAction = onAction) }
                    if (state.isEmpty) {
                        EmptyCard(onAction = onAction)
                    } else {
                        UpcomingSection(upcoming = state.upcoming, onAction = onAction)
                        CategoryChips(categories = state.categories, onAction = onAction)
                    }
                }
            }
        }
        SubCalFab(
            icon = DesignR.drawable.ic_add,
            contentDescription = stringResource(R.string.home_add),
            onClick = { onAction(HomeAction.AddClicked) },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppSpacing.marginMobile)
                    .testTag("add_fab"),
        )
    }
}

/** "{M}월 구독료" · 금액 · 지난달 대비 · 월 평균 · 연간 */
@Composable
private fun MonthCard(summary: MonthSummary) {
    val typography = AppThemeTokens.typography
    val colors = AppThemeTokens.colors
    SubCalCard(modifier = Modifier.testTag("month_card")) {
        Column(modifier = Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.home_month_title, summary.month),
                    style = typography.labelLg,
                    color = colors.textSecondary,
                )
                summary.range?.let { range ->
                    Text(
                        text =
                            stringResource(
                                R.string.home_month_range,
                                range.start.monthValue,
                                range.start.dayOfMonth,
                                range.endInclusive.monthValue,
                                range.endInclusive.dayOfMonth,
                            ),
                        style = typography.labelSm,
                        color = colors.textTertiary,
                    )
                }
            }
            MoneyText(
                money = Money(summary.totalKrw, Currency.KRW),
                style = typography.displayHeroMobile,
                tone = MoneyTone.HIGHLIGHT,
                modifier = Modifier.testTag("month_total"),
            )
            ChangeText(summary.changeKrw)
            Text(
                text =
                    stringResource(
                        R.string.home_month_average,
                        formatMoney(Money(summary.monthlyAverageKrw, Currency.KRW)),
                        formatKrwInManwon(summary.annualKrw),
                    ),
                style = typography.bodySm.copy(fontFeatureSettings = "tnum"),
                color = colors.textSecondary,
            )
        }
    }
}

/** 증가 danger "+", 감소 success "−" (theme-guide 6장 2번) */
@Composable
private fun ChangeText(changeKrw: Long) {
    val colors = AppThemeTokens.colors
    val amount = formatMoney(Money(changeKrw.absoluteValue, Currency.KRW))
    val (text, color) =
        when {
            changeKrw > 0 -> stringResource(R.string.home_month_change_up, amount) to colors.danger
            changeKrw < 0 -> stringResource(R.string.home_month_change_down, amount) to colors.success
            else -> stringResource(R.string.home_month_change_same) to colors.textSecondary
        }
    Text(
        text = text,
        style = AppThemeTokens.typography.labelMd.copy(fontFeatureSettings = "tnum"),
        color = color,
        modifier = Modifier.testTag("month_change"),
    )
}

@Composable
private fun UpcomingSection(
    upcoming: List<UpcomingPayment>,
    onAction: (HomeAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.home_upcoming_title),
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            SubCalSecondaryButton(
                text = stringResource(R.string.home_upcoming_see_all),
                onClick = { onAction(HomeAction.SeeAllClicked) },
                size = SubCalButtonSize.SMALL,
                modifier = Modifier.testTag("see_all"),
            )
        }
        SubCalCard(modifier = Modifier.testTag("upcoming_card")) {
            if (upcoming.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_upcoming_empty),
                    style = AppThemeTokens.typography.bodyMd,
                    color = AppThemeTokens.colors.textSecondary,
                    modifier = Modifier.padding(AppSpacing.lg),
                )
            }
            upcoming.forEachIndexed { index, item ->
                if (index > 0) SubCalDivider(inset = AppSpacing.marginMobile)
                PaymentRow(
                    payment = item.payment,
                    daysUntil = item.daysUntil,
                    onClick = { onAction(HomeAction.UpcomingClicked(item.payment.subscription.id)) },
                )
            }
        }
    }
}

/** 이용 중 구독의 카테고리별 개수. 누르면 목록 탭(필터) */
@Composable
private fun CategoryChips(
    categories: List<CategoryCount>,
    onAction: (HomeAction) -> Unit,
) {
    if (categories.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        categories.forEach { (category, count) ->
            SubCalFilterChip(
                text = stringResource(R.string.home_category_chip, categoryLabel(category), count),
                selected = false,
                onClick = { onAction(HomeAction.CategoryClicked(category)) },
                role = Role.Button,
                modifier = Modifier.testTag("category_${category.name.lowercase()}"),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun HomePreview() {
    AppTheme { HomeScreen(state = previewHomeState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun HomeMonthStartPreview() {
    AppTheme {
        HomeScreen(
            state =
                previewHomeState(monthStartDay = 25).copy(
                    showBillingDateHint = true,
                    yearReportBanner = YearReportBanner(year = 2026, totalKrw = 635_780),
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun HomeEmptyPreview() {
    AppTheme { HomeScreen(state = previewHomeState(empty = true), onAction = {}) }
}
