package jpark.bro.subcal.core.designsystem.component

import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountInputTest {
    @Test
    fun `원화 입력은 그대로 원이다`() {
        assertEquals(13_900L, amountInputToMinor("13900", Currency.KRW))
        assertNull(amountInputToMinor("", Currency.KRW))
    }

    @Test
    fun `달러 입력은 센트로 바꾼다`() {
        assertEquals(999L, amountInputToMinor("9.99", Currency.USD))
        assertEquals(950L, amountInputToMinor("9.5", Currency.USD))
        assertEquals(2_000L, amountInputToMinor("20.", Currency.USD))
        assertEquals(5L, amountInputToMinor("0.05", Currency.USD))
        assertEquals(1_000L, amountInputToMinor("10", Currency.USD))
    }

    @Test
    fun `저장한 금액을 입력 원문으로 되돌린다`() {
        assertEquals("17000", minorToAmountInput(Money(17_000, Currency.KRW)))
        assertEquals("9.99", minorToAmountInput(Money(999, Currency.USD)))
        assertEquals("0.05", minorToAmountInput(Money(5, Currency.USD)))
        assertEquals("20", minorToAmountInput(Money(2_000, Currency.USD)))
    }
}
