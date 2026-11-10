package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 진행바 (무료체험 · 가성비). 트랙 fill, 채움 primary → accent 그라데이션. [color]를 주면 그 색 한 가지로 채운다 */
@Composable
fun SubCalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    color: Color? = null,
) {
    val colors = AppThemeTokens.colors
    val value = progress.coerceIn(0f, 1f)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .clip(AppShapes.full)
                .background(colors.fill)
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f) },
    ) {
        if (value > 0f) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(value)
                        .fillMaxHeight()
                        .clip(AppShapes.full)
                        .then(
                            if (color != null) {
                                Modifier.background(color)
                            } else {
                                Modifier.background(Brush.horizontalGradient(listOf(colors.primary, colors.accent)))
                            },
                        ),
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun SubCalProgressBarPreview() {
    SubCalPreviewSurface {
        SubCalProgressBar(progress = 0f)
        SubCalProgressBar(progress = 0.1f)
        SubCalProgressBar(progress = 0.62f)
        SubCalProgressBar(progress = 1f)
        SubCalProgressBar(progress = 1f, color = AppThemeTokens.colors.danger)
    }
}
