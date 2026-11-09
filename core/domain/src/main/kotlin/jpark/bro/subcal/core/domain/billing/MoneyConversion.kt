package jpark.bro.subcal.core.domain.billing

import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money

private const val CENTS_PER_DOLLAR = 100L

/** 원화 금액. USD는 round(센트 × 환율 / 100), 원 단위 반올림 (product-spec 2-4) */
fun Money.toKrw(usdKrwRate: Long): Long =
    when (currency) {
        Currency.KRW -> amountMinor
        Currency.USD -> roundedDiv(amountMinor * usdKrwRate, CENTS_PER_DOLLAR)
    }

/** 0 이상의 값을 나누고 반올림한다. 부동소수를 거치지 않는다 */
internal fun roundedDiv(
    numerator: Long,
    denominator: Long,
): Long = (numerator * 2 + denominator) / (denominator * 2)
