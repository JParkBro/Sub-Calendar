package jpark.bro.subcal.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.R
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 로딩 애니메이션 프레임 수 · 한 프레임 시간 (통통 튀기 시트: 기본 → 눌림) */
private const val LOADING_FRAMES = 2
private const val LOADING_FRAME_MILLIS = 300

/** 대기 애니메이션 프레임 수 · 한 프레임 시간 (숨쉬기 시트: 8프레임 · 8fps, 한 바퀴 1초) */
private const val IDLE_FRAMES = 8
private const val IDLE_FRAME_MILLIS = 125

/**
 * 마스코트 정지 이미지. [size] 안에 들어가는 가장 큰 **정수 배율**로, 보간 없이 그려 픽셀이 번지지 않는다.
 * 꾸밈용이면 [contentDescription]에 null을 넘겨 스크린 리더가 건너뛰게 한다.
 */
@Composable
fun SubCalMascot(
    pose: SubCalMascotPose,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    MascotSprite(
        sheet = ImageBitmap.imageResource(pose.drawable),
        frameCount = 1,
        frame = 0,
        modifier = modifier.size(size).mascotSemantics(contentDescription, loading = false),
    )
}

/** 마스코트가 통통 튀는 로딩 표시. 진행률을 모르는 기다림에 쓴다 */
@Composable
fun SubCalMascotLoading(
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
) {
    MascotSprite(
        sheet = ImageBitmap.imageResource(R.drawable.mascot_bounce_sheet_80x80_2f),
        frameCount = LOADING_FRAMES,
        frame = rememberLoopFrame(LOADING_FRAMES, LOADING_FRAME_MILLIS),
        modifier = modifier.size(size).mascotSemantics(contentDescription, loading = true),
    )
}

/**
 * 마스코트가 숨쉬며 눈을 깜빡이는 대기 애니메이션. 빈 화면처럼 사용자를 기다리는 자리에 쓴다.
 * 로딩(기다리게 하는 쪽)에는 [SubCalMascotLoading]을 쓴다. 꾸밈용이면 [contentDescription]에 null.
 */
@Composable
fun SubCalMascotIdle(
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    MascotSprite(
        sheet = ImageBitmap.imageResource(R.drawable.mascot_breath_sheet_80x80_8f),
        frameCount = IDLE_FRAMES,
        frame = rememberLoopFrame(IDLE_FRAMES, IDLE_FRAME_MILLIS),
        modifier = modifier.size(size).mascotSemantics(contentDescription, loading = false),
    )
}

/** 0 → [frameCount] - 1을 [frameMillis]마다 넘기며 끝없이 반복 */
@Composable
private fun rememberLoopFrame(
    frameCount: Int,
    frameMillis: Int,
): Int {
    val frame by rememberInfiniteTransition(label = "mascot").animateValue(
        initialValue = 0,
        targetValue = frameCount,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(tween(frameCount * frameMillis, easing = LinearEasing)),
        label = "frame",
    )
    return frame % frameCount
}

/** [availablePx] 안에 [spritePx] 그림이 들어가는 가장 큰 정수 배율. 작아도 1배는 그린다 */
internal fun mascotPixelScale(
    availablePx: Int,
    spritePx: Int,
): Int = (availablePx / spritePx).coerceAtLeast(1)

private fun Modifier.mascotSemantics(
    description: String?,
    loading: Boolean,
): Modifier =
    clearAndSetSemantics {
        if (description != null) contentDescription = description
        if (loading) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }

/** 가로로 프레임이 붙은 [sheet]에서 [frame]번째를 가운데에 그린다 */
@Composable
private fun MascotSprite(
    sheet: ImageBitmap,
    frameCount: Int,
    frame: Int,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier.drawBehind {
                val frameWidth = sheet.width / frameCount
                val scale = mascotPixelScale(minOf(size.width, size.height).toInt(), maxOf(frameWidth, sheet.height))
                val dst = IntSize(frameWidth * scale, sheet.height * scale)
                drawImage(
                    image = sheet,
                    srcOffset = IntOffset(frame * frameWidth, 0),
                    srcSize = IntSize(frameWidth, sheet.height),
                    dstOffset =
                        IntOffset(
                            ((size.width - dst.width) / 2).toInt(),
                            ((size.height - dst.height) / 2).toInt(),
                        ),
                    dstSize = dst,
                    filterQuality = FilterQuality.None,
                )
            },
    )
}

@ComponentPreviews
@Composable
private fun SubCalMascotPreview() {
    SubCalPreviewSurface {
        Row {
            SubCalMascot(pose = SubCalMascotPose.WAVE, contentDescription = null)
            SubCalMascot(pose = SubCalMascotPose.FACE_SMILE, contentDescription = null, size = 80.dp)
        }
        Row {
            SubCalMascotIdle(contentDescription = null, size = 80.dp)
            SubCalMascotLoading(contentDescription = "불러오는 중")
        }
    }
}
