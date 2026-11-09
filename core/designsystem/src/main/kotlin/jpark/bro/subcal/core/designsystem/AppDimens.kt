@file:Suppress("MagicNumber") // 토큰 정의 파일

package jpark.bro.subcal.core.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 모서리 반지름 (theme-guide.md 7장, 값은 Stitch DESIGN.md rounded). Shape를 못 쓰는 Glance 위젯은 이 값을 쓴다 */
@Immutable
object AppRadius {
    val sm: Dp = 4.dp
    val default: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
}

/** 모서리 (theme-guide.md 7장) */
@Immutable
object AppShapes {
    val sm: Shape = RoundedCornerShape(AppRadius.sm)
    val default: Shape = RoundedCornerShape(AppRadius.default)
    val md: Shape = RoundedCornerShape(AppRadius.md)
    val lg: Shape = RoundedCornerShape(AppRadius.lg)
    val xl: Shape = RoundedCornerShape(AppRadius.xl)
    val full: Shape = CircleShape
}

/** 간격 (theme-guide.md 7장, 값은 Stitch DESIGN.md spacing) */
@Immutable
object AppSpacing {
    /** 화면 좌우 여백 */
    val marginMobile: Dp = 16.dp
    val margin: Dp = 20.dp

    /** 카드 · 그리드 사이 */
    val gutter: Dp = 16.dp
    val gutterCompact: Dp = 12.dp

    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 14.dp
    val lg: Dp = 20.dp
    val xl: Dp = 28.dp
    val xxl: Dp = 40.dp
}
