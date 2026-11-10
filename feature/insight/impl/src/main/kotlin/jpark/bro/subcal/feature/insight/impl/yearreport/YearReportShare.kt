package jpark.bro.subcal.feature.insight.impl.yearreport

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.feature.insight.impl.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** 공유 이미지 크기: 인스타그램 스토리 비율 9:16 (product-spec 4-11) */
private const val SHARE_WIDTH_PX = 1080
private const val SHARE_HEIGHT_PX = 1920

/** 공유 이미지는 기기 밀도 · 글꼴 크기와 상관없이 360dp 폭으로 그린다 */
private const val SHARE_WIDTH_DP = 360f

private const val PNG_QUALITY = 100

/** 캐시 폴더 안 공유 파일 위치. `res/xml/insight_share_paths.xml`과 같다 */
private const val SHARE_DIR = "share"

/** `AndroidManifest.xml`의 FileProvider authority 접미사 */
private const val SHARE_AUTHORITY_SUFFIX = ".share"

/**
 * [content]를 1080×1920으로 배치해 [layer]에 기록만 하고 화면에는 그리지 않는다.
 * 부모 안에서는 크기 0이라 레이아웃에 영향이 없고, 접근성 트리에서도 뺀다.
 */
@Composable
internal fun ShareImageCapture(
    layer: GraphicsLayer,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .layout { measurable, _ ->
                    val placeable = measurable.measure(Constraints.fixed(SHARE_WIDTH_PX, SHARE_HEIGHT_PX))
                    layout(0, 0) { placeable.place(0, 0) }
                }.clearAndSetSemantics { }
                .drawWithContent { layer.record { this@drawWithContent.drawContent() } },
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(density = SHARE_WIDTH_PX / SHARE_WIDTH_DP, fontScale = 1f),
            content = content,
        )
    }
}

/** 공유 이미지 (1080×1920에 360dp 폭으로 그림): 배경 가운데에 카드 한 장 */
@Composable
internal fun YearReportShareImage(
    year: Int,
    page: YearReportPage,
    content: YearReportContent,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .padding(AppSpacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        YearReportCard(year = year, page = page, content = content)
    }
}

/** [layer]를 PNG로 캐시 폴더에 쓰고 공유 시트를 연다. 저장 권한 없이 FileProvider로 넘긴다. 실패하면 false */
internal suspend fun Context.shareYearReportImage(
    layer: GraphicsLayer,
    year: Int,
): Boolean {
    val bitmap = layer.toImageBitmap().asAndroidBitmap()
    val uri = withContext(Dispatchers.IO) { writeShareImage(bitmap, year) } ?: return false
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    startActivity(Intent.createChooser(send, getString(R.string.insight_year_report_share_chooser)))
    return true
}

private fun Context.writeShareImage(
    bitmap: Bitmap,
    year: Int,
): Uri? =
    runCatching {
        val dir = File(cacheDir, SHARE_DIR).apply { mkdirs() }
        val file = File(dir, "subcal-year-report-$year.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
        FileProvider.getUriForFile(this, packageName + SHARE_AUTHORITY_SUFFIX, file)
    }.getOrNull()
