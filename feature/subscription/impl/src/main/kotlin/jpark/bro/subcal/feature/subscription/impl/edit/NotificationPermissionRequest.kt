package jpark.bro.subcal.feature.subscription.impl.edit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * 결제 알림 권한을 묻는 함수를 돌려준다 (product-spec 5장).
 * Android 13 이상이고 권한이 없을 때만 시스템 창을 띄우고, 답과 상관없이 [onDone]을 부른다.
 * 두 번 거절하면 시스템이 창을 띄우지 않고 바로 거절로 돌려준다.
 */
@Composable
internal fun rememberNotificationPermissionRequest(onDone: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnDone by rememberUpdatedState(onDone)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { currentOnDone() }
    return remember(context, launcher) {
        {
            val needsRequest =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
            if (needsRequest) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) else currentOnDone()
        }
    }
}
