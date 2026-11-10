package jpark.bro.subcal.navigation

import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingResult
import jpark.bro.subcal.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey

/** 앱 시작 화면: 온보딩을 마치지 않았으면 온보딩 (module-map 4-3) */
internal fun startKeyFor(onboardingDone: Boolean): NavKey = if (onboardingDone) HomeKey else OnboardingKey

/**
 * 알림 · 위젯에서 구독을 열 때: 뒤로 가면 홈이 나오도록 [홈, 구독 상세]로 만든다 (module-map 4-3).
 * 온보딩을 마치기 전이면 무시한다.
 */
internal fun Navigator.openSubscriptionDetail(id: String) {
    if (backStack.firstOrNull() == OnboardingKey) return
    replaceAll(HomeKey)
    goTo(SubscriptionDetailKey(id))
}

/** 온보딩이 끝나면 이전 화면 없이 홈으로 */
internal class AppOnboardingResultHandler(
    private val navigator: Navigator,
) : OnboardingResultHandler {
    override fun handle(result: OnboardingResult) {
        when (result) {
            OnboardingResult.Done -> navigator.replaceAll(HomeKey)
        }
    }
}
