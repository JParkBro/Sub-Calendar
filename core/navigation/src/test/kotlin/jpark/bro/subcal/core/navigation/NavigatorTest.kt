package jpark.bro.subcal.core.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

private data object Home : NavKey

private data class List(
    val filter: String? = null,
) : NavKey

private data class Detail(
    val id: String,
) : NavKey

class NavigatorTest {
    private val navigator = Navigator(Home, setOf(Home, List()))

    @Test
    fun `일반 화면은 현재 탭 스택에 쌓인다`() {
        navigator.goTo(Detail("a"))

        assertEquals(listOf(Home, Detail("a")), navigator.backStack.toList())
    }

    @Test
    fun `다른 탭으로 갔다 돌아와도 보던 위치가 유지된다`() {
        navigator.goTo(List())
        navigator.goTo(Detail("a"))
        navigator.goTo(Home)
        navigator.goTo(List())

        assertEquals(listOf(Home, List(), Detail("a")), navigator.backStack.toList())
    }

    @Test
    fun `첫 화면에서 뒤로 가면 시작 탭으로 돌아가고 시작 탭에서는 그대로다`() {
        navigator.goTo(List())

        navigator.goBack()
        assertEquals(listOf<NavKey>(Home), navigator.backStack.toList())
        assertEquals(Home, navigator.currentTab)

        navigator.goBack()
        assertEquals(listOf<NavKey>(Home), navigator.backStack.toList())
    }

    @Test
    fun `인자가 다른 탭 키도 같은 탭으로 본다`() {
        assertTrue(navigator.isTopLevel(List("VIDEO")))
        assertFalse(navigator.isTopLevel(Detail("a")))
        assertFalse(navigator.isTopLevel(null))
    }

    @Test
    fun `switchTab은 탭 스택을 새 인자로 바꾼다`() {
        navigator.goTo(List())
        navigator.goTo(Detail("a"))
        navigator.goTo(Home)

        navigator.switchTab(List("VIDEO"))

        assertEquals(listOf(Home, List("VIDEO")), navigator.backStack.toList())
        assertEquals(List("VIDEO"), navigator.currentTab)
    }

    @Test
    fun `switchTab 뒤에 탭을 다시 눌러도 적용된 인자를 유지한다`() {
        navigator.switchTab(List("VIDEO"))
        navigator.goTo(Home)

        navigator.goTo(List())

        assertEquals(List("VIDEO"), navigator.currentTab)
    }

    @Test
    fun `탭이 아닌 키로 switchTab하면 실패한다`() {
        assertThrows(IllegalArgumentException::class.java) { navigator.switchTab(Detail("a")) }
    }

    @Test
    fun `replaceAll은 모든 스택을 지우고 시작 탭을 바꾼다`() {
        navigator.goTo(List())
        navigator.goTo(Detail("a"))

        navigator.replaceAll(Home)

        assertEquals(listOf<NavKey>(Home), navigator.backStack.toList())
        assertEquals(Home, navigator.currentTab)
    }

    @Test
    fun `replaceTop은 맨 위 화면만 바꾼다`() {
        navigator.goTo(Detail("a"))

        navigator.replaceTop(Detail("b"))

        assertEquals(listOf(Home, Detail("b")), navigator.backStack.toList())
    }
}
