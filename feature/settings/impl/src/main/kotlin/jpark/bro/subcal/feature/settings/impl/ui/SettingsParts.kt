package jpark.bro.subcal.feature.settings.impl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens

/** 설정 화면들이 함께 쓰는 조각: 칩 줄 · 묶음 이름 · 시트 제목 */
@Composable
internal fun ChipRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) { content() }
}

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text,
        style = AppThemeTokens.typography.labelMd,
        color = AppThemeTokens.colors.textSecondary,
        modifier = Modifier.padding(horizontal = AppSpacing.xs),
    )
}

@Composable
internal fun SheetTitle(text: String) {
    Text(
        text = text,
        style = AppThemeTokens.typography.headlineSm,
        color = AppThemeTokens.colors.textPrimary,
        modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
    )
}
