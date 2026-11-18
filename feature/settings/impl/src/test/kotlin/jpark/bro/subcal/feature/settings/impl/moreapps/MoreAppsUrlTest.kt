package jpark.bro.subcal.feature.settings.impl.moreapps

import org.junit.Assert.assertEquals
import org.junit.Test

class MoreAppsUrlTest {
    @Test
    fun `출시 패키지 이름과 라이트 테마를 붙인다`() {
        assertEquals(
            "https://jparkbro-apps.jparkbro.workers.dev/?from=jpark.bro.subcal&theme=light",
            moreAppsUrl(packageName = "jpark.bro.subcal", isDark = false),
        )
    }

    @Test
    fun `dev 접미사는 떼고 다크 테마를 붙인다`() {
        assertEquals(
            "https://jparkbro-apps.jparkbro.workers.dev/?from=jpark.bro.subcal&theme=dark",
            moreAppsUrl(packageName = "jpark.bro.subcal.dev", isDark = true),
        )
    }
}
