package jpark.bro.subcal.feature.subscription.impl.list

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.billingCycleLabel
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import jpark.bro.subcal.feature.subscription.impl.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun SectionHeader(
    label: String,
    count: Int,
) {
    Row(
        modifier = Modifier.padding(start = AppSpacing.xs, top = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(text = label, style = AppThemeTokens.typography.labelMd, color = AppThemeTokens.colors.textPrimary)
        Text(
            text = count.toString(),
            style = AppThemeTokens.typography.labelMd,
            color = AppThemeTokens.colors.textTertiary,
        )
    }
}

@Composable
internal fun SubscriptionRowCard(
    row: SubscriptionRow,
    onClick: () -> Unit,
) {
    // "매월 14일 · 파티 4인" (product-spec 4-2)
    val cycle =
        billingCycleLabel(row.cycle, row.firstBillingDate).let { label ->
            row.partySize?.let { stringResource(R.string.subscription_list_row_party, label, it) } ?: label
        }
    SubCalCard(onClick = onClick, modifier = Modifier.testTag("subscription_row")) {
        SubCalListItem(
            title = row.name,
            subtitle =
                row.planName?.let {
                    stringResource(
                        R.string.subscription_list_row_subtitle,
                        cycle,
                        it,
                    )
                } ?: cycle,
            showChevron = false,
            leading = { SubCalServiceIcon(name = row.name, colorKey = row.colorKey, dimmed = row.isInactive) },
            trailing = {
                Column(horizontalAlignment = Alignment.End) {
                    MoneyText(money = row.amount, style = AppThemeTokens.typography.labelLg)
                    row.monthlyKrw?.let { monthly ->
                        Text(
                            text =
                                stringResource(
                                    R.string.subscription_list_row_monthly,
                                    formatMoney(Money(monthly, Currency.KRW)),
                                ),
                            style = AppThemeTokens.typography.labelSm.copy(fontFeatureSettings = "tnum"),
                            color = AppThemeTokens.colors.textSecondary,
                        )
                    }
                }
            },
        )
    }
}

@Composable
internal fun EmptyContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.subscription_list_empty),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}

@Composable
internal fun SortSheet(
    selected: ListSort,
    onAction: (SubscriptionListAction) -> Unit,
) {
    SubCalBottomSheet(onDismissRequest = { onAction(SubscriptionListAction.SortSheetDismissed) }) {
        Text(
            text = stringResource(R.string.subscription_list_sort_title),
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
        )
        Column(modifier = Modifier.selectableGroup().padding(bottom = AppSpacing.xl)) {
            ListSort.entries.forEach { sort ->
                SubCalListItem(
                    title = stringResource(sort.labelRes),
                    onClick = { onAction(SubscriptionListAction.SortSelected(sort)) },
                    showChevron = false,
                    emphasized = sort == selected,
                    modifier = Modifier.testTag("sort_${sort.name.lowercase()}"),
                )
            }
        }
    }
}

@get:StringRes
internal val StatusFilter.labelRes: Int
    get() =
        when (this) {
            StatusFilter.ACTIVE -> R.string.subscription_list_filter_active
            StatusFilter.PAUSED -> R.string.subscription_list_filter_paused
            StatusFilter.CANCELLED -> R.string.subscription_list_filter_cancelled
            StatusFilter.TRIAL -> R.string.subscription_list_filter_trial
        }

@get:StringRes
internal val ListSort.labelRes: Int
    get() =
        when (this) {
            ListSort.NEXT_BILLING -> R.string.subscription_list_sort_next_billing
            ListSort.AMOUNT -> R.string.subscription_list_sort_amount
            ListSort.NAME -> R.string.subscription_list_sort_name
        }
