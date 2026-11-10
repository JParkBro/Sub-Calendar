package jpark.bro.subcal.widget.config

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppRadius
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalDdayText
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.widget.R
import jpark.bro.subcal.widget.WidgetDisplay
import jpark.bro.subcal.widget.WidgetPayment
import jpark.bro.subcal.widget.widgetColorScheme

/**
 * 설정 화면의 위젯 미리보기 (Compose). 실제 위젯(Glance)과 같은 값 · 색을 쓴다.
 * 테마 배경은 앱 화면 모드가 아니라 기기 다크 모드를 따르므로 [isSystemInDarkTheme]로 고른다.
 */
@Composable
internal fun WidgetPreviewCard(
    display: WidgetDisplay,
    size: WidgetSize,
    background: WidgetBackground,
    modifier: Modifier = Modifier,
) {
    val scheme = widgetColorScheme(background)
    val palette = if (isSystemInDarkTheme()) scheme.night else scheme.day
    Column(
        modifier =
            modifier
                .size(width = if (size == WidgetSize.SMALL) SmallWidth else MediumWidth, height = WidgetHeight)
                .clip(RoundedCornerShape(AppRadius.xl))
                .background(palette.background)
                .padding(AppSpacing.md),
    ) {
        val first = display.upcoming.firstOrNull()
        when {
            size == WidgetSize.SMALL && first != null -> {
                SubCalServiceIcon(name = first.name, colorKey = first.colorKey, size = 32.dp)
                Text(
                    text = first.name,
                    style = AppThemeTokens.typography.labelMd,
                    color = palette.textSecondary,
                    maxLines = 1,
                )
                Spacer(Modifier.weight(1f))
                SubCalDdayText(daysUntil = first.daysUntil, style = AppThemeTokens.typography.headlineLg)
                Text(
                    text = formatMoney(first.amount),
                    style = AppThemeTokens.typography.bodySm,
                    color = palette.textSecondary,
                )
            }

            size == WidgetSize.MEDIUM && display.hasSubscriptions -> {
                Text(
                    text = stringResource(R.string.widget_medium_title, display.month),
                    style = AppThemeTokens.typography.labelMd,
                    color = palette.textSecondary,
                )
                Text(
                    text = formatMoney(Money(display.totalKrw, Currency.KRW)),
                    style = AppThemeTokens.typography.headlineLg,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                )
                Spacer(Modifier.weight(1f))
                display.upcoming.forEach { PreviewRow(it, palette.textPrimary) }
            }

            else -> {
                Spacer(Modifier.weight(1f))
                Text(
                    text =
                        stringResource(
                            if (size == WidgetSize.SMALL) R.string.widget_small_empty else R.string.widget_medium_empty,
                        ),
                    style = AppThemeTokens.typography.bodyMd,
                    color = palette.textPrimary,
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PreviewRow(
    payment: WidgetPayment,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.padding(top = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        SubCalServiceIcon(name = payment.name, colorKey = payment.colorKey, size = 24.dp)
        Text(
            text = payment.name,
            style = AppThemeTokens.typography.labelLg,
            color = color,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        SubCalDdayText(daysUntil = payment.daysUntil, style = AppThemeTokens.typography.labelLg)
    }
}

private val SmallWidth = 160.dp
private val MediumWidth = 320.dp
private val WidgetHeight = 160.dp

/** product-spec 9장 샘플 (2026.10.30) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewWidgetDisplay(): WidgetDisplay =
    WidgetDisplay(
        month = 10,
        totalKrw = 72_490,
        upcoming =
            listOf(
                WidgetPayment("spotify", "스포티파이", "GREEN", 0, Money(10_900, Currency.KRW)),
                WidgetPayment("netflix", "넷플릭스 프리미엄", "RED", 3, Money(17_000, Currency.KRW)),
            ),
        hasSubscriptions = true,
    )
