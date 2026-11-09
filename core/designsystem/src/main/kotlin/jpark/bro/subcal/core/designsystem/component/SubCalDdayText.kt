package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.R
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface
import kotlin.math.absoluteValue

/** 오늘부터 이 날수까지는 임박 (theme-guide.md 6장 3번) */
private const val SOON_DAYS = 3

internal enum class DdayTone {
    DEFAULT,
    SOON,
    PASSED,
}

internal fun ddayTone(daysUntil: Int): DdayTone =
    when {
        daysUntil < 0 -> DdayTone.PASSED
        daysUntil <= SOON_DAYS -> DdayTone.SOON
        else -> DdayTone.DEFAULT
    }

/** 오늘 · 내일 · D-n (product-spec 2-12). 지난 결제는 D+n. 숫자 폭이 흔들리지 않게 tabular-nums를 켠다 */
@Composable
fun SubCalDdayText(
    daysUntil: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = AppThemeTokens.typography.metricTabular,
) {
    val colors = AppThemeTokens.colors
    Text(
        text = ddayLabel(daysUntil),
        style = style.copy(fontFeatureSettings = "tnum"),
        color =
            when (ddayTone(daysUntil)) {
                DdayTone.DEFAULT -> colors.textPrimary
                DdayTone.SOON -> colors.danger
                DdayTone.PASSED -> colors.textTertiary
            },
        modifier = modifier,
    )
}

/** D-day 문구 (색 없이 글자만 필요할 때) */
@Composable
fun ddayLabel(daysUntil: Int): String =
    when {
        daysUntil < 0 -> stringResource(R.string.common_dday_elapsed, daysUntil.absoluteValue)
        daysUntil == 0 -> stringResource(R.string.common_dday_today)
        daysUntil == 1 -> stringResource(R.string.common_dday_tomorrow)
        else -> stringResource(R.string.common_dday_remaining, daysUntil)
    }

@ComponentPreviews
@Composable
private fun SubCalDdayTextPreview() {
    SubCalPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            SubCalDdayText(daysUntil = 0)
            SubCalDdayText(daysUntil = 1)
            SubCalDdayText(daysUntil = 3)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            SubCalDdayText(daysUntil = 6)
            SubCalDdayText(daysUntil = -2)
        }
    }
}
