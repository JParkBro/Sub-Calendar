package jpark.bro.subcal.feature.calendar.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.PaymentRow
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import org.koin.compose.viewmodel.koinViewModel
import java.time.format.TextStyle
import java.util.Locale
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun CalendarRoot(
    onNavigate: (NavKey) -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is CalendarEvent.OpenDetail -> onNavigate(SubscriptionDetailKey(event.subscriptionId))
        }
    }
    CalendarScreen(state = state, onAction = viewModel::onAction)
}

/** 캘린더 (product-spec 4-5) */
@Composable
private fun CalendarScreen(
    state: CalendarState,
    onAction: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("calendar"),
    ) {
        SubCalTopBar(title = stringResource(R.string.calendar_title))
        if (state.isLoading) return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = AppSpacing.marginMobile, end = AppSpacing.marginMobile, bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            MonthHeader(state = state, onAction = onAction)
            SubCalCard {
                CalendarGrid(
                    weeks = state.weeks,
                    selectedDate = state.selected?.date,
                    onDayClick = { onAction(CalendarAction.DayClicked(it.date)) },
                    modifier = Modifier.padding(AppSpacing.sm),
                )
            }
        }
    }

    state.selected?.let { DaySheet(selected = it, onAction = onAction) }
}

/** "yyyy년 M월" + 이전 · 다음, "이번 달 n건 · n원" */
@Composable
private fun MonthHeader(
    state: CalendarState,
    onAction: (CalendarAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubCalIconButton(
                icon = DesignR.drawable.ic_chevron_left,
                contentDescription = stringResource(R.string.calendar_previous_month),
                onClick = { onAction(CalendarAction.PreviousMonthClicked) },
                enabled = state.canGoPrevious,
                modifier = Modifier.testTag("previous_month"),
            )
            Text(
                text = stringResource(R.string.calendar_month, state.month.year, state.month.monthValue),
                style = AppThemeTokens.typography.headlineMd,
                color = AppThemeTokens.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).testTag("month_title"),
            )
            SubCalIconButton(
                icon = DesignR.drawable.ic_chevron_right,
                contentDescription = stringResource(R.string.calendar_next_month),
                onClick = { onAction(CalendarAction.NextMonthClicked) },
                enabled = state.canGoNext,
                modifier = Modifier.testTag("next_month"),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.calendar_month_count, state.paymentCount),
                style = AppThemeTokens.typography.labelMd,
                color = AppThemeTokens.colors.textSecondary,
            )
            MoneyText(
                money = Money(state.totalKrw, Currency.KRW),
                style = AppThemeTokens.typography.labelLg,
                tone = MoneyTone.HIGHLIGHT,
                modifier = Modifier.testTag("month_total"),
            )
        }
    }
}

/** "M월 d일 (요일) · n원" + 그날 결제 */
@Composable
private fun DaySheet(
    selected: SelectedDay,
    onAction: (CalendarAction) -> Unit,
) {
    SubCalBottomSheet(onDismissRequest = { onAction(CalendarAction.SheetDismissed) }) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    stringResource(
                        R.string.calendar_sheet_title,
                        selected.date.monthValue,
                        selected.date.dayOfMonth,
                        selected.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                    ),
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            MoneyText(
                money = Money(selected.totalKrw, Currency.KRW),
                style = AppThemeTokens.typography.headlineSm,
                tone = MoneyTone.HIGHLIGHT,
            )
        }
        Column(modifier = Modifier.padding(bottom = AppSpacing.xl).testTag("day_sheet")) {
            selected.payments.forEachIndexed { index, item ->
                if (index > 0) SubCalDivider(inset = AppSpacing.marginMobile)
                PaymentRow(
                    payment = item.payment,
                    daysUntil = item.daysUntil,
                    onClick = { onAction(CalendarAction.PaymentClicked(item.payment.subscription.id)) },
                )
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun CalendarPreview() {
    AppTheme { CalendarScreen(state = previewCalendarState(), onAction = {}) }
}
