package jpark.bro.subcal.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class KoinModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `모든 Koin 모듈의 의존성이 해결됨`() {
        // Navigator(startKey, topLevelKeys)는 appModule에서 값을 직접 넣어 만든다
        val navigatorArgs = listOf(NavKey::class, Set::class)
        // 화면 ViewModel은 NavKey를 parametersOf로 받는다
        val screenKeys = listOf(SubscriptionListKey::class)
        appModule.verify(extraTypes = listOf(Context::class, SavedStateHandle::class) + navigatorArgs + screenKeys)
    }
}
