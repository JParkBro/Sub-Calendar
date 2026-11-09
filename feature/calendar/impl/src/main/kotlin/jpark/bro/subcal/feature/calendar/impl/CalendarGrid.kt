package jpark.bro.subcal.feature.calendar.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val DateCircleSize = 32.dp
private val DayIconSize = 16.dp
private val DayCellMinHeight = 64.dp
private const val TRIAL_DASH_PX = 6f
private val TrialDash = floatArrayOf(TRIAL_DASH_PX, TRIAL_DASH_PX)

/** 일요일 시작 월 그리드 (product-spec 4-5) */
@Composable
internal fun CalendarGrid(
    weeks: List<List<CalendarDay?>>,
    selectedDate: LocalDate?,
    onDayClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row {
            WeekHeader.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                    style = AppThemeTokens.typography.labelSm,
                    color = dayOfWeek.numberColor(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        weeks.forEach { week ->
            Row {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f).heightIn(min = DayCellMinHeight)) {
                        day?.let { DayCell(it, isSelected = it.date == selectedDate, onClick = { onDayClick(it) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppThemeTokens.colors
    val description =
        stringResource(R.string.calendar_day_description, day.date.monthValue, day.date.dayOfMonth, day.paymentCount)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(AppShapes.md)
                .clickable(enabled = day.paymentCount > 0, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = description }
                .padding(vertical = AppSpacing.xs)
                .testTag("day_${day.date.dayOfMonth}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val circle =
            when {
                day.isToday -> colors.primary
                isSelected -> colors.primarySoft
                else -> Color.Transparent
            }
        // 오늘 원(primary) 위에서는 on-primary로 그려야 보인다
        val trialColor = if (day.isToday) colors.onPrimary else colors.primary
        Box(
            modifier =
                Modifier
                    .size(DateCircleSize)
                    .clip(AppShapes.full)
                    .background(circle)
                    .then(if (day.hasTrialEnd) Modifier.trialEndMarker(trialColor) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = AppThemeTokens.typography.labelMd,
                color =
                    when {
                        day.isToday -> colors.onPrimary
                        isSelected -> colors.onPrimarySoft
                        else -> day.date.dayOfWeek.numberColor()
                    },
            )
        }
        if (day.icons.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                day.icons.forEach { SubCalServiceIcon(name = it.name, colorKey = it.colorKey, size = DayIconSize) }
            }
        }
        if (day.moreCount > 0) {
            Text(
                text = stringResource(R.string.calendar_day_more, day.moreCount),
                style = AppThemeTokens.typography.labelSm,
                color = colors.textSecondary,
            )
        }
    }
}

/** 체험 종료일: 점선 원 */
private fun Modifier.trialEndMarker(color: Color): Modifier =
    drawBehind {
        val stroke = 1.5.dp.toPx()
        drawCircle(
            color = color,
            radius = size.minDimension / 2 - stroke,
            center = Offset(size.width / 2, size.height / 2),
            style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(TrialDash)),
        )
    }

@Composable
private fun DayOfWeek.numberColor(): Color =
    when (this) {
        DayOfWeek.SUNDAY -> AppThemeTokens.colors.calSunday
        DayOfWeek.SATURDAY -> AppThemeTokens.colors.calSaturday
        else -> AppThemeTokens.colors.textPrimary
    }
