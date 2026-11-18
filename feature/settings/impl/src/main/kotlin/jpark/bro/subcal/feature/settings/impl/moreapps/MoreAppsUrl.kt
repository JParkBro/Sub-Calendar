package jpark.bro.subcal.feature.settings.impl.moreapps

import java.net.URLEncoder

/** 개발자의 다른 앱 소개 페이지 (데이즈와 같은 페이지, 소스는 이 저장소 밖) */
private const val MORE_APPS_URL = "https://jparkbro-apps.jparkbro.workers.dev/"

/** dev flavor의 applicationIdSuffix (build-logic Flavors.kt `AppFlavor.Dev`) */
private const val DEV_APPLICATION_ID_SUFFIX = ".dev"

/**
 * 소개 페이지 주소. 앱 데이터는 보내지 않고 두 값만 붙인다.
 * - `from`: 출시 패키지 이름. 페이지가 목록에서 이 앱을 빼고 유입 경로로 쓴다
 * - `theme`: `light` · `dark`
 */
internal fun moreAppsUrl(
    packageName: String,
    isDark: Boolean,
): String {
    val from = URLEncoder.encode(packageName.removeSuffix(DEV_APPLICATION_ID_SUFFIX), Charsets.UTF_8.name())
    val theme = if (isDark) "dark" else "light"
    return "$MORE_APPS_URL?from=$from&theme=$theme"
}
