package jpark.bro.subcal.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.R
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalSheetHandle
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface

/** 업데이트 확인 시트. 어느 화면에 있든 뜨도록 MainActivity가 앱 전체 위에 둔다 */
@Composable
internal fun UpdateSheet(
    prompt: UpdatePrompt,
    onAction: (UpdateAction) -> Unit,
) {
    when (prompt) {
        is UpdatePrompt.UpdateAvailable -> {
            SubCalBottomSheet(onDismissRequest = { onAction(UpdateAction.UpdateDismissed) }) {
                UpdateSheetBody(
                    title = stringResource(R.string.app_update_available_title),
                    message = stringResource(R.string.app_update_available_message),
                    confirmText = stringResource(R.string.app_update_available_confirm),
                    onConfirm = { onAction(UpdateAction.UpdateConfirmed) },
                    onLater = { onAction(UpdateAction.UpdateDismissed) },
                )
            }
        }

        UpdatePrompt.ReadyToInstall -> {
            SubCalBottomSheet(onDismissRequest = { onAction(UpdateAction.InstallDismissed) }) {
                UpdateSheetBody(
                    title = stringResource(R.string.app_update_ready_title),
                    message = stringResource(R.string.app_update_ready_message),
                    confirmText = stringResource(R.string.app_update_ready_confirm),
                    onConfirm = { onAction(UpdateAction.InstallConfirmed) },
                    onLater = { onAction(UpdateAction.InstallDismissed) },
                )
            }
        }

        UpdatePrompt.None -> {
            Unit
        }
    }
}

@Composable
private fun UpdateSheetBody(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onLater: () -> Unit,
) {
    Column(
        modifier =
            Modifier.padding(
                start = AppSpacing.marginMobile,
                end = AppSpacing.marginMobile,
                bottom = AppSpacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(text = title, style = AppThemeTokens.typography.headlineSm, color = AppThemeTokens.colors.textPrimary)
        Text(text = message, style = AppThemeTokens.typography.bodySm, color = AppThemeTokens.colors.textSecondary)
        SubCalPrimaryButton(
            text = confirmText,
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().testTag("update_confirm"),
        )
        SubCalSecondaryButton(
            text = stringResource(R.string.app_update_later),
            onClick = onLater,
            modifier = Modifier.fillMaxWidth().testTag("update_later"),
        )
    }
}

/** ModalBottomSheet는 Preview에서 그려지지 않으므로 시트 안 내용만 보여준다 */
@ComponentPreviews
@Composable
private fun UpdateSheetBodyPreview() {
    SubCalPreviewSurface {
        Column(
            modifier =
                Modifier
                    .clip(AppShapes.xl)
                    .background(AppThemeTokens.colors.surfaceRaised),
        ) {
            SubCalSheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
            UpdateSheetBody(
                title = stringResource(R.string.app_update_ready_title),
                message = stringResource(R.string.app_update_ready_message),
                confirmText = stringResource(R.string.app_update_ready_confirm),
                onConfirm = {},
                onLater = {},
            )
        }
    }
}
