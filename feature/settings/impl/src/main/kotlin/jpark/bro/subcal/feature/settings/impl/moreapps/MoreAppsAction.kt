package jpark.bro.subcal.feature.settings.impl.moreapps

internal sealed interface MoreAppsAction {
    /** 메인 프레임을 오류 없이 불러왔다 */
    data object PageLoaded : MoreAppsAction

    /** 메인 프레임 네트워크 · HTTP 오류 */
    data object PageFailed : MoreAppsAction

    data object RetryClicked : MoreAppsAction

    /** 페이지에서 Play 스토어 링크를 눌렀다 */
    data class StoreLinkClicked(
        val url: String,
    ) : MoreAppsAction

    data object BackClicked : MoreAppsAction
}
