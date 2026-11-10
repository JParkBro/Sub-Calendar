package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppFixedColors
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

private const val FULL_CIRCLE = 360f
private const val START_ANGLE = -90f
private const val SLICE_GAP_DEGREES = 2f
private const val MIN_SLICE_DEGREES = 0.5f

/** 막대 폭: 칸 너비의 비율 */
private const val BAR_WIDTH_RATIO = 0.6f

/** 막대 모서리 반지름: 막대 폭의 비율 */
private const val BAR_CORNER_RATIO = 0.25f

/** 도넛 한 조각: [value]는 비율 계산용 양수 */
data class SubCalChartSlice(
    val value: Float,
    val color: Color,
)

/** 막대 하나: [label]은 막대 아래, [highlighted]면 primary (예: 이번 달) */
data class SubCalChartBar(
    val value: Float,
    val label: String,
    val highlighted: Boolean = false,
)

/**
 * 차트 색 (theme-guide 5장 "차트 색"): 큰 순서대로 primary → accent → BLUE → AMBER → PURPLE → PINK → TEAL.
 * 기타는 [chartOtherColor]. 색만으로 구분하지 않도록 범례에 이름 · 비율을 함께 쓴다.
 */
@Composable
fun chartSeriesColor(index: Int): Color {
    val colors = AppThemeTokens.colors
    val series =
        listOf(
            colors.primary,
            colors.accent,
            AppFixedColors.service("BLUE"),
            AppFixedColors.service("AMBER"),
            AppFixedColors.service("PURPLE"),
            AppFixedColors.service("PINK"),
            AppFixedColors.service("TEAL"),
        )
    return series[index % series.size]
}

/** 차트의 "기타" 색 */
@Composable
fun chartOtherColor(): Color = AppThemeTokens.colors.textTertiary

/** 도넛 차트 (Canvas). 가운데에 [center]를 그린다. [description]은 화면 낭독기용 요약 */
@Composable
fun SubCalDonutChart(
    slices: List<SubCalChartSlice>,
    description: String,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    thickness: Dp = 22.dp,
    center: @Composable () -> Unit = {},
) {
    val track = AppThemeTokens.colors.fill
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Box(
        modifier = modifier.size(size).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = thickness.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)
            if (total <= 0f) {
                drawArc(track, 0f, FULL_CIRCLE, false, topLeft, arcSize, style = Stroke(stroke))
                return@Canvas
            }
            // 조각이 하나뿐이면 틈 없이 원 전체를 그린다
            val gap = if (slices.count { it.value > 0f } > 1) SLICE_GAP_DEGREES else 0f
            var start = START_ANGLE
            slices.filter { it.value > 0f }.forEach { slice ->
                val sweep = slice.value / total * FULL_CIRCLE
                drawArc(
                    color = slice.color,
                    startAngle = start + gap / 2,
                    sweepAngle = (sweep - gap).coerceAtLeast(MIN_SLICE_DEGREES),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke),
                )
                start += sweep
            }
        }
        center()
    }
}

/** 막대 차트 (Canvas) + 아래 라벨. 강조 막대는 primary, 나머지는 fill */
@Composable
fun SubCalBarChart(
    bars: List<SubCalChartBar>,
    description: String,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
) {
    val colors = AppThemeTokens.colors
    val max = bars.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f
    Column(
        modifier = modifier.fillMaxWidth().semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            if (bars.isEmpty()) return@Canvas
            val slot = size.width / bars.size
            val barWidth = slot * BAR_WIDTH_RATIO
            val radius = CornerRadius(barWidth * BAR_CORNER_RATIO, barWidth * BAR_CORNER_RATIO)
            bars.forEachIndexed { index, bar ->
                val barHeight = (bar.value / max * size.height).coerceAtLeast(if (bar.value > 0f) 2.dp.toPx() else 0f)
                drawRoundRect(
                    color = if (bar.highlighted) colors.primary else colors.fill,
                    topLeft = Offset(slot * index + (slot - barWidth) / 2, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = radius,
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    style = AppThemeTokens.typography.labelSm,
                    color = if (bar.highlighted) colors.primary else colors.textTertiary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun SubCalChartsPreview() {
    SubCalPreviewSurface {
        SubCalDonutChart(
            slices = (0..3).map { SubCalChartSlice(value = (4 - it).toFloat(), color = chartSeriesColor(it)) },
            description = "",
        ) { Text("4개", color = AppThemeTokens.colors.textPrimary) }
        SubCalBarChart(
            bars =
                (1..12).map {
                    SubCalChartBar(
                        value = (it % 5 + 3).toFloat(),
                        label = "$it",
                        highlighted = it == 12,
                    )
                },
            description = "",
        )
    }
}
