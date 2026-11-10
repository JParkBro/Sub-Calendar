package jpark.bro.subcal.navigation

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.calendar.api.CalendarKey
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingResult
import jpark.bro.subcal.feature.subscription.api.SubscriptionDetailKey
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
    fun `알림에서 구독을 열면 다른 탭에 있어도 홈 위에 상세를 쌓는다`() {
        val navigator = Navigator(startKey = HomeKey, topLevelKeys = TopLevelDestination.keys)
        navigator.goTo(CalendarKey)

        navigator.openSubscriptionDetail("netflix")

        assertEquals(listOf(HomeKey, SubscriptionDetailKey("netflix")), navigator.backStack.toList())
        assertEquals(HomeKey, navigator.currentTab)
    }

    @Test
    fun `온보딩 중에는 알림으로 구독을 열지 않는다`() {
        val navigator = Navigator(startKey = OnboardingKey, topLevelKeys = TopLevelDestination.keys)

        navigator.openSubscriptionDetail("netflix")

        assertEquals(listOf(OnboardingKey), navigator.backStack.toList())
    }
}
