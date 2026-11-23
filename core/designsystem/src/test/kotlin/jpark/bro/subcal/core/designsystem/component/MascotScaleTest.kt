package jpark.bro.subcal.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

class MascotScaleTest {
    @Test
    fun `딱 맞으면 그 배율로 그린다`() {
        assertEquals(3, mascotPixelScale(availablePx = 240, spritePx = 80))
    }

    @Test
    fun `남는 칸은 버리고 정수 배율로 내린다`() {
        // 120dp × 2.75 = 330px → 4배(320px)
        assertEquals(4, mascotPixelScale(availablePx = 330, spritePx = 80))
        assertEquals(2, mascotPixelScale(availablePx = 239, spritePx = 80))
    }

    @Test
    fun `자리가 그림보다 작아도 1배로 그린다`() {
        assertEquals(1, mascotPixelScale(availablePx = 40, spritePx = 80))
    }
}
