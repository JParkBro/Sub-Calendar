package jpark.bro.subcal.feature.subscription.impl.edit

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppFixedColors
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDangerButton
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalSwitch
import jpark.bro.subcal.core.designsystem.component.SubCalTextField
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.PriceChangeTiming
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.subscription.impl.R
import java.time.LocalDate

@Composable
internal fun PriceChangeSheet(onAction: (SubscriptionEditAction) -> Unit) {
    SubCalBottomSheet(onDismissRequest = { onAction(SubscriptionEditAction.SheetDismissed) }) {
        SheetTitle(stringResource(R.string.subscription_edit_price_change_title))
        Column(modifier = Modifier.padding(bottom = AppSpacing.xl)) {
            SubCalListItem(
                title = stringResource(R.string.subscription_edit_price_change_next),
                subtitle = stringResource(R.string.subscription_edit_price_change_next_hint),
                onClick = {
                    onAction(
                        SubscriptionEditAction.PriceChangeTimingSelected(PriceChangeTiming.NEXT_PAYMENT),
                    )
                },
                emphasized = true,
                modifier = Modifier.testTag("price_change_next"),
            )
            SubCalListItem(
                title = stringResource(R.string.subscription_edit_price_change_start),
                subtitle = stringResource(R.string.subscription_edit_price_change_start_hint),
                onClick = { onAction(SubscriptionEditAction.PriceChangeTimingSelected(PriceChangeTiming.FROM_START)) },
                modifier = Modifier.testTag("price_change_start"),
            )
        }
    }
}

@Composable
internal fun DeleteSheet(onAction: (SubscriptionEditAction) -> Unit) {
    SubCalBottomSheet(onDismissRequest = { onAction(SubscriptionEditAction.SheetDismissed) }) {
        SheetTitle(stringResource(R.string.subscription_edit_delete_title))
        Column(
            modifier =
                Modifier.padding(
                    start = AppSpacing.marginMobile,
                    end = AppSpacing.marginMobile,
                    bottom = AppSpacing.xl,
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.subscription_edit_delete_message),
                style = AppThemeTokens.typography.bodyMd,
                color = AppThemeTokens.colors.textSecondary,
            )
            SubCalDangerButton(
                text = stringResource(R.string.subscription_edit_delete),
                onClick = { onAction(SubscriptionEditAction.DeleteConfirmed) },
                filled = true,
                modifier = Modifier.fillMaxWidth().testTag("delete_confirm"),
            )
            SubCalSecondaryButton(
                text = stringResource(R.string.subscription_edit_delete_keep),
                onClick = { onAction(SubscriptionEditAction.SheetDismissed) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(
        text = text,
        style = AppThemeTokens.typography.headlineSm,
        color = AppThemeTokens.colors.textPrimary,
        modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
    )
}
