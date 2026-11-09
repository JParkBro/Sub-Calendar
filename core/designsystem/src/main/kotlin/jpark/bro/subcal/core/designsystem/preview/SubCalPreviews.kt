package jpark.bro.subcal.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens

/** 기본 핸드폰 크기 (세로) */
private const val PHONE = "spec:width=411dp,height=891dp"

/** 기본 핸드폰 크기 (가로) */
private const val PHONE_LANDSCAPE = "spec:width=411dp,height=891dp,orientation=landscape"

private const val NIGHT = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL

/** 글꼴 크기 최대 설정 (접근성) */
private const val LARGE_FONT = 2f

/**
 * 공통 컴포넌트용 Preview: 라이트 · 다크 · 글꼴 2배.
 * 컴포넌트는 가로 · 세로 차이가 없으므로 가로는 [ScreenPreviews]에만 둔다.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = NIGHT)
@Preview(name = "Font x2", fontScale = LARGE_FONT)
annotation class ComponentPreviews

/** 화면용 Preview: 핸드폰 라이트 · 다크 · 글꼴 2배 · 가로 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(name = "Light", device = PHONE)
@Preview(name = "Dark", device = PHONE, uiMode = NIGHT)
@Preview(name = "Font x2", device = PHONE, fontScale = LARGE_FONT)
@Preview(name = "Landscape", device = PHONE_LANDSCAPE)
annotation class ScreenPreviews

/** 컴포넌트 Preview 바탕: 테마 + bg 배경 + 견본 사이 간격 */
@Composable
fun SubCalPreviewSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppTheme {
        Column(
            modifier =
                modifier
                    .background(AppThemeTokens.colors.bg)
                    .padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            content = content,
        )
    }
}
