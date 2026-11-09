package jpark.bro.subcal.feature.onboarding.impl

internal sealed interface OnboardingEvent {
    /** 온보딩을 마쳤다. 목적지는 app이 정한다 */
    data object Finished : OnboardingEvent

    data object SaveFailed : OnboardingEvent
}
