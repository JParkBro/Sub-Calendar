package jpark.bro.subcal.feature.calendar.impl

import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.TrialOutcome
import java.time.LocalDate
import java.time.YearMonth

/** product-spec 9장 11월 (오늘 = 2026.10.30). 25일에 3건을 겹쳐 "+n"을 보여준다 */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewCalendarState(): CalendarState {
    val records =
        listOf(
            preview("넷플릭스 프리미엄", 17_000, "2024-07-02", "RED"),
            preview("쿠팡 와우", 7_890, "2025-02-05", "ORANGE"),
            preview("유튜브 프리미엄", 14_900, "2025-01-14", "PINK"),
            preview("멜론", 10_900, "2025-05-25", "GREEN"),
            preview("왓챠", 7_900, "2025-05-25", "PINK"),
            preview("지니", 8_400, "2025-05-25", "BLUE"),
            SubscriptionRecord(
                preview("밀리의 서재", 9_900, "2026-11-12", "AMBER").subscription.copy(
                    trialStartedOn = LocalDate.of(2026, 10, 13),
                    trialOutcome = TrialOutcome.PENDING,
                ),
            ),
        )
    return buildCalendarState(
        records = records,
        usdKrwRate = 1_400,
        month = YearMonth.of(2026, 11),
        selectedDate = null,
        today = LocalDate.of(2026, 10, 30),
    )
}

private fun preview(
    name: String,
    amount: Long,
    first: String,
    colorKey: String,
): SubscriptionRecord =
    SubscriptionRecord(
        Subscription(
            id = name,
            name = name,
            amount = Money(amount, Currency.KRW),
            cycle = BillingCycle(CycleUnit.MONTH),
            firstBillingDate = LocalDate.parse(first),
            colorKey = colorKey,
            createdAt = 0,
            updatedAt = 0,
        ),
    )
