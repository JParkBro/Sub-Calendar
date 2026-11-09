package jpark.bro.subcal.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomBar
import jpark.bro.subcal.core.designsystem.component.SubCalTab
import jpark.bro.subcal.core.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

/** 앱 루트: NavDisplay + 하단 탭. 하단 탭은 탭의 첫 화면에서만 보인다. */
@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun SubCalApp(navigator: Navigator = koinInject()) {
    val showTabs = navigator.isTopLevel(navigator.backStack.lastOrNull())

    Scaffold(
        containerColor = AppThemeTokens.colors.bg,
        bottomBar = {
            if (showTabs) {
                SubCalTabBar(
                    current = navigator.currentTab,
                    onSelect = { navigator.goTo(it.key) },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::goBack,
            modifier = Modifier.padding(innerPadding),
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider = koinEntryProvider(),
        )
    }
}

@Composable
private fun SubCalTabBar(
    current: NavKey,
    onSelect: (TopLevelDestination) -> Unit,
) {
    val destinations = TopLevelDestination.entries
    SubCalBottomBar(
        tabs = destinations.map { SubCalTab(label = stringResource(it.labelRes), icon = it.iconRes) },
        selectedIndex = destinations.indexOfFirst { it.key::class == current::class },
        onSelect = { onSelect(destinations[it]) },
    )
}
