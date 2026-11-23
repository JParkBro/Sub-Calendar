package jpark.bro.subcal.feature.settings.impl.moreapps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalMascotLoading
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.SubCalWebView
import jpark.bro.subcal.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.subcal.core.designsystem.R as DesignR

private const val PLAY_STORE_PACKAGE = "com.android.vending"

@Composable
internal fun MoreAppsRoot(
    onBack: () -> Unit,
    viewModel: MoreAppsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val openFailed = stringResource(R.string.settings_more_open_failed)
    val isDark = AppThemeTokens.isDark
    // 테마가 바뀌면 페이지도 그 테마로 다시 연다
    val url = remember(isDark) { moreAppsUrl(context.packageName, isDark) }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is MoreAppsEvent.OpenStore -> {
                if (!context.openStore(event.url)) toastHostState.showSnackbar(openFailed)
            }

            MoreAppsEvent.NavigateBack -> {
                onBack()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        MoreAppsScreen(
            state = state,
            onAction = viewModel::onAction,
            page = { modifier ->
                SubCalWebView(
                    url = url,
                    onPageLoaded = { viewModel.onAction(MoreAppsAction.PageLoaded) },
                    onPageFailed = { viewModel.onAction(MoreAppsAction.PageFailed) },
                    onStoreLink = { viewModel.onAction(MoreAppsAction.StoreLinkClicked(it)) },
                    reloadKey = state.reloadCount,
                    modifier = modifier,
                )
            },
        )
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/**
 * 상단 바 + 소개 페이지. 웹뷰는 [page] 슬롯으로 받고(Preview는 비운다),
 * 로딩 · 실패 중에도 컴포지션에 남겨 다시 시도할 때 같은 인스턴스로 다시 불러온다.
 */
@Composable
private fun MoreAppsScreen(
    state: MoreAppsState,
    onAction: (MoreAppsAction) -> Unit,
    page: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("more_apps"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.settings_more_apps_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_more_apps_back),
                    onClick = { onAction(MoreAppsAction.BackClicked) },
                )
            },
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val isPageVisible = !state.isLoading && !state.isFailed
            page(
                Modifier
                    .fillMaxSize()
                    .alpha(if (isPageVisible) 1f else 0f)
                    .testTag("more_apps_page"),
            )
            when {
                state.isFailed -> {
                    FailedContent(
                        onRetry = { onAction(MoreAppsAction.RetryClicked) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.isLoading -> {
                    SubCalMascotLoading(
                        contentDescription = stringResource(R.string.settings_more_apps_loading),
                        modifier = Modifier.align(Alignment.Center).testTag("more_apps_loading"),
                    )
                }
            }
        }
    }
}

@Composable
private fun FailedContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = AppSpacing.marginMobile),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.settings_more_apps_failed),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        SubCalSecondaryButton(
            text = stringResource(R.string.settings_more_apps_retry),
            onClick = onRetry,
            size = SubCalButtonSize.SMALL,
            modifier = Modifier.testTag("more_apps_retry"),
        )
    }
}

/** Play 스토어 앱으로 열고, 없으면 패키지 없이 다시 열어 브라우저로 넘긴다. 둘 다 못 열면 false */
private fun Context.openStore(url: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    return tryStartActivity(Intent(intent).setPackage(PLAY_STORE_PACKAGE)) || tryStartActivity(intent)
}

private fun Context.tryStartActivity(intent: Intent): Boolean =
    try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

@ScreenPreviews
@Composable
private fun MoreAppsLoadingPreview() {
    AppTheme { MoreAppsScreen(state = MoreAppsState(), onAction = {}, page = {}) }
}

@ScreenPreviews
@Composable
private fun MoreAppsFailedPreview() {
    AppTheme {
        MoreAppsScreen(state = MoreAppsState(isLoading = false, isFailed = true), onAction = {}, page = {})
    }
}
