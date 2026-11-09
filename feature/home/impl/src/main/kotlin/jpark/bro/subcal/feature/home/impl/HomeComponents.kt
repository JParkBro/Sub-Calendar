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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import jpark.bro.subcal.feature.subscription.api.ServicePickerKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionEditKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue
import jpark.bro.subcal.core.designsystem.R as DesignR

/** 온보딩에서 오늘 결제로 한꺼번에 등록했으니 실제 결제일로 고치라는 안내 (product-spec 5장) */
@Composable
internal fun BillingDateHintCard(onAction: (HomeAction) -> Unit) {
    SubCalCard(
        onClick = { onAction(HomeAction.BillingDateHintClicked) },
        modifier = Modifier.testTag("billing_date_hint"),
    ) {
        SubCalListItem(
            title = stringResource(R.string.home_billing_date_hint_title),
            subtitle = stringResource(R.string.home_billing_date_hint_message),
            showChevron = false,
            trailing = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_close,
                    contentDescription = stringResource(R.string.home_billing_date_hint_close),
                    onClick = { onAction(HomeAction.BillingDateHintDismissed) },
                    modifier = Modifier.testTag("billing_date_hint_close"),
                )
            },
        )
    }
}

/** 빈 상태: 안내 + 빠른 등록 칩 */
@Composable
internal fun EmptyCard(onAction: (HomeAction) -> Unit) {
    SubCalCard(modifier = Modifier.testTag("empty_card")) {
        Column(modifier = Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = stringResource(R.string.home_empty_title),
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textPrimary,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                QuickAdd.entries.forEach { quickAdd ->
                    SubCalFilterChip(
                        text = stringResource(quickAdd.labelRes),
                        selected = false,
                        onClick = { onAction(HomeAction.QuickAddClicked(quickAdd)) },
                        role = Role.Button,
                        modifier = Modifier.testTag("quick_${quickAdd.name.lowercase()}"),
                    )
                }
            }
        }
    }
}

@get:StringRes
private val QuickAdd.labelRes: Int
    get() =
        when (this) {
            QuickAdd.NETFLIX -> R.string.home_quick_netflix
            QuickAdd.YOUTUBE_PREMIUM -> R.string.home_quick_youtube_premium
            QuickAdd.COUPANG_WOW -> R.string.home_quick_coupang_wow
            QuickAdd.MANUAL -> R.string.home_quick_manual
        }
