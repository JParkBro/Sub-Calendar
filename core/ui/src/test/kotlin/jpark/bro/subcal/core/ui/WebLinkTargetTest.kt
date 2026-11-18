package jpark.bro.subcal.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class WebLinkTargetTest {
    private val pageHost = "jparkbro-apps.jparkbro.workers.dev"

    @Test
    fun `같은 사이트 https는 웹뷰 안에서 연다`() {
        assertEquals(WebLinkTarget.IN_PAGE, webLinkTarget(pageHost, "https", pageHost))
    }

    @Test
    fun `Play 스토어 https 링크는 스토어로 넘긴다`() {
        assertEquals(WebLinkTarget.PLAY_STORE, webLinkTarget(pageHost, "https", "play.google.com"))
    }

    @Test
    fun `market 링크는 스토어로 넘긴다`() {
        assertEquals(WebLinkTarget.PLAY_STORE, webLinkTarget(pageHost, "market", "details"))
    }

    @Test
    fun `다른 사이트 https는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(pageHost, "https", "example.com"))
    }

    @Test
    fun `같은 사이트라도 http는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(pageHost, "http", pageHost))
    }

    @Test
    fun `intent 링크는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(pageHost, "intent", null))
    }

    @Test
    fun `javascript 링크는 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(pageHost, "javascript", null))
    }

    @Test
    fun `play google com 흉내 낸 하위 도메인은 막는다`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(pageHost, "https", "play.google.com.example.com"))
    }
}
