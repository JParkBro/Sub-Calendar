package jpark.bro.subcal.feature.subscription.impl.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBadge
import jpark.bro.subcal.core.designsystem.component.SubCalBadgeTone
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDdayText
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.domain.billing.PaymentHistoryEntry
import jpark.bro.subcal.core.domain.billing.PriceTrend
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.billingCycleLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.subscription.impl.R
import java.time.LocalDate

/** 아이콘 · 이름 · 상태 뱃지 · "요금제 · 주기 · 결제 수단" */
@Composable
internal fun DetailHeaderCard(content: DetailContent) {
    val subtitle =
        listOfNotNull(
            content.planName,
            billingCycleLabel(content.cycle, content.firstBillingDate),
            content.paymentMethod,
        ).joinToString(stringResource(R.string.subscription_detail_separator))
    val inactive = content.status == DetailStatus.PAUSED || content.status == DetailStatus.CANCELLED
    SubCalCard {
        SubCalListItem(
            title = content.name,
            subtitle = subtitle,
            showChevron = false,
            emphasized = true,
            leading = { SubCalServiceIcon(name = content.name, colorKey = content.colorKey, dimmed = inactive) },
            trailing = { StatusBadge(content.status) },
        )
    }
}

@Composable
private fun StatusBadge(status: DetailStatus) {
    val (text, tone) =
        when (status) {
            DetailStatus.ACTIVE -> R.string.subscription_detail_status_active to SubCalBadgeTone.SUCCESS
            DetailStatus.TRIAL -> R.string.subscription_detail_status_trial to SubCalBadgeTone.CYCLE
            DetailStatus.PAUSED -> R.string.subscription_detail_status_paused to SubCalBadgeTone.PAUSED
            DetailStatus.CANCELLED -> R.string.subscription_detail_status_cancelled to SubCalBadgeTone.CANCELLED
        }
    SubCalBadge(text = stringResource(text), tone = tone, modifier = Modifier.testTag("status_badge"))
}

/** 금액(주기) · 지금까지(누적 지출) */
@Composable
internal fun StatCards(content: DetailContent) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        StatCard(
            label = billingCycleLabel(content.cycle, content.firstBillingDate),
            modifier = Modifier.weight(1f),
        ) {
            MoneyText(money = content.amount, style = AppThemeTokens.typography.headlineMd)
        }
        StatCard(
            label = stringResource(R.string.subscription_detail_cumulative, content.paymentCount),
            modifier = Modifier.weight(1f),
        ) {
            MoneyText(
                money = Money(content.cumulativeKrw, Currency.KRW),
                style = AppThemeTokens.typography.headlineMd,
                tone = MoneyTone.HIGHLIGHT,
                modifier = Modifier.testTag("cumulative"),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
) {
    SubCalCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(text = label, style = AppThemeTokens.typography.labelMd, color = AppThemeTokens.colors.textSecondary)
            value()
        }
    }
}

@Composable
internal fun NextPaymentCard(
    next: NextPayment,
    isTrial: Boolean,
) {
    SubCalCard {
        SubCalListItem(
            title =
                stringResource(
                    if (isTrial) R.string.subscription_detail_trial_end else R.string.subscription_detail_next_payment,
                ),
            subtitle =
                stringResource(
                    R.string.subscription_detail_month_day,
                    next.date.monthValue,
                    next.date.dayOfMonth,
                ),
            showChevron = false,
            trailing = { SubCalDdayText(daysUntil = next.daysUntil, style = AppThemeTokens.typography.labelLg) },
            modifier = Modifier.testTag("next_payment"),
        )
    }
}

/** 결제 이력 카드. [onShowAll]이 있으면 "더보기" */
@Composable
internal fun HistoryCard(
    entries: List<PaymentHistoryEntry>,
    onShowAll: (() -> Unit)?,
) {
    SubCalCard(modifier = Modifier.testTag("history_card")) {
        Text(
            text = stringResource(R.string.subscription_detail_history),
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            modifier =
                Modifier.padding(
                    start = AppSpacing.marginMobile,
                    end = AppSpacing.marginMobile,
                    top = AppSpacing.lg,
                ),
        )
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.subscription_detail_history_empty),
                style = AppThemeTokens.typography.bodyMd,
                color = AppThemeTokens.colors.textSecondary,
                modifier = Modifier.padding(AppSpacing.marginMobile),
            )
        }
        entries.forEach { entry -> HistoryRow(entry) }
        onShowAll?.let {
            SubCalDivider(inset = AppSpacing.marginMobile)
            SubCalListItem(
                title = stringResource(R.string.subscription_detail_history_more),
                onClick = it,
                modifier = Modifier.testTag("history_more"),
            )
        }
    }
}

/** 결제 한 줄(날짜 · 금액 · 인상/인하) 또는 일시정지 구간 */
@Composable
internal fun HistoryRow(entry: PaymentHistoryEntry) {
    when (entry) {
        is PaymentHistoryEntry.Paid -> {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = fullDate(entry.date),
                    style = AppThemeTokens.typography.bodyMd.copy(fontFeatureSettings = "tnum"),
                    color = AppThemeTokens.colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                MoneyText(money = entry.payment.amount, style = AppThemeTokens.typography.labelLg)
                when (entry.trend) {
                    PriceTrend.UP -> {
                        SubCalBadge(
                            stringResource(R.string.subscription_detail_price_up),
                            SubCalBadgeTone.SOON,
                        )
                    }

                    PriceTrend.DOWN -> {
                        SubCalBadge(
                            stringResource(R.string.subscription_detail_price_down),
                            SubCalBadgeTone.SUCCESS,
                        )
                    }

                    PriceTrend.SAME -> {
                        Unit
                    }
                }
            }
        }

        is PaymentHistoryEntry.Paused -> {
            val end = entry.end
            Text(
                text =
                    if (end == null) {
                        stringResource(R.string.subscription_detail_paused_open, fullDate(entry.start))
                    } else {
                        stringResource(R.string.subscription_detail_paused_range, fullDate(entry.start), fullDate(end))
                    },
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textTertiary,
                modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            )
        }
    }
}

/** 사용 체크 (product-spec 4-4): "+1"은 오늘로 기록, 길게 누르면 날짜 선택 */
@Composable
internal fun UsageCard(
    usage: DetailUsage,
    onAction: (SubscriptionDetailAction) -> Unit,
) {
    SubCalCard {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = stringResource(R.string.subscription_detail_usage_title),
                    style = AppThemeTokens.typography.labelMd,
                    color = AppThemeTokens.colors.textSecondary,
                )
                Text(
                    text =
                        usage.costPerUseKrw?.let { cost ->
                            stringResource(
                                R.string.subscription_detail_usage_summary,
                                usage.count,
                                formatMoney(Money(cost, Currency.KRW)),
                            )
                        } ?: stringResource(R.string.subscription_detail_usage_none),
                    style = AppThemeTokens.typography.bodyMd.copy(fontFeatureSettings = "tnum"),
                    color = AppThemeTokens.colors.textPrimary,
                    modifier = Modifier.testTag("usage_summary"),
                )
            }
            SubCalPrimaryButton(
                text = stringResource(R.string.subscription_detail_usage_add),
                onClick = { onAction(SubscriptionDetailAction.UsageAddClicked) },
                size = SubCalButtonSize.SMALL,
                onLongClick = { onAction(SubscriptionDetailAction.UsageAddLongClicked) },
                longClickLabel = stringResource(R.string.subscription_detail_usage_pick_date),
                modifier = Modifier.testTag("usage_add_button"),
            )
        }
    }
}

@Composable
internal fun MemoCard(memo: String) {
    SubCalCard {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.subscription_detail_memo),
                style = AppThemeTokens.typography.labelMd,
                color = AppThemeTokens.colors.textSecondary,
            )
            Text(text = memo, style = AppThemeTokens.typography.bodyMd, color = AppThemeTokens.colors.textPrimary)
        }
    }
}

@Composable
internal fun fullDate(date: LocalDate): String =
    stringResource(R.string.subscription_date_full, date.year, date.monthValue, date.dayOfMonth)
