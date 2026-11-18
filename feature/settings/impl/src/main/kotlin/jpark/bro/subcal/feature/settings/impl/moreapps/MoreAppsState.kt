package jpark.bro.subcal.feature.settings.impl.moreapps

/** 다른 앱 둘러보기 (product-spec 6장): 개발자의 다른 앱 소개 페이지를 웹뷰로 연다 */
internal data class MoreAppsState(
    val isLoading: Boolean = true,
    val isFailed: Boolean = false,
    /** 다시 시도할 때마다 1씩 올려 웹뷰가 같은 URL을 다시 불러오게 한다 */
    val reloadCount: Int = 0,
)
