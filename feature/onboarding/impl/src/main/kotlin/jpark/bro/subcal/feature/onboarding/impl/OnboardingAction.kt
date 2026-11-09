package jpark.bro.subcal.feature.onboarding.impl

import jpark.bro.subcal.core.model.Category

internal sealed interface OnboardingAction {
    data class QueryChanged(
        val query: String,
    ) : OnboardingAction

    /** null이면 전체 */
    data class CategorySelected(
        val category: Category?,
    ) : OnboardingAction

    data class ServiceToggled(
        val presetId: String,
    ) : OnboardingAction

    /** "n개 등록하고 시작하기" */
    data object RegisterClicked : OnboardingAction

    data object SkipClicked : OnboardingAction
}
