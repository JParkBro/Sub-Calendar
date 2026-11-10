package jpark.bro.subcal.feature.subscription.impl.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.feature.subscription.impl.R
import jpark.bro.subcal.core.designsystem.R as DesignR

/** 상세 헤더 메뉴: 해지 고민함에 담기 · 빼기 (product-spec 4-4). 해지한 구독은 메뉴가 없다 */
@Composable
internal fun DetailMenu(
    isOpen: Boolean,
    considerCancel: Boolean,
    showConsiderCancel: Boolean,
    onAction: (SubscriptionDetailAction) -> Unit,
) {
    if (!showConsiderCancel) return
    Box {
        SubCalIconButton(
            icon = DesignR.drawable.ic_more_vert,
            contentDescription = stringResource(R.string.subscription_detail_menu),
            onClick = { onAction(SubscriptionDetailAction.MenuClicked) },
            modifier = Modifier.testTag("detail_menu"),
        )
        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = { onAction(SubscriptionDetailAction.MenuDismissed) },
            containerColor = AppThemeTokens.colors.surfaceRaised,
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text =
                            stringResource(
                                if (considerCancel) {
                                    R.string.subscription_detail_consider_remove
                                } else {
                                    R.string.subscription_detail_consider_add
                                },
                            ),
                        style = AppThemeTokens.typography.labelLg,
                        color = AppThemeTokens.colors.textPrimary,
                    )
                },
                onClick = { onAction(SubscriptionDetailAction.ConsiderCancelToggled) },
                modifier = Modifier.testTag("menu_consider_cancel"),
            )
        }
    }
}
