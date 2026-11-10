package jpark.bro.subcal.navigation

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.calendar.api.CalendarKey
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingResult
import jpark.bro.subcal.feature.settings.api.DataReset
import jpark.bro.subcal.feature.settings.api.MoreKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
import jpark.bro.subcal.feature.trial.api.TrialKey
import org.junit.Assert.assertEquals
import org.junit.Test

class AppResultHandlersTest {
    @Test
    fun `온보딩을 마치지 않았으면 온보딩부터`() {
        assertEquals(OnboardingKey, startKeyFor(onboardingDone = false))
        assertEquals(HomeKey, startKeyFor(onboardingDone = true))
    }

    @Test
    fun `온보딩이 끝나면 온보딩을 지우고 홈만 남긴다`() {
        val navigator = Navigator(startKey = OnboardingKey, topLevelKeys = TopLevelDestination.keys)

        AppOnboardingResultHandler(navigator).handle(OnboardingResult.Done)

        assertEquals(listOf(HomeKey), navigator.backStack.toList())
        assertEquals(HomeKey, navigator.currentTab)
    }

    @Test
    fun `백업을 불러오면 다른 탭에 있어도 홈만 남긴다`() {
        val navigator = Navigator(startKey = HomeKey, topLevelKeys = TopLevelDestination.keys)
        navigator.goTo(MoreKey)

        AppDataResetHandler(navigator).handle(DataReset.Restored)

        assertEquals(listOf(HomeKey), navigator.backStack.toList())
        assertEquals(HomeKey, navigator.currentTab)
    }

    @Test
    fun `모든 데이터를 지우면 온보딩만 남긴다`() {
        val navigator = Navigator(startKey = HomeKey, topLevelKeys = TopLevelDestination.keys)
        navigator.goTo(MoreKey)

        AppDataResetHandler(navigator).handle(DataReset.Deleted)

        assertEquals(listOf(OnboardingKey), navigator.backStack.toList())
    }

    @Test
    fun `알림에서 구독을 열면 다른 탭에 있어도 홈 위에 상세를 쌓는다`() {
        val navigator = Navigator(startKey = HomeKey, topLevelKeys = TopLevelDestination.keys)
        navigator.goTo(CalendarKey)

        navigator.open(AppOpenRequest.Subscription("netflix"))

        assertEquals(listOf(HomeKey, SubscriptionDetailKey("netflix")), navigator.backStack.toList())
        assertEquals(HomeKey, navigator.currentTab)
    }

    @Test
    fun `체험 알림을 누르면 홈 위에 무료체험을 쌓는다`() {
        val navigator = Navigator(startKey = HomeKey, topLevelKeys = TopLevelDestination.keys)
        navigator.goTo(MoreKey)

        navigator.open(AppOpenRequest.Trial)

        assertEquals(listOf(HomeKey, TrialKey), navigator.backStack.toList())
    }

    @Test
    fun `온보딩 중에는 알림으로 구독을 열지 않는다`() {
        val navigator = Navigator(startKey = OnboardingKey, topLevelKeys = TopLevelDestination.keys)

        navigator.open(AppOpenRequest.Subscription("netflix"))

        assertEquals(listOf(OnboardingKey), navigator.backStack.toList())
    }
}
