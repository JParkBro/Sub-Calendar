package jpark.bro.subcal.feature.settings.impl.notification

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.NotificationSettings
import java.time.LocalDate
import java.time.LocalTime

/**
 * 알림 설정 (product-spec 6장): 결제 알림 · 기본 시점 · 시각 · 구독별 설정 · 무료체험 알림.
 * 월초 요약(2차 다음 PR), 파티 정산(3차) 토글은 아직 숨긴다.
 */
internal data class NotificationSettingsState(
    val isLoading: Boolean = true,
    /** 기기(시스템)에서 이 앱 알림이 켜져 있는지. 꺼져 있으면 시스템 설정을 여는 카드 */
    val deviceNotificationsEnabled: Boolean = true,
    val settings: NotificationSettings = NotificationSettings(),
    /** 해지하지 않은 구독, 이름순 */
    val subscriptions: List<SubscriptionReminderItem> = emptyList(),
    val isTimeSheetVisible: Boolean = false,
    /** 알림 시점을 고르는 중인 구독 */
    val editing: SubscriptionReminderItem? = null,
)

internal data class SubscriptionReminderItem(
    val id: String,
    val name: String,
    val colorKey: String,
    val cycle: BillingCycle,
    val firstBillingDate: LocalDate,
    val choice: ReminderChoice,
    /** 실제로 알릴 시점: 기본이면 전역 설정 또는 매년 결제 7일 전. 끄면 null */
    val leadDays: Int?,
)

/** 고를 수 있는 알림 시각: 오전 8시 ~ 오후 9시 정각 (product-spec 2-12) */
internal val SelectableTimes: List<LocalTime> =
    (NotificationSettings.EarliestTime.hour..NotificationSettings.LatestTime.hour).map { LocalTime.of(it, 0) }

/** 기본 알림 시점 칩 순서: 당일 · 1 · 3 · 7일 전 */
internal val DaysBeforeChoices: List<Int> = NotificationSettings.DaysBeforeOptions.sorted()
