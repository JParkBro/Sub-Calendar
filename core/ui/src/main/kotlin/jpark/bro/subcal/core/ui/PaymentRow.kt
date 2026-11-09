package jpark.bro.subcal.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalDdayText
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.Payment
import jpark.bro.subcal.core.model.Subscription
import java.time.LocalDate

/** 결제 한 줄: 아이콘 · 이름 · 날짜 · D-day · 금액 (theme-guide.md 7장). [daysUntil]은 호출하는 쪽에서 계산해 넘긴다 */
@Composable
fun PaymentRow(
    payment: Payment,
    daysUntil: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val typography = AppThemeTokens.typography
    SubCalListItem(
        title = payment.subscription.name,
        modifier = modifier,
        subtitle = stringResource(R.string.common_date_month_day, payment.date.monthValue, payment.date.dayOfMonth),
        onClick = onClick,
        showChevron = false,
        leading = { SubCalServiceIcon(name = payment.subscription.name, colorKey = payment.subscription.colorKey) },
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                MoneyText(money = payment.amount, style = typography.labelLg)
                SubCalDdayText(daysUntil = daysUntil, style = typography.labelSm)
            }
        },
    )
}

/** product-spec 9장 샘플 (오늘 = 2026.10.30) */
@ComponentPreviews
@Composable
private fun PaymentRowPreview() {
    fun payment(
        name: String,
        amount: Long,
        date: LocalDate,
        colorKey: String,
    ): Payment {
        val money = Money(amount, Currency.KRW)
        val subscription =
            Subscription(
                id = name,
                name = name,
                amount = money,
                cycle = BillingCycle(CycleUnit.MONTH),
                firstBillingDate = date,
                colorKey = colorKey,
                createdAt = 0,
                updatedAt = 0,
            )
        return Payment(subscription, date, money)
    }
    SubCalPreviewSurface {
        SubCalCard {
            PaymentRow(payment("스포티파이", 10_900, LocalDate.of(2026, 10, 30), "GREEN"), daysUntil = 0)
            PaymentRow(payment("넷플릭스 프리미엄", 17_000, LocalDate.of(2026, 11, 2), "RED"), daysUntil = 3)
            PaymentRow(payment("쿠팡 와우", 7_890, LocalDate.of(2026, 11, 5), "ORANGE"), daysUntil = 6)
        }
    }
}
