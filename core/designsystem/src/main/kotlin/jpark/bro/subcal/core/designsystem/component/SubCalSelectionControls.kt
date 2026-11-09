package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.R
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 비활성 컨트롤 투명도 */
private const val DISABLED_ALPHA = 0.4f

/** 토글. ON primary + on-primary 손잡이, OFF border-strong + surface 손잡이 */
@Composable
fun SubCalSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppThemeTokens.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
        enabled = enabled,
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                checkedBorderColor = colors.primary,
                uncheckedThumbColor = colors.surface,
                uncheckedTrackColor = colors.borderStrong,
                uncheckedBorderColor = colors.borderStrong,
                disabledCheckedThumbColor = colors.onPrimary,
                disabledCheckedTrackColor = colors.primary,
                disabledCheckedBorderColor = colors.primary,
                disabledUncheckedThumbColor = colors.surface,
                disabledUncheckedTrackColor = colors.borderStrong,
                disabledUncheckedBorderColor = colors.borderStrong,
            ),
    )
}

/** 체크박스 (원형). 빈 상태 surface + border-strong, 선택 primary + on-primary 체크 */
@Composable
fun SubCalCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppThemeTokens.colors
    val toggle =
        if (onCheckedChange != null) {
            Modifier.toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
        } else {
            Modifier
        }
    Box(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .then(toggle)
                .alpha(if (enabled) 1f else DISABLED_ALPHA),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(22.dp)
                    .clip(AppShapes.full)
                    .background(if (checked) colors.primary else colors.surface)
                    .then(if (checked) Modifier else Modifier.border(1.5.dp, colors.borderStrong, AppShapes.full)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun SubCalSelectionControlsPreview() {
    SubCalPreviewSurface {
        var on by remember { mutableStateOf(true) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubCalSwitch(checked = on, onCheckedChange = { on = it })
            SubCalSwitch(checked = false, onCheckedChange = {})
            SubCalSwitch(checked = true, onCheckedChange = {}, enabled = false)
        }
        var agreed by remember { mutableStateOf(true) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubCalCheckbox(checked = agreed, onCheckedChange = { agreed = it })
            SubCalCheckbox(checked = false, onCheckedChange = {})
            SubCalCheckbox(checked = true, onCheckedChange = {}, enabled = false)
        }
    }
}
