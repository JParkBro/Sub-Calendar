package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppColors
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 뱃지 종류 (theme-guide.md 7장). 색만으로 구분하지 않도록 항상 문구와 함께 쓴다 */
enum class SubCalBadgeTone {
    /** 매년 · 3개월마다: accent-soft */
    CYCLE,

    /** 카테고리: accent-soft */
    CATEGORY,

    /** 임박: danger-soft */
    SOON,

    /** 이용 중 · 받음: success-soft */
    SUCCESS,

    /** 일시정지: fill */
    PAUSED,

    /** 해지: fill */
    CANCELLED,
}

enum class SubCalBadgeSize {
    /** 리스트 · 카드 안 (labelSm) */
    SMALL,

    /** 설정 행 끝 상태 표시처럼 조금 큰 것 (labelMd) */
    MEDIUM,
}

/** 알약 모양 뱃지. [showDot]이면 앞에 상태 점을 붙인다 */
@Composable
fun SubCalBadge(
    text: String,
    tone: SubCalBadgeTone,
    modifier: Modifier = Modifier,
    size: SubCalBadgeSize = SubCalBadgeSize.SMALL,
    showDot: Boolean = false,
) {
    val colors = AppThemeTokens.colors.badgeColors(tone)
    val typography = AppThemeTokens.typography
    Row(
        modifier =
            modifier
                .clip(AppShapes.full)
                .background(colors.background)
                .then(
                    when (size) {
                        SubCalBadgeSize.SMALL -> Modifier.padding(horizontal = AppSpacing.sm, vertical = 2.dp)
                        SubCalBadgeSize.MEDIUM -> Modifier.padding(horizontal = 12.dp, vertical = AppSpacing.xs)
                    },
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (showDot) {
            Box(
                modifier =
                    Modifier
                        .size(if (size == SubCalBadgeSize.SMALL) 6.dp else 8.dp)
                        .clip(AppShapes.full)
                        .background(colors.dot),
            )
        }
        Text(
            text = text,
            style = if (size == SubCalBadgeSize.SMALL) typography.labelSm else typography.labelMd,
            color = colors.content,
        )
    }
}

private data class BadgeColors(
    val background: Color,
    val content: Color,
    val dot: Color,
)

private fun AppColors.badgeColors(tone: SubCalBadgeTone): BadgeColors =
    when (tone) {
        SubCalBadgeTone.CYCLE,
        SubCalBadgeTone.CATEGORY,
        -> BadgeColors(accentSoft, onAccentSoft, accent)

        SubCalBadgeTone.SOON -> BadgeColors(dangerSoft, danger, danger)

        SubCalBadgeTone.SUCCESS -> BadgeColors(successSoft, success, success)

        SubCalBadgeTone.PAUSED -> BadgeColors(fill, textSecondary, warning)

        SubCalBadgeTone.CANCELLED -> BadgeColors(fill, textSecondary, textTertiary)
    }

@ComponentPreviews
@Composable
private fun SubCalBadgePreview() {
    SubCalPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            SubCalBadge(text = "매년", tone = SubCalBadgeTone.CYCLE)
            SubCalBadge(text = "영상", tone = SubCalBadgeTone.CATEGORY)
            SubCalBadge(text = "임박", tone = SubCalBadgeTone.SOON)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            SubCalBadge(text = "이용 중", tone = SubCalBadgeTone.SUCCESS, showDot = true)
            SubCalBadge(text = "일시정지", tone = SubCalBadgeTone.PAUSED, showDot = true)
            SubCalBadge(text = "해지", tone = SubCalBadgeTone.CANCELLED, showDot = true)
        }
        SubCalBadge(text = "받음", tone = SubCalBadgeTone.SUCCESS, size = SubCalBadgeSize.MEDIUM)
    }
}
