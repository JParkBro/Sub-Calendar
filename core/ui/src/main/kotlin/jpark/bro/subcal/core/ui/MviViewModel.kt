package jpark.bro.subcal.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 화면 ViewModel의 기반 클래스. 공개 멤버는 state · event · onAction뿐이다.
 * Event가 없는 화면은 E에 Nothing을 넣는다.
 */
abstract class MviViewModel<S, A, E>(
    initialState: S,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _event = Channel<E>(Channel.BUFFERED)
    val event: Flow<E> = _event.receiveAsFlow()

    protected val currentState: S get() = _state.value

    abstract fun onAction(action: A)

    protected fun setState(reduce: S.() -> S) {
        _state.update(reduce)
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch { _event.send(event) }
    }
}
