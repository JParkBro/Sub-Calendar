package jpark.bro.subcal.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.R
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/**
 * 필터 칩. 활성 text-primary 배경 + bg 글자, 비활성 fill + text-secondary.
 * 여러 개를 고를 수 있는 묶음(알림 등)은 [role]에 [Role.Checkbox]를 넘긴다.
 */
@Composable
fun SubCalFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.RadioButton,
) {
    val colors = AppThemeTokens.colors
    Box(
        modifier =
            modifier
                .heightIn(min = 36.dp)
                .clip(AppShapes.full)
                .background(if (selected) colors.textPrimary else colors.fill)
                .selectable(selected = selected, role = role, onClick = onClick)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppThemeTokens.typography.labelMd,
            color = if (selected) colors.bg else colors.textSecondary,
        )
    }
}

/** 세그먼트 항목 */
@Immutable
data class SubCalSegment(
    val text: String,
    @DrawableRes val icon: Int? = null,
)

/** 세그먼트 컨트롤. 트랙 fill, 선택 항목 surface + shadow + text-primary, 비선택 text-secondary */
@Composable
fun SubCalSegmentedControl(
    segments: List<SubCalSegment>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShapes.full)
                .background(colors.fill)
                .padding(AppSpacing.xs)
                .selectableGroup(),
    ) {
        segments.forEachIndexed { index, segment ->
            val selected = index == selectedIndex
            val tint = if (selected) colors.textPrimary else colors.textSecondary
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .then(
                            if (selected) {
                                Modifier.shadow(
                                    elevation = 2.dp,
                                    shape = AppShapes.full,
                                    ambientColor = colors.shadow,
                                    spotColor = colors.shadow,
                                )
                            } else {
                                Modifier
                            },
                        ).clip(AppShapes.full)
                        .background(if (selected) colors.surface else colors.fill)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (segment.icon != null) {
                    Icon(
                        painter = painterResource(segment.icon),
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                }
                Text(
                    text = segment.text,
                    style = AppThemeTokens.typography.labelLg,
                    color = tint,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun SubCalChipPreview() {
    SubCalPreviewSurface {
        var filter by remember { mutableStateOf("이용 중") }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            listOf("이용 중", "일시정지", "해지").forEach {
                SubCalFilterChip(text = it, selected = it == filter, onClick = { filter = it })
            }
        }
        var mode by remember { mutableIntStateOf(0) }
        SubCalSegmentedControl(
            segments =
                listOf(
                    SubCalSegment("라이트", R.drawable.ic_light_mode),
                    SubCalSegment("다크", R.drawable.ic_dark_mode),
                    SubCalSegment("시스템", R.drawable.ic_brightness_auto),
                ),
            selectedIndex = mode,
            onSelect = { mode = it },
        )
        SubCalSegmentedControl(
            segments = listOf(SubCalSegment("원"), SubCalSegment("달러")),
            selectedIndex = 1,
            onSelect = {},
        )
    }
}
