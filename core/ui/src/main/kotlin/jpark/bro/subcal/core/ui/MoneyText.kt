package jpark.bro.subcal.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import java.util.Locale

private const val CENTS_PER_DOLLAR = 100

/** 금액 색 (theme-guide.md 6장 2번) */
enum class MoneyTone {
    /** 기본: text-primary */
    DEFAULT,

    /** 이번 달 대표 금액: primary */
    HIGHLIGHT,

    /** 아낀 돈 · 감소: success */
    POSITIVE,

    /** 증가: danger */
    NEGATIVE,
}

/** 금액 한 덩어리. 숫자 폭이 흔들리지 않게 tabular-nums를 켠다 */
@Composable
fun MoneyText(
    money: Money,
    modifier: Modifier = Modifier,
    style: TextStyle = AppThemeTokens.typography.metricTabular,
    tone: MoneyTone = MoneyTone.DEFAULT,
) {
    val colors = AppThemeTokens.colors
    Text(
        text = formatMoney(money),
        style = style.copy(fontFeatureSettings = "tnum"),
        color =
            when (tone) {
                MoneyTone.DEFAULT -> colors.textPrimary
                MoneyTone.HIGHLIGHT -> colors.primary
                MoneyTone.POSITIVE -> colors.success
                MoneyTone.NEGATIVE -> colors.danger
            },
        modifier = modifier,
    )
}

/** 표시용 금액: 13,900원 · $9.99 (product-spec 2-4) */
@Composable
fun formatMoney(money: Money): String =
    stringResource(
        when (money.currency) {
            Currency.KRW -> R.string.common_money_krw
            Currency.USD -> R.string.common_money_usd
        },
        formatMoneyNumber(money),
    )

/** 통화 기호를 뺀 숫자: 천 단위 쉼표, USD는 소수 둘째 자리까지. 기기 언어와 상관없이 같은 모양이다 */
internal fun formatMoneyNumber(money: Money): String =
    when (money.currency) {
        Currency.KRW -> {
            String.format(Locale.US, "%,d", money.amountMinor)
        }

        Currency.USD -> {
            String.format(
                Locale.US,
                "%,d.%02d",
                money.amountMinor / CENTS_PER_DOLLAR,
                money.amountMinor % CENTS_PER_DOLLAR,
            )
        }
    }

@ComponentPreviews
@Composable
private fun MoneyTextPreview() {
    SubCalPreviewSurface {
        MoneyText(
            money = Money(72_490, Currency.KRW),
            style = AppThemeTokens.typography.displayHeroMobile,
            tone = MoneyTone.HIGHLIGHT,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            MoneyText(money = Money(13_900, Currency.KRW))
            MoneyText(money = Money(999, Currency.USD))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            MoneyText(money = Money(15_800, Currency.KRW), tone = MoneyTone.POSITIVE)
            MoneyText(money = Money(10_900, Currency.KRW), tone = MoneyTone.NEGATIVE)
        }
    }
}
