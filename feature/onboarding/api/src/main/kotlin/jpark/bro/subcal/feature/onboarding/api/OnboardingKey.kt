package jpark.bro.subcal.feature.onboarding.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 첫 실행 서비스 선택. 끝나면 [OnboardingResult]를 알리고 app이 목적지를 정한다 (module-map 4-3) */
@Serializable
data object OnboardingKey : NavKey
