package jpark.bro.subcal.feature.subscription.impl.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.subscription.impl.R

// 3차 카드: 사용 체크 · 파티

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

/** 파티 (product-spec 4-4): "파티 n인 · 내 몫 n원", 멤버가 없으면 추가 안내 */
@Composable
internal fun PartyCard(
    party: DetailParty,
    onClick: () -> Unit,
) {
    SubCalCard {
        SubCalListItem(
            title = stringResource(R.string.subscription_detail_party_title),
            subtitle =
                if (party.size > 1) {
                    stringResource(R.string.subscription_detail_party_summary, party.size, formatMoney(party.myShare))
                } else {
                    stringResource(R.string.subscription_detail_party_empty)
                },
            onClick = onClick,
            modifier = Modifier.testTag("party_row"),
        )
    }
}
