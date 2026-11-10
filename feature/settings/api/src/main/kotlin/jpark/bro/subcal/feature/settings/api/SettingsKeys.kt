package jpark.bro.subcal.feature.settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 더보기 탭 (product-spec 6장) */
@Serializable
data object MoreKey : NavKey

/** 알림 설정 (product-spec 6장) */
@Serializable
data object NotificationSettingsKey : NavKey

/** 데이터를 통째로 바꿨을 때. app이 목적지를 정한다 (module-map 4-3) */
sealed interface DataReset {
    /** 백업을 불러왔다 → 홈 */
    data object Restored : DataReset
}

/** 구현은 app에 둔다 */
fun interface DataResetHandler {
    fun handle(result: DataReset)
}
