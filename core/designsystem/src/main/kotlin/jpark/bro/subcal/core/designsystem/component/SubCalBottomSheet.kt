package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 바텀시트: surface-raised, 뒤 배경 overlay, 드래그 핸들 border-strong. 항상 끝까지 펼친다 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppThemeTokens.colors
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceRaised,
        scrimColor = colors.overlay,
        dragHandle = { SubCalSheetHandle() },
        content = content,
    )
}

/** 시트 위쪽 드래그 핸들 */
@Composable
fun SubCalSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .padding(vertical = AppSpacing.sm + AppSpacing.xs)
                .size(width = 36.dp, height = 4.dp)
                .clip(AppShapes.full)
                .background(AppThemeTokens.colors.borderStrong),
    )
}

/** ModalBottomSheet는 Preview에서 그려지지 않으므로 같은 색 · 핸들로 시트 모양만 보여준다 */
@ComponentPreviews
@Composable
private fun SubCalBottomSheetPreview() {
    SubCalPreviewSurface {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.xl)
                    .background(AppThemeTokens.colors.surfaceRaised)
                    .padding(horizontal = AppSpacing.lg)
                    .padding(bottom = AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            SubCalSheetHandle()
            Text(
                text = "언제부터 적용할까요?",
                style = AppThemeTokens.typography.headlineMd,
                color = AppThemeTokens.colors.textPrimary,
            )
            SubCalPrimaryButton(text = "다음 결제부터", onClick = {}, modifier = Modifier.fillMaxWidth())
            SubCalSecondaryButton(text = "처음부터", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
