package jpark.bro.subcal.navigation

import jpark.bro.subcal.core.navigation.Navigator
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingKey
import jpark.bro.subcal.feature.onboarding.api.OnboardingResult
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
}
