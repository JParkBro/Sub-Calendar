package jpark.bro.subcal.navigation

import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingResult
import jpark.bro.subcal.feature.onboarding.api.OnboardingResultHandler

/** 앱 시작 화면: 온보딩을 마치지 않았으면 온보딩 (module-map 4-3) */
internal fun startKeyFor(onboardingDone: Boolean): NavKey = if (onboardingDone) HomeKey else OnboardingKey

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
