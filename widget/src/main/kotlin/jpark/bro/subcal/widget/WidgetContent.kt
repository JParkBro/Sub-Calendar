package jpark.bro.subcal.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import jpark.bro.subcal.core.designsystem.AppFixedColors
import jpark.bro.subcal.core.designsystem.AppRadius
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.DefaultTypography
import jpark.bro.subcal.core.designsystem.component.serviceInitials
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.WidgetBackground
import jpark.bro.subcal.core.model.WidgetSize
import jpark.bro.subcal.core.navigation.AppIntents
import jpark.bro.subcal.core.ui.formatMoney
import kotlin.math.absoluteValue
import jpark.bro.subcal.core.designsystem.R as DesignR

/**
 * 위젯 화면 (product-spec 7장, widget/CLAUDE.md).
 * 작게: 가장 가까운 결제 1건 — 아이콘 · 이름 · D-day · 금액. 누르면 그 구독 상세(없으면 앱).
 * 중간: "{M}월 구독료" + 금액, 다가오는 결제 2행. 누르면 홈.
 */
@Composable
internal fun WidgetContent(
    display: WidgetDisplay,
    size: WidgetSize,
    colors: WidgetColors,
) {
    val context = LocalContext.current
    val openId = if (size == WidgetSize.SMALL) display.upcoming.firstOrNull()?.subscriptionId else null
    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(AppRadius.xl)
                .background(colors.background)
                .padding(AppSpacing.md)
                .clickable(actionStartActivity(context.openAppIntent(openId))),
    ) {
        when (size) {
            WidgetSize.SMALL -> SmallBody(display = display, colors = colors)
            WidgetSize.MEDIUM -> MediumBody(display = display, colors = colors)
        }
    }
}

@Composable
private fun ColumnScope.SmallBody(
    display: WidgetDisplay,
    colors: WidgetColors,
) {
    val context = LocalContext.current
    val payment = display.upcoming.firstOrNull()
    if (payment == null) {
        Message(text = context.getString(R.string.widget_small_empty), colors = colors)
        return
    }
    ServiceIcon(name = payment.name, colorKey = payment.colorKey)
    Spacer(GlanceModifier.height(AppSpacing.xs))
    Text(
        text = payment.name,
        style = TextStyle(color = colors.textSecondary, fontSize = DefaultTypography.labelMd.fontSize),
        maxLines = 1,
    )
    Spacer(GlanceModifier.defaultWeight())
    Text(
        text = context.ddayText(payment.daysUntil),
        style =
            TextStyle(
                color = if (payment.daysUntil <= DANGER_DAYS) colors.danger else colors.textPrimary,
                fontSize = DefaultTypography.headlineLg.fontSize,
                fontWeight = FontWeight.Bold,
            ),
        maxLines = 1,
    )
    Text(
        text = context.formatMoney(payment.amount),
        style = TextStyle(color = colors.textSecondary, fontSize = DefaultTypography.bodySm.fontSize),
        maxLines = 1,
    )
}

@Composable
private fun ColumnScope.MediumBody(
    display: WidgetDisplay,
    colors: WidgetColors,
) {
    val context = LocalContext.current
    if (!display.hasSubscriptions) {
        Message(text = context.getString(R.string.widget_medium_empty), colors = colors)
        return
    }
    Text(
        text = context.getString(R.string.widget_medium_title, display.month),
        style = TextStyle(color = colors.textSecondary, fontSize = DefaultTypography.labelMd.fontSize),
    )
    Text(
        text = context.formatMoney(Money(display.totalKrw, Currency.KRW)),
        style =
            TextStyle(
                color = colors.textPrimary,
                fontSize = DefaultTypography.headlineLg.fontSize,
                fontWeight = FontWeight.Bold,
            ),
        maxLines = 1,
    )
    Spacer(GlanceModifier.defaultWeight())
    display.upcoming.forEach { payment ->
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ServiceIcon(name = payment.name, colorKey = payment.colorKey, size = ROW_ICON_SIZE)
            Spacer(GlanceModifier.width(AppSpacing.sm))
            Text(
                text = payment.name,
                style = TextStyle(color = colors.textPrimary, fontSize = DefaultTypography.labelLg.fontSize),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
            )
            Text(
                text = context.ddayText(payment.daysUntil),
                style =
                    TextStyle(
                        color = if (payment.daysUntil <= DANGER_DAYS) colors.danger else colors.textPrimary,
                        fontSize = DefaultTypography.labelLg.fontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                    ),
            )
        }
    }
}

/** 원 + 이니셜 (브랜드 로고 이미지 금지, theme-guide 5장) */
@Composable
private fun ServiceIcon(
    name: String,
    colorKey: String,
    size: androidx.compose.ui.unit.Dp = ICON_SIZE,
) {
    Box(
        modifier =
            GlanceModifier
                .size(size)
                .cornerRadius(size / 2)
                .background(ColorProvider(AppFixedColors.service(colorKey))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = serviceInitials(name),
            style =
                TextStyle(
                    color = ColorProvider(AppFixedColors.onService),
                    fontSize = DefaultTypography.labelSm.fontSize,
                    fontWeight = FontWeight.Bold,
                ),
            maxLines = 1,
        )
    }
}

@Composable
private fun ColumnScope.Message(
    text: String,
    colors: WidgetColors,
) {
    Spacer(GlanceModifier.defaultWeight())
    Text(text = text, style = TextStyle(color = colors.textPrimary, fontSize = DefaultTypography.bodyMd.fontSize))
    Spacer(GlanceModifier.defaultWeight())
}

private val ICON_SIZE = 32.dp
private val ROW_ICON_SIZE = 24.dp

/** 오늘 ~ 3일 이내 결제는 danger (CLAUDE.md D-day 색) */
private const val DANGER_DAYS = 3

/** D-day: 오늘 · 내일 · D-n (product-spec 2-12) */
private fun Context.ddayText(daysUntil: Int): String =
    when {
        daysUntil < 0 -> getString(DesignR.string.common_dday_elapsed, daysUntil.absoluteValue)
        daysUntil == 0 -> getString(DesignR.string.common_dday_today)
        daysUntil == 1 -> getString(DesignR.string.common_dday_tomorrow)
        else -> getString(DesignR.string.common_dday_remaining, daysUntil)
    }

/** 앱을 연다. [subscriptionId]가 있으면 [홈, 상세] 백스택으로 (MainActivity가 처리) */
internal fun Context.openAppIntent(subscriptionId: String?): Intent =
    (packageManager.getLaunchIntentForPackage(packageName) ?: Intent()).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (subscriptionId != null) putExtra(AppIntents.EXTRA_OPEN_SUBSCRIPTION_ID, subscriptionId)
    }

/** product-spec 9장 샘플 (2026.10.30) */
@Suppress("MagicNumber") // 샘플 데이터
private val PreviewDisplay =
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

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 110, heightDp = 110)
@Composable
private fun SmallWidgetPreview() {
    WidgetContent(PreviewDisplay, WidgetSize.SMALL, widgetColorScheme(WidgetBackground.THEME).toGlance())
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun MediumWidgetPreview() {
    WidgetContent(PreviewDisplay, WidgetSize.MEDIUM, widgetColorScheme(WidgetBackground.DARK).toGlance())
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 110, heightDp = 110)
@Composable
private fun EmptyWidgetPreview() {
    WidgetContent(
        PreviewDisplay.copy(upcoming = emptyList(), hasSubscriptions = false),
        WidgetSize.SMALL,
        widgetColorScheme(WidgetBackground.WHITE).toGlance(),
    )
}
