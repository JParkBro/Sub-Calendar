package jpark.bro.subcal.core.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppSpacing

/** 버튼 크기. LARGE는 화면 하단 CTA, SMALL은 카드 · 행 안의 작은 버튼 */
enum class SubCalButtonSize(
    internal val minHeight: Dp,
    internal val horizontalPadding: Dp,
    internal val iconSize: Dp,
) {
    LARGE(minHeight = 56.dp, horizontalPadding = AppSpacing.lg, iconSize = 20.dp),
    SMALL(minHeight = 36.dp, horizontalPadding = AppSpacing.md, iconSize = 16.dp),
}
