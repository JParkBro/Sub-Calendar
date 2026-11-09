package jpark.bro.subcal.core.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

/**
 * 앱의 백스택. 하단 탭마다 백스택을 따로 두어 탭을 바꿔도 각 탭의 화면 위치가 유지된다.
 * 탭 키는 인자를 가질 수 있어(목록의 카테고리 필터) 키 값이 아니라 클래스로 구분한다.
 * - backStack: NavDisplay에 넘기는 목록 = 시작 탭 스택 (+ 현재 탭이 시작 탭이 아니면 현재 탭 스택)
 * - 현재 탭의 스택이 첫 화면만 남은 상태에서 뒤로 가면 시작 탭으로 돌아간다.
 */
class Navigator(
    startKey: NavKey,
    topLevelKeys: Set<NavKey> = emptySet(),
) {
    private val topLevelTypes: Set<KClass<out NavKey>> = topLevelKeys.map { it::class }.toSet()
    private val stacks = linkedMapOf<KClass<out NavKey>, MutableList<NavKey>>()
    private var startTab: KClass<out NavKey> = startKey::class

    val backStack: SnapshotStateList<NavKey> = mutableStateListOf()

    /** 현재 탭의 첫 화면 키 (하단 탭 선택 표시용) */
    var currentTab: NavKey by mutableStateOf(startKey)
        private set

    init {
        reset(startKey)
    }

    fun isTopLevel(key: NavKey?): Boolean = key != null && key::class in topLevelTypes

    /** 탭 키면 그 탭으로 전환해 보던 위치를 유지하고, 아니면 현재 탭 스택에 쌓는다. */
    fun goTo(key: NavKey) {
        if (isTopLevel(key)) {
            currentTab = stacks.getOrPut(key::class) { mutableListOf(key) }.first()
        } else {
            currentStack().add(key)
        }
        sync()
    }

    /** 다른 화면에서 탭으로 보낼 때: 그 탭의 스택을 [key] 하나로 바꿔 인자를 적용한다 */
    fun switchTab(key: NavKey) {
        require(isTopLevel(key)) { "탭 키가 아닙니다: $key" }
        stacks[key::class] = mutableListOf(key)
        currentTab = key
        sync()
    }

    fun goBack() {
        val stack = currentStack()
        when {
            stack.size > 1 -> stack.removeAt(stack.lastIndex)
            currentTab::class != startTab -> currentTab = stacks.getValue(startTab).first()
            else -> return
        }
        sync()
    }

    /** 온보딩 완료처럼 이전 화면을 모두 지우고 이동 */
    fun replaceAll(key: NavKey) {
        reset(key)
    }

    /** 현재 화면을 다른 화면으로 교체 */
    fun replaceTop(key: NavKey) {
        val stack = currentStack()
        stack.removeAt(stack.lastIndex)
        stack.add(key)
        sync()
    }

    private fun currentStack(): MutableList<NavKey> = stacks.getValue(currentTab::class)

    private fun reset(key: NavKey) {
        stacks.clear()
        stacks[key::class] = mutableListOf(key)
        startTab = key::class
        currentTab = key
        sync()
    }

    private fun sync() {
        val start = stacks.getValue(startTab)
        val visible = if (currentTab::class != startTab) start + currentStack() else start
        backStack.clear()
        backStack.addAll(visible)
    }
}
