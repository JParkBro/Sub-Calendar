package jpark.bro.subcal.core.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import jpark.bro.subcal.core.designsystem.AppThemeTokens

/**
 * 웹 페이지 한 장을 보여주는 웹뷰 (정책 문서 · 다른 앱 둘러보기, theme-guide 7장).
 *
 * - 링크는 [webLinkTarget]으로 고른다: 같은 사이트 https만 안에서 열고, Play 스토어 링크는 [onStoreLink]로 넘기고, 나머지는 막는다.
 * - 메인 프레임 오류(네트워크 · HTTP)는 [onPageFailed], 오류 없이 끝나면 [onPageLoaded]. 하위 리소스 오류는 무시한다.
 * - [url]이나 [reloadKey]가 바뀌면 같은 웹뷰로 다시 불러온다.
 */
@Composable
fun SubCalWebView(
    url: String,
    onPageLoaded: () -> Unit,
    onPageFailed: () -> Unit,
    onStoreLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    reloadKey: Int = 0,
) {
    val currentOnPageLoaded by rememberUpdatedState(onPageLoaded)
    val currentOnPageFailed by rememberUpdatedState(onPageFailed)
    val currentOnStoreLink by rememberUpdatedState(onStoreLink)
    val pageHost by rememberUpdatedState(remember(url) { Uri.parse(url).host.orEmpty() })
    val client =
        remember {
            PageClient(
                pageHost = { pageHost },
                onLoaded = { currentOnPageLoaded() },
                onFailed = { currentOnPageFailed() },
                onStoreLink = { currentOnStoreLink(it) },
            )
        }
    // 페이지가 그려지기 전 흰 화면이 번쩍이지 않게 앱 배경으로 칠한다
    val background = AppThemeTokens.colors.bg.toArgb()

    AndroidView(
        factory = { context -> WebView(context).apply { configure(client) } },
        update = { webView ->
            webView.setBackgroundColor(background)
            val request = url to reloadKey
            if (webView.tag != request) {
                webView.tag = request
                webView.loadUrl(url)
            }
        },
        onRelease = { it.destroy() },
        modifier = modifier,
    )
}

// 소개 · 정책 페이지가 목록을 fetch로 그린다. 같은 사이트 https만 열고(webLinkTarget), 앱 객체는 넘기지 않는다
@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configure(client: WebViewClient) {
    settings.javaScriptEnabled = true
    settings.allowFileAccess = false
    settings.allowContentAccess = false
    webViewClient = client
}

private class PageClient(
    private val pageHost: () -> String,
    private val onLoaded: () -> Unit,
    private val onFailed: () -> Unit,
    private val onStoreLink: (String) -> Unit,
) : WebViewClient() {
    /** 오류 뒤에 onPageFinished가 와도 성공으로 보지 않는다 */
    private var hasError = false

    override fun onPageStarted(
        view: WebView,
        url: String?,
        favicon: Bitmap?,
    ) {
        hasError = false
    }

    override fun onPageFinished(
        view: WebView,
        url: String?,
    ) {
        if (!hasError) onLoaded()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        failMainFrame(request)
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse,
    ) {
        failMainFrame(request)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        val link = request.url
        return when (webLinkTarget(pageHost(), link.scheme, link.host)) {
            WebLinkTarget.IN_PAGE -> {
                false
            }

            WebLinkTarget.PLAY_STORE -> {
                onStoreLink(link.toString())
                true
            }

            WebLinkTarget.BLOCKED -> {
                true
            }
        }
    }

    private fun failMainFrame(request: WebResourceRequest) {
        if (!request.isForMainFrame) return
        hasError = true
        onFailed()
    }
}
