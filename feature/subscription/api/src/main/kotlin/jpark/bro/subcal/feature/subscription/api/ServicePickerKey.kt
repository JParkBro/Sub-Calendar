package jpark.bro.subcal.feature.subscription.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 서비스 선택: 프리셋을 고르거나 직접 입력으로 구독 입력 화면을 연다 */
@Serializable
data object ServicePickerKey : NavKey
