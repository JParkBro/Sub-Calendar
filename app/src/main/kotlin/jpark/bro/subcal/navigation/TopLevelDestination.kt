package jpark.bro.subcal.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import jpark.bro.subcal.R
import jpark.bro.subcal.feature.calendar.api.CalendarKey
import jpark.bro.subcal.feature.home.api.HomeKey
import jpark.bro.subcal.feature.settings.api.MoreKey
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey

/** 하단 탭 (product-spec 3-1). 통계는 8단계에 캘린더와 더보기 사이에 추가한다 (module-map 9장) */
internal enum class TopLevelDestination(
    val key: NavKey,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    HOME(HomeKey, R.string.app_tab_home, R.drawable.ic_tab_home),
    LIST(SubscriptionListKey(), R.string.app_tab_list, R.drawable.ic_tab_list),
    CALENDAR(CalendarKey, R.string.app_tab_calendar, R.drawable.ic_tab_calendar),
    MORE(MoreKey, R.string.app_tab_more, R.drawable.ic_tab_more),
    ;

    companion object {
        val keys: Set<NavKey> = entries.map { it.key }.toSet()
    }
}
