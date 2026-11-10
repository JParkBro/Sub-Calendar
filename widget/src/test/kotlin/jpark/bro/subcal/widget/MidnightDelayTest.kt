package jpark.bro.subcal.widget

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class MidnightDelayTest {
    @Test
    fun `다음 자정 1분 뒤까지 기다린다`() {
        val now = LocalDateTime.of(2026, 10, 30, 23, 0).atZone(ZoneId.of("Asia/Seoul"))

        assertEquals(Duration.ofMinutes(61), delayUntilNextMidnight(now))
    }

    @Test
    fun `자정 정각이면 다음 날 자정까지 기다린다`() {
        val now = LocalDateTime.of(2026, 10, 31, 0, 0).atZone(ZoneId.of("Asia/Seoul"))

        assertEquals(Duration.ofHours(24).plusMinutes(1), delayUntilNextMidnight(now))
    }

    @Test
    fun `서머타임으로 하루가 23시간인 날도 자정에 맞춘다`() {
        // 미국 동부 2026.3.8 02시에 서머타임 시작: 3/8 00:00 → 3/9 00:00은 23시간
        val now = LocalDateTime.of(2026, 3, 8, 0, 0).atZone(ZoneId.of("America/New_York"))

        assertEquals(Duration.ofHours(23).plusMinutes(1), delayUntilNextMidnight(now))
    }
}
