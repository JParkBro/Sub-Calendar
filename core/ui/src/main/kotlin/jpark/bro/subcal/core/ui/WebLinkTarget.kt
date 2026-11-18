package jpark.bro.subcal.core.ui

/** 웹뷰 안에서 누른 링크를 어디서 열지 (`SubCalWebView`) */
enum class WebLinkTarget {
    /** 처음 연 페이지와 같은 사이트: 웹뷰 안에서 연다 */
    IN_PAGE,

    /** Play 스토어: 화면이 스토어 앱으로 넘긴다 */
    PLAY_STORE,

    /** http, 다른 사이트, `intent:`, `javascript:` 등: 열지 않는다 */
    BLOCKED,
}

/** [pageHost]는 처음 연 URL의 host */
fun webLinkTarget(
    pageHost: String,
    linkScheme: String?,
    linkHost: String?,
): WebLinkTarget =
    when {
        linkScheme == "https" && linkHost == pageHost -> WebLinkTarget.IN_PAGE
        linkScheme == "https" && linkHost == "play.google.com" -> WebLinkTarget.PLAY_STORE
        linkScheme == "market" -> WebLinkTarget.PLAY_STORE
        else -> WebLinkTarget.BLOCKED
    }
