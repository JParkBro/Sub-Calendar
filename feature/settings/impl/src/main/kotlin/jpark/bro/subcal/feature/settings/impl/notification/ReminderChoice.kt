package jpark.bro.subcal.feature.settings.impl.notification

import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.Subscription

/** 구독별 알림 시점 선택지 (product-spec 2-12): 기본 · 끔 · 당일 · 1 · 3 · 7일 전 */
@Suppress("MagicNumber") // 일수가 곧 선택지 값
internal enum class ReminderChoice(
    val mode: ReminderMode,
    val daysBefore: Int?,
) {
    DEFAULT(ReminderMode.DEFAULT, null),
    OFF(ReminderMode.OFF, null),
    SAME_DAY(ReminderMode.CUSTOM, 0),
    ONE_DAY(ReminderMode.CUSTOM, 1),
    THREE_DAYS(ReminderMode.CUSTOM, 3),
    SEVEN_DAYS(ReminderMode.CUSTOM, 7),
    ;

    companion object {
        /** 저장된 값에 맞는 선택지. 고를 수 없는 일수면 기본으로 본다 */
        fun of(subscription: Subscription): ReminderChoice =
            when (subscription.reminderMode) {
                ReminderMode.DEFAULT -> {
                    DEFAULT
                }

                ReminderMode.OFF -> {
                    OFF
                }

                ReminderMode.CUSTOM -> {
                    entries.firstOrNull { it.daysBefore == subscription.reminderDaysBefore }
                        ?: DEFAULT
                }
            }
    }
}
