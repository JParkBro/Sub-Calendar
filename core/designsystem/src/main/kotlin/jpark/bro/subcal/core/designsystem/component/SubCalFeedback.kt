package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.R
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 안내 박스. 색만으로 구분하지 않도록 아이콘을 함께 보여준다 */
@Composable
fun SubCalInfoBox(
    text: String,
    modifier: Modifier = Modifier,
    tone: SubCalInfoBoxTone = SubCalInfoBoxTone.TIP,
) {
    val colors = AppThemeTokens.colors
    val (background, content, icon) =
        when (tone) {
            SubCalInfoBoxTone.TIP -> Triple(colors.fill, colors.textSecondary, R.drawable.ic_info)
            SubCalInfoBoxTone.WARNING -> Triple(colors.warningSoft, colors.warning, R.drawable.ic_warning)
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShapes.md)
                .background(background)
                .padding(AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = content,
            modifier =
                Modifier
                    .padding(top = 2.dp)
                    .size(16.dp),
        )
        Text(text = text, style = AppThemeTokens.typography.bodySm, color = content, modifier = Modifier.weight(1f))
    }
}

/** 토스트: text-primary 배경 + bg 글자 + shadow. [actionLabel]이 있으면 오른쪽에 누를 수 있는 글자 (실행 취소 등) */
@Composable
fun SubCalToast(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            modifier
                .padding(AppSpacing.marginMobile)
                .shadow(elevation = 8.dp, shape = AppShapes.md, ambientColor = colors.shadow, spotColor = colors.shadow)
                .clip(AppShapes.md)
                .background(colors.textPrimary)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = message, style = AppThemeTokens.typography.bodyMd, color = colors.bg)
        actionLabel?.let { label ->
            Text(
                text = label,
                style = AppThemeTokens.typography.labelLg,
                color = colors.bg,
                textDecoration = TextDecoration.Underline,
                modifier =
                    Modifier
                        .clip(AppShapes.sm)
                        .clickable(role = Role.Button, onClick = onActionClick)
                        .padding(AppSpacing.xs)
                        .testTag("toast_action"),
            )
        }
    }
}

/** 스낵바 자리에 [SubCalToast]를 띄운다. 화면 Root에서 SnackbarHost 대신 쓴다. actionLabel을 주면 누를 수 있다 */
@Composable
fun SubCalToastHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        SubCalToast(
            message = data.visuals.message,
            actionLabel = data.visuals.actionLabel,
            onActionClick = data::performAction,
        )
    }
}

@ComponentPreviews
@Composable
private fun SubCalFeedbackPreview() {
    SubCalPreviewSurface {
        SubCalInfoBox(text = "위젯은 홈 화면을 길게 눌러 추가할 수 있어요.")
        SubCalInfoBox(
            text = "같은 카테고리 구독이 2개 있어요.",
            tone = SubCalInfoBoxTone.WARNING,
        )
        SubCalToast(message = "저장했어요")
        SubCalToast(message = "사용을 기록했어요", actionLabel = "실행 취소")
    }
}
