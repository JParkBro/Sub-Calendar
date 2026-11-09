package jpark.bro.subcal.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.R
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 원형 보조 아이콘 버튼 (시트 닫기 등): fill 배경 + text-primary 아이콘 */
@Composable
fun SubCalIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppThemeTokens.colors
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .clip(AppShapes.full)
                .background(colors.fill),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = if (enabled) colors.textPrimary else colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** FAB (+ 버튼): primary · 눌림 primary-pressed · shadow */
@Composable
fun SubCalFab(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier =
            modifier
                .size(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = AppShapes.full,
                    ambientColor = colors.shadow,
                    spotColor = colors.shadow,
                ).clip(AppShapes.full)
                .background(if (pressed) colors.primaryPressed else colors.primary)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    role = Role.Button,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = colors.onPrimary,
            modifier = Modifier.size(28.dp),
        )
    }
}

@ComponentPreviews
@Composable
private fun SubCalIconButtonPreview() {
    SubCalPreviewSurface {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubCalIconButton(icon = R.drawable.ic_close, contentDescription = "닫기", onClick = {})
            SubCalIconButton(icon = R.drawable.ic_close, contentDescription = "닫기", onClick = {}, enabled = false)
            SubCalFab(icon = R.drawable.ic_add, contentDescription = "추가", onClick = {})
        }
    }
}
