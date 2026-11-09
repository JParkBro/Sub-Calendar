package jpark.bro.subcal.feature.subscription.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar

/** 앱 뼈대용 빈 탭 화면. 실제 화면은 다음 PR에서 만든다 */
@Composable
internal fun SubscriptionListRoot(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(AppThemeTokens.colors.bg)) {
        SubCalTopBar(title = stringResource(R.string.subscription_list_title))
    }
}
