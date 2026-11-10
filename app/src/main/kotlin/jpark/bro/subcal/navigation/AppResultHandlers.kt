package jpark.bro.subcal.navigation

import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingResult
import jpark.bro.subcal.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.subcal.feature.settings.api.DataReset
import jpark.bro.subcal.feature.settings.api.DataResetHandler
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import jpark.bro.subcal.feature.trial.api.TrialKey

/** 앱 시작 화면: 온보딩을 마치지 않았으면 온보딩 (module-map 4-3) */
internal fun startKeyFor(onboardingDone: Boolean): NavKey = if (onboardingDone) HomeKey else OnboardingKey

/** 알림 · 위젯이 열어 달라는 화면 (AppIntents) */
internal sealed interface AppOpenRequest {
    data class Subscription(
        val id: String,
    ) : AppOpenRequest

    data object Trial : AppOpenRequest
}

/**
 * 알림 · 위젯에서 화면을 열 때: 뒤로 가면 홈이 나오도록 [홈, 그 화면]으로 만든다 (module-map 4-3).
 * 온보딩을 마치기 전이면 무시한다.
 */
internal fun Navigator.open(request: AppOpenRequest) {
    if (backStack.firstOrNull() == OnboardingKey) return
    replaceAll(HomeKey)
    goTo(
        when (request) {
            is AppOpenRequest.Subscription -> SubscriptionDetailKey(request.id)
            AppOpenRequest.Trial -> TrialKey
        },
    )
}

/** 백업을 불러오면 홈, 모든 데이터를 지우면 온보딩으로. 이전 화면은 남기지 않는다 (module-map 4-3) */
internal class AppDataResetHandler(
    private val navigator: Navigator,
) : DataResetHandler {
    override fun handle(result: DataReset) {
        when (result) {
            DataReset.Restored -> navigator.replaceAll(HomeKey)
            DataReset.Deleted -> navigator.replaceAll(OnboardingKey)
        }
    }
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
