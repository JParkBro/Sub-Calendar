package jpark.bro.subcal.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoundToManwonTest {
    @Test
    fun `만 원 단위로 반올림한다`() {
        // product-spec 9장 연간 환산 914,280원
        assertEquals(91L, roundToManwon(914_280))
        assertEquals(92L, roundToManwon(915_000))
        assertEquals(91L, roundToManwon(914_999))
        assertEquals(1L, roundToManwon(10_000))
        assertEquals(1_235L, roundToManwon(12_345_678))
    }

    @Test
    fun `1만 원 미만은 만 원 단위로 쓰지 않는다`() {
        assertNull(roundToManwon(9_999))
        assertNull(roundToManwon(0))
        assertNull(roundToManwon(-9_999))
    }

    @Test
    fun `음수는 절댓값을 반올림한다`() {
        assertEquals(-2L, roundToManwon(-15_000))
    }
}
