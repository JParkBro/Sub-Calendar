package jpark.bro.subcal.feature.settings.impl.moreapps

import jpark.bro.subcal.core.ui.MviViewModel

internal class MoreAppsViewModel : MviViewModel<MoreAppsState, MoreAppsAction, MoreAppsEvent>(MoreAppsState()) {
    override fun onAction(action: MoreAppsAction) {
        when (action) {
            MoreAppsAction.PageLoaded -> {
                setState { copy(isLoading = false, isFailed = false) }
            }

            MoreAppsAction.PageFailed -> {
                setState { copy(isLoading = false, isFailed = true) }
            }

            MoreAppsAction.RetryClicked -> {
                setState { copy(isLoading = true, isFailed = false, reloadCount = reloadCount + 1) }
            }

            is MoreAppsAction.StoreLinkClicked -> {
                sendEvent(MoreAppsEvent.OpenStore(action.url))
            }

            MoreAppsAction.BackClicked -> {
                sendEvent(MoreAppsEvent.NavigateBack)
            }
        }
    }
}
