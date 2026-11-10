package jpark.bro.subcal.feature.settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 더보기 탭 (product-spec 6장) */
@Serializable
data object MoreKey : NavKey

/** 알림 설정 (product-spec 6장) */
@Serializable
data object NotificationSettingsKey : NavKey
