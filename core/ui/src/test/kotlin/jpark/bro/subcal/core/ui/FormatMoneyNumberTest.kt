package jpark.bro.subcal.core.ui

import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatMoneyNumberTest {
    @Test
    fun `원화는 천 단위 쉼표를 찍고 소수가 없다`() {
        assertEquals("0", formatMoneyNumber(Money(0, Currency.KRW)))
        assertEquals("999", formatMoneyNumber(Money(999, Currency.KRW)))
        assertEquals("13,900", formatMoneyNumber(Money(13_900, Currency.KRW)))
        assertEquals("1,234,567", formatMoneyNumber(Money(1_234_567, Currency.KRW)))
    }

    @Test
    fun `달러는 센트를 소수 둘째 자리로 표시한다`() {
        assertEquals("9.99", formatMoneyNumber(Money(999, Currency.USD)))
        assertEquals("0.05", formatMoneyNumber(Money(5, Currency.USD)))
        assertEquals("10.00", formatMoneyNumber(Money(1_000, Currency.USD)))
        assertEquals("1,234.50", formatMoneyNumber(Money(123_450, Currency.USD)))
    }
}
