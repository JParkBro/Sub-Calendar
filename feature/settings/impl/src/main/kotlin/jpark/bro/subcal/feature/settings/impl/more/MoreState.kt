package jpark.bro.subcal.feature.settings.impl.more

import jpark.bro.subcal.core.model.NotificationSettings

/**
 * 더보기 (product-spec 6장). 이번 단계에는 "알림 설정"만 있고 나머지 로컬 메뉴는 7단계에서 붙인다.
 * 아직 만들지 않은 메뉴는 숨긴다 (비활성 표시하지 않음).
 */
internal data class MoreState(
    val isLoading: Boolean = true,
    /** 알림 메뉴 설명: "결제 1일 전 · 오전 9:00" 또는 "꺼짐" */
    val notification: NotificationSettings = NotificationSettings(),
)
