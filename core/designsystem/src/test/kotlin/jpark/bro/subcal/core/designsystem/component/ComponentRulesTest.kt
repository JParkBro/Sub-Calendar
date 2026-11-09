package jpark.bro.subcal.core.designsystem.component

import androidx.compose.ui.text.AnnotatedString
import jpark.bro.subcal.core.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Test

class ComponentRulesTest {
    @Test
    fun `D-day는 오늘부터 3일까지 임박이고 4일부터 기본, 지나면 흐리게`() {
        assertEquals(DdayTone.PASSED, ddayTone(-1))
        assertEquals(DdayTone.SOON, ddayTone(0))
        assertEquals(DdayTone.SOON, ddayTone(3))
        assertEquals(DdayTone.DEFAULT, ddayTone(4))
    }

    @Test
    fun `한글 이름은 첫 글자 1자`() {
        assertEquals("넷", serviceInitials("넷플릭스 프리미엄"))
        assertEquals("디", serviceInitials("디즈니+"))
    }

    @Test
    fun `영문 이름은 첫 두 단어의 머리글자 대문자`() {
        assertEquals("YP", serviceInitials("YouTube Premium"))
        assertEquals("GO", serviceInitials("google one 200GB"))
        assertEquals("S", serviceInitials("Spotify"))
    }

    @Test
    fun `빈 이름과 앞뒤 공백, 이모지`() {
        assertEquals("", serviceInitials("   "))
        assertEquals("S", serviceInitials("  Spotify  "))
        assertEquals("🎵", serviceInitials("🎵 음악"))
    }

    @Test
    fun `원화 입력은 숫자만 남기고 앞의 0을 지운다`() {
        assertEquals("17000", sanitizeAmountInput("17,000원", Currency.KRW))
        assertEquals("100", sanitizeAmountInput("00100", Currency.KRW))
        assertEquals("0", sanitizeAmountInput("00", Currency.KRW))
        assertEquals("", sanitizeAmountInput("", Currency.KRW))
    }

    @Test
    fun `달러로 입력한 소수는 원화로 바꾸면 버린다`() {
        assertEquals("9", sanitizeAmountInput("9.99", Currency.KRW))
    }

    @Test
    fun `달러 입력은 소수점 하나와 소수 둘째 자리까지`() {
        assertEquals("9.99", sanitizeAmountInput("9.999", Currency.USD))
        assertEquals("9.", sanitizeAmountInput("9.", Currency.USD))
        assertEquals("1.23", sanitizeAmountInput("1.2.3", Currency.USD))
        assertEquals("0.5", sanitizeAmountInput(".5", Currency.USD))
        assertEquals("12", sanitizeAmountInput("12", Currency.USD))
    }

    @Test
    fun `정수부는 12자리까지만 받는다`() {
        assertEquals("123456789012", sanitizeAmountInput("1234567890123", Currency.KRW))
    }

    private fun transformed(raw: String) = ThousandsSeparatorTransformation.filter(AnnotatedString(raw))

    @Test
    fun `천 단위 쉼표를 그린다`() {
        assertEquals("999", transformed("999").text.text)
        assertEquals("1,000", transformed("1000").text.text)
        assertEquals("1,234,567", transformed("1234567").text.text)
        assertEquals("1,234.56", transformed("1234.56").text.text)
    }

    @Test
    fun `커서 위치가 쉼표를 건너 원문과 맞는다`() {
        val result = transformed("1234567")

        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(1, result.offsetMapping.originalToTransformed(1))
        assertEquals(3, result.offsetMapping.originalToTransformed(2))
        assertEquals(9, result.offsetMapping.originalToTransformed(7))
        assertEquals(1, result.offsetMapping.transformedToOriginal(2))
        assertEquals(7, result.offsetMapping.transformedToOriginal(9))
    }

    @Test
    fun `소수부 커서도 쉼표 개수만큼 밀린다`() {
        val result = transformed("1234.5")

        assertEquals(7, result.offsetMapping.originalToTransformed(6))
        assertEquals(6, result.offsetMapping.transformedToOriginal(7))
    }
}
