package jpark.bro.subcal.feature.onboarding.api

sealed interface OnboardingResult {
    /** 등록하고 시작 · 건너뛰기 → 홈 */
    data object Done : OnboardingResult
}

/** 구현은 app에 둔다 */
fun interface OnboardingResultHandler {
    fun handle(result: OnboardingResult)
}
