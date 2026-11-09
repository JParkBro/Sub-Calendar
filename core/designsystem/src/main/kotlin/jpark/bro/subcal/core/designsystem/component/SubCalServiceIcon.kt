package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppFixedColors
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 해지 · 일시정지 구독 아이콘 투명도 (theme-guide.md 6장 6번) */
private const val DIMMED_ALPHA = 0.5f

/** 아이콘 크기 대비 이니셜 크기 */
private const val INITIALS_SCALE = 0.4f

/** 영문 이름에서 머리글자를 딸 단어 수 */
private const val MAX_LATIN_INITIALS = 2

/**
 * 서비스 아이콘: 지정 색 원 + 이니셜 (theme-guide.md 5장). 브랜드 로고 이미지를 쓰지 않는다.
 * 옆에 이름이 항상 함께 나오므로 스크린 리더에서는 읽지 않는다.
 * 이니셜은 원 안에 맞춰야 해서 글꼴 크기 설정과 무관하게 아이콘 크기를 따른다.
 */
@Composable
fun SubCalServiceIcon(
    name: String,
    colorKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    dimmed: Boolean = false,
) {
    val fontSize = with(LocalDensity.current) { (size * INITIALS_SCALE).toSp() }
    Box(
        modifier =
            modifier
                .size(size)
                .alpha(if (dimmed) DIMMED_ALPHA else 1f)
                .clip(AppShapes.full)
                .background(AppFixedColors.service(colorKey))
                .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = serviceInitials(name),
            style = AppThemeTokens.typography.labelLg.copy(fontSize = fontSize, lineHeight = fontSize),
            fontWeight = FontWeight.Bold,
            color = AppFixedColors.onService,
            maxLines = 1,
        )
    }
}

/** 한글 등은 첫 글자 1자, 영문은 첫 두 단어의 머리글자 대문자 (theme-guide.md 5장) */
internal fun serviceInitials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val first = words.firstOrNull() ?: return ""
    return if (first[0] in 'a'..'z' || first[0] in 'A'..'Z') {
        words.take(MAX_LATIN_INITIALS).joinToString("") { it.firstCharacter().uppercase() }
    } else {
        first.firstCharacter()
    }
}

/** 이모지처럼 두 Char로 된 글자를 자르지 않는다 */
private fun String.firstCharacter(): String = substring(0, offsetByCodePoints(0, 1))

/** product-spec 9장 샘플 */
@ComponentPreviews
@Composable
private fun SubCalServiceIconPreview() {
    SubCalPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            SubCalServiceIcon(name = "넷플릭스 프리미엄", colorKey = "RED")
            SubCalServiceIcon(name = "YouTube Premium", colorKey = "PINK")
            SubCalServiceIcon(name = "Spotify", colorKey = "GREEN")
            SubCalServiceIcon(name = "왓챠", colorKey = "BLACK", dimmed = true)
        }
    }
}
