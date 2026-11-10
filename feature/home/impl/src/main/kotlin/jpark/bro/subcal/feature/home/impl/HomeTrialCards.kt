package jpark.bro.subcal.feature.home.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.core.designsystem.R as DesignR

/** "무료체험 n건 종료 임박" → 무료체험 화면 (product-spec 4-1) */
@Composable
internal fun TrialWarningCard(
    count: Int,
    onAction: (HomeAction) -> Unit,
) {
    SubCalCard(
        onClick = { onAction(HomeAction.TrialWarningClicked) },
        modifier = Modifier.testTag("trial_warning"),
    ) {
        SubCalListItem(
            title = stringResource(R.string.home_trial_warning, count),
            subtitle = stringResource(R.string.home_trial_warning_message),
            onClick = { onAction(HomeAction.TrialWarningClicked) },
            leading = {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_warning),
                    contentDescription = null,
                    tint = AppThemeTokens.colors.danger,
                )
            },
        )
    }
}

/** "결제됐나요?": 첫 결제일이 지난 체험의 결과를 바로 고른다 (product-spec 2-7) */
@Composable
internal fun UnresolvedTrialCard(
    trial: UnresolvedTrial,
    isWorking: Boolean,
    onAction: (HomeAction) -> Unit,
) {
    SubCalCard(modifier = Modifier.testTag("unresolved_${trial.id}")) {
        Column(modifier = Modifier.padding(bottom = AppSpacing.md)) {
            SubCalListItem(
                title = stringResource(R.string.home_unresolved_title, trial.name),
                subtitle =
                    stringResource(
                        R.string.home_unresolved_message,
                        trial.firstBillingDate.monthValue,
                        trial.firstBillingDate.dayOfMonth,
                        formatMoney(trial.amount),
                    ),
                leading = { SubCalServiceIcon(name = trial.name, colorKey = trial.colorKey) },
            )
            Row(
                modifier = Modifier.padding(horizontal = AppSpacing.marginMobile),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                SubCalSecondaryButton(
                    text = stringResource(R.string.home_unresolved_cancelled),
                    onClick = { onAction(HomeAction.UnresolvedCancelledClicked(trial.id)) },
                    enabled = !isWorking,
                    size = SubCalButtonSize.SMALL,
                    modifier = Modifier.weight(1f).testTag("unresolved_cancelled_${trial.id}"),
                )
                SubCalPrimaryButton(
                    text = stringResource(R.string.home_unresolved_keep),
                    onClick = { onAction(HomeAction.UnresolvedKeepClicked(trial.id)) },
                    enabled = !isWorking,
                    size = SubCalButtonSize.SMALL,
                    modifier = Modifier.weight(1f).testTag("unresolved_keep_${trial.id}"),
                )
            }
        }
    }
}
