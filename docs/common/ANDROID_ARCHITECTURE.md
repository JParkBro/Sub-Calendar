# Android 아키텍처 가이드 (범용 템플릿)

> Jetpack Compose · Navigation 3 · Koin · Ktor · Room · MVI · 기능별 api/impl 멀티 모듈
>
> 이 문서 하나만으로 새 앱에 같은 구조를 적용할 수 있도록 작성했습니다. 문서 안의 `{app}`, `{base.package}`, `{feature}`, `{Feature}`는 프로젝트에 맞게 바꿔 사용합니다.
>
> 이 문서는 **무엇을 어디에** 두는지 정합니다. 코드를 **어떻게** 쓰는지(이름, MVI 코드 규칙, Compose, 코루틴, 에러 처리 등)는 [CODING_CONVENTIONS.md](CODING_CONVENTIONS.md)를 따릅니다. 테스트 방법은 [TESTING.md](TESTING.md)를 따릅니다.

| 자리표시자 | 의미 | 예 |
|---|---|---|
| `{app}` | 프로젝트 약칭 (convention plugin ID 접두사) | `myapp` |
| `{base.package}` | 기본 패키지 | `com.example.myapp` |
| `{feature}` | 기능 이름 (소문자) | `home`, `settings` |
| `{Feature}` | 기능 이름 (클래스 접두사) | `Home`, `Settings` |

---

## 목차

1. 기술 스택
2. 아키텍처 원칙
3. 모듈 구조
4. 의존 규칙
5. 내비게이션 (Navigation 3 + api/impl)
6. 화면 패턴 (MVI)
7. 의존성 주입 (Koin)
8. 데이터 계층 (Repository · Room · Ktor)
9. Gradle 구성 (Version Catalog · Convention Plugin)
10. 이름 · 패키지 규칙
11. 테스트 전략
12. 새 기능 추가 절차
13. 자주 하는 실수

---

## 1. 기술 스택

| 영역 | 라이브러리 | 비고 |
|---|---|---|
| 언어 | Kotlin | Coroutines, Flow, kotlinx.serialization |
| UI | Jetpack Compose | Material 3 위에 자체 디자인 시스템 |
| 내비게이션 | Navigation 3 (`androidx.navigation3`) | 백스택을 앱이 직접 소유 |
| ViewModel 연동 | `lifecycle-viewmodel-navigation3` | 화면 단위 ViewModel 수명 관리 |
| 의존성 주입 | Koin (`koin-android`, `koin-compose`, `koin-compose-viewmodel`, `koin-compose-navigation3`) | Navigation 3 연동은 실험적 API |
| 네트워크 | Ktor Client (OkHttp 엔진) | ContentNegotiation, Auth, Logging |
| 로컬 DB | Room | 오프라인 우선일 때 원본 저장소 |
| 키-값 저장 | DataStore (Preferences) | 설정, 플래그, 커서 |
| 백그라운드 | WorkManager (`koin-androidx-workmanager`) | 동기화, 재시도 |
| 이미지 | Coil | 선택 |
| 빌드 | Gradle Version Catalog + build-logic convention plugins | |

> 버전은 이 문서에 고정하지 않습니다. 적용 시점의 최신 안정 버전을 확인하고, **Koin의 Navigation 3 연동은 `@KoinExperimentalAPI`이므로 버전을 고정**한 뒤 업데이트 시 변경 사항을 확인합니다.

---

## 2. 아키텍처 원칙

1. **단방향 데이터 흐름.** 화면은 State를 그리고, 사용자 입력은 Action으로 올라가며, ViewModel만 State를 바꿉니다.
2. **계층 분리.** UI(feature) → Domain(선택) → Data → 데이터 소스(DB, 네트워크). 안쪽 계층은 바깥 계층을 모릅니다.
3. **기능은 api/impl로 분리.** 다른 기능은 api(내비게이션 키)만 알고 구현은 모릅니다.
4. **화면 이동은 ViewModel 밖에서.** ViewModel은 Event만 내보내고, 실제 이동은 Root 컴포저블이 처리합니다.
5. **오프라인 우선 (선택).** 서버 동기화가 있는 앱은 Room을 원본으로 두고, 화면은 Room만 구독합니다.
6. **원격 저장소는 인터페이스 뒤에.** Firebase, 자체 서버 등 구현체를 교체할 수 있게 합니다.
7. **순수 로직은 순수 Kotlin 모듈에.** 계산 · 규칙은 Android 의존 없이 JVM 테스트로 검증합니다.

---

## 3. 모듈 구조

```
{project-root}/
├─ app/                          # Application, MainActivity, NavDisplay, Koin 시작, 시작 목적지 결정
├─ build-logic/
│  └─ convention/                # convention plugins
├─ gradle/
│  └─ libs.versions.toml
│
├─ core/
│  ├─ model/                     # [JVM] 도메인 모델 (data class, enum)
│  ├─ common/                    # [JVM] Result, 디스패처 한정자, 날짜·문자열 유틸
│  ├─ domain/                    # [JVM] 유스케이스, 비즈니스 규칙 (선택)
│  ├─ navigation/                # Navigator, 탭 백스택, NavKey 공통 유틸
│  ├─ designsystem/              # 테마 토큰, 타이포, 공통 컴포넌트
│  ├─ ui/                        # MVI 기반 클래스, ObserveEvents, 기능 간 공유 UI
│  ├─ database/                  # Room: Entity, DAO, Database, Migration (선택)
│  ├─ datastore/                 # DataStore (선택)
│  ├─ network/                   # Ktor HttpClient, API 서비스, DTO (선택)
│  └─ data/                      # Repository 인터페이스 + 구현, 매퍼
│
├─ feature/
│  └─ {feature}/
│     ├─ api/                    # NavKey만
│     └─ impl/                   # Screen, ViewModel, Action, State, Event, Koin 모듈
│
└─ (선택) widget/, sync/, ads/ 등 앱 특화 모듈
```

`[JVM]` 표시는 Android 의존이 없는 `kotlin("jvm")` 모듈입니다. 테스트가 에뮬레이터 없이 빠르게 돕니다.

### 모듈별 책임

| 모듈 | 들어가는 것 | 들어가면 안 되는 것 |
|---|---|---|
| `app` | Koin 시작, 모듈 조립, NavDisplay, 시작 목적지 결정 | 화면 구현, 비즈니스 로직 |
| `core:model` | 순수 데이터 클래스, enum | Android · 직렬화 프레임워크 의존 |
| `core:domain` | 유스케이스, 계산 규칙 | Repository 구현, Android 의존 |
| `core:navigation` | Navigator, 백스택 조작 | 기능별 NavKey |
| `core:designsystem` | 색 · 글꼴 · 모양 토큰, 버튼 · 카드 등 기본 컴포넌트 | 기능 전용 화면 |
| `core:ui` | MviViewModel, ObserveEvents, 공유 UI 조각 | 특정 기능에 묶인 컴포넌트 |
| `core:data` | Repository, 매퍼, 원격 데이터 소스 인터페이스 | Compose, ViewModel |
| `core:database` | Entity, DAO, Migration | Repository 로직 |
| `core:network` | HttpClient, API 호출, DTO | Repository 로직 |
| `feature:*:api` | `@Serializable` NavKey | ViewModel, Compose, Koin |
| `feature:*:impl` | 화면, ViewModel, MVI 타입, Koin 모듈 | 다른 기능의 impl 참조 |

---

## 4. 의존 규칙

```
                              app
                               │ (모든 impl, core:navigation, core:designsystem)
             ┌─────────────────┼─────────────────┐
     feature:a:impl     feature:b:impl     feature:c:impl
        │    │   └──────────┐  │                 │
        │    └──▶ feature:b:api ◀── 다른 impl도 api만 참조
        ▼
   core:ui · core:designsystem · core:navigation
        │
        ▼
   core:domain ◀── core:data ──▶ core:database / core:datastore / core:network
        │              │
        └──────▶ core:model ◀──────┘
```

| 규칙 | 설명 |
|---|---|
| impl → api ✅ | 다른 기능으로 이동하려면 그 기능의 api에만 의존 |
| impl → impl ❌ | 절대 금지. 순환 참조와 결합의 원인 |
| api → api ❌ | api는 다른 api를 참조하지 않음. 그래야 순환이 생기지 않음 |
| api → core:model ⚠️ | 키에 enum 같은 단순 타입이 필요할 때만 허용 |
| feature → core:database / network ❌ | 기능은 Repository(`core:data`)만 통해 데이터 접근 |
| core → feature ❌ | core는 기능을 모름 |
| app → impl ✅ | Koin 모듈 수집과 NavDisplay 조립을 위해 app만 impl을 앎 |

impl끼리 서로의 api를 참조하는 것(`a:impl → b:api`, `b:impl → a:api`)은 api 사이에 의존이 없으므로 순환이 아닙니다.

---

## 5. 내비게이션 (Navigation 3 + api/impl)

### 5-1. 개념

- Navigation 3에서는 **백스택이 그냥 상태 리스트**입니다. 앱이 직접 추가 · 제거합니다.
- 각 화면은 **NavKey**(직렬화 가능한 키)로 식별됩니다.
- `NavDisplay`가 백스택 맨 위 키를 보고 `entryProvider`에서 해당 화면을 찾아 그립니다.
- Koin의 `navigation<Key> { }` DSL로 각 impl이 자기 화면을 등록하고, app은 `koinEntryProvider()`로 모아서 씁니다.

### 5-2. api 모듈

```kotlin
// feature/{feature}/api/src/main/kotlin/{base.package}/feature/{feature}/api/{Feature}Key.kt
package {base.package}.feature.{feature}.api

@Serializable
data object {Feature}Key : NavKey

// 인자가 있는 화면
@Serializable
data class {Feature}DetailKey(val id: String) : NavKey
```

**api에 넣는 기준:** 다른 기능에서 들어올 수 있는 화면의 키만 넣습니다. 기능 내부에서만 이동하는 화면 키는 impl에 `internal`로 둡니다.

**키 인자 규칙:** ID, 모드 같은 작은 값만 넣습니다. 객체 전체를 넘기지 말고 ID로 넘긴 뒤 화면에서 Repository로 조회합니다.

### 5-3. Navigator (core:navigation)

```kotlin
class Navigator(startKey: NavKey) {
    val backStack: SnapshotStateList<NavKey> = mutableStateListOf(startKey)

    fun goTo(key: NavKey) {
        backStack.add(key)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    /** 로그인 완료 → 메인처럼 이전 화면을 모두 지우고 이동 */
    fun replaceAll(key: NavKey) {
        backStack.clear()
        backStack.add(key)
    }

    /** 현재 화면을 다른 화면으로 교체 */
    fun replaceTop(key: NavKey) {
        backStack.removeLastOrNull()
        backStack.add(key)
    }
}
```

**하단 탭이 있는 앱:** `Navigator(startKey, topLevelKeys)`처럼 탭 키 목록을 받아 탭마다 백스택을 따로 둡니다 (탭 키 → 백스택 맵 + `currentTab`). 기능 모듈이 쓰는 `goTo` · `goBack` · `replaceAll`은 그대로이고, `goTo(탭 키)`는 탭 전환이 됩니다.
- `backStack`(NavDisplay에 넘기는 목록) = 시작 탭 스택 + (현재 탭이 시작 탭이 아니면) 현재 탭 스택
- 탭의 첫 화면에서 뒤로 가면 시작 탭으로 돌아갑니다.
- 탭을 바꾸면 다른 탭 화면은 `backStack`에서 빠지므로 그 화면의 ViewModel · 저장 상태는 정리됩니다. 화면 위치(어느 화면까지 들어갔는지)만 유지됩니다.

**프로세스 종료 대비:** `mutableStateListOf`는 프로세스가 종료되면 사라집니다. 복원이 중요하면 백스택을 `rememberNavBackStack()`(직렬화 저장 지원)으로 만들고 Navigator가 그것을 감싸도록 구성합니다.

### 5-4. impl 모듈의 화면 등록

```kotlin
// feature/{feature}/impl/.../di/{Feature}Module.kt
@OptIn(KoinExperimentalAPI::class)
val {feature}Module = module {
    viewModelOf(::{Feature}ViewModel)

    navigation<{Feature}Key> {
        val navigator = get<Navigator>()
        {Feature}Root(
            onNavigate = navigator::goTo,
            onBack = navigator::goBack,
        )
    }

    navigation<{Feature}DetailKey> { key ->
        val navigator = get<Navigator>()
        {Feature}DetailRoot(
            id = key.id,
            onBack = navigator::goBack,
        )
    }
}
```

### 5-5. app 모듈

```kotlin
// app/.../MainActivity.kt
setContent {
    AppTheme {
        val navigator: Navigator = koinInject()
        NavDisplay(
            backStack = navigator.backStack,
            onBack = { navigator.goBack() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = koinEntryProvider(),
        )
    }
}

// app/.../di/AppModule.kt
val appModule = module {
    single { Navigator(startKey = resolveStartKey()) }
    includes(
        coreModules,
        featureAModule,
        featureBModule,
        // ...
    )
}
```

- `rememberViewModelStoreNavEntryDecorator()`를 빼면 백스택에서 빠진 화면의 ViewModel이 Activity 수명만큼 남습니다. **반드시 넣습니다.**
- 공식 레시피처럼 `activityRetainedScope { }` 안에 Navigator와 navigation 항목을 두는 방식도 가능합니다. 단일 Activity 앱이면 `single`로 충분합니다.

### 5-6. 다른 기능으로 이동하기

```kotlin
// feature/a/impl/build.gradle.kts
dependencies {
    implementation(projects.feature.a.api)
    implementation(projects.feature.b.api)   // b로 이동해야 하므로
}

// feature/a/impl 의 Root
ObserveEvents(viewModel.event) { event ->
    when (event) {
        is AEvent.OpenB -> onNavigate(BKey(event.id))
    }
}
```

### 5-7. 목적지를 app에서 결정해야 하는 경우

"로그인 완료 후 어디로 갈지"처럼 **여러 조건에 따라 목적지가 바뀌는 흐름의 끝**은 impl이 결과만 알리고 app이 결정합니다.

```kotlin
// feature/auth/api
@Serializable data object AuthKey : NavKey
sealed interface AuthResult { data object SignedIn : AuthResult; data object NeedsProfile : AuthResult }

// feature/auth/impl: 결과 콜백만 노출
navigation<AuthKey> {
    AuthRoot(onFinished = get<AuthResultHandler>()::handle)
}

// app: 목적지 결정
class AppAuthResultHandler(private val navigator: Navigator) : AuthResultHandler {
    override fun handle(result: AuthResult) = when (result) {
        AuthResult.SignedIn     -> navigator.replaceAll(MainKey)
        AuthResult.NeedsProfile -> navigator.replaceAll(ProfileSetupKey)
    }
}
```

`AuthResultHandler` 인터페이스는 `auth:api`에, 구현은 `app`에 둡니다. 이렇게 하면 auth:impl이 메인 · 프로필 기능을 몰라도 됩니다.

### 5-8. 다이얼로그 · 바텀시트

- **확인 다이얼로그, 선택 시트**처럼 화면에 딸린 UI는 내비게이션이 아니라 **화면 State**(`showDeleteDialog = true`)로 처리합니다.
- 딥링크로 열리거나 독립된 화면처럼 동작해야 하는 시트만 Navigation 3의 Scene(다이얼로그 · 바텀시트 전략)으로 등록합니다.

---

## 6. 화면 패턴 (MVI)

### 6-1. 파일 구성

```
feature/{feature}/impl/src/main/kotlin/{base.package}/feature/{feature}/impl/
├─ {Feature}Action.kt      # 사용자 의도
├─ {Feature}State.kt       # 화면 상태
├─ {Feature}Event.kt       # 일회성 효과 (없으면 파일 생략)
├─ {Feature}ViewModel.kt   # Action 처리 → State 갱신 · Event 발행
├─ {Feature}Screen.kt      # internal {Feature}Root + private {Feature}Screen + Preview
├─ components/             # 이 화면 전용 컴포저블
└─ di/{Feature}Module.kt   # Koin 모듈 + navigation 등록
```

- 화면이 여러 개인 기능은 **화면마다 하위 패키지**(소문자, 예: `impl/itemdetail/`, `impl/itemedit/`)를 두고 위 파일 묶음을 그 안에 둡니다. `di/`와 기능 내부 NavKey(`{Feature}Keys.kt`, 하나면 키 이름 파일)는 impl 최상위에 둡니다.
- 인자가 있는 화면은 Root가 키를 받고 `koinViewModel { parametersOf(key) }`로 ViewModel에 넘깁니다 (`viewModel { params -> {Feature}ViewModel(key = params.get()) }`).

| 타입 | 역할 | 형태 |
|---|---|---|
| Action | 사용자가 한 일 (클릭, 입력, 새로고침) | `sealed interface` |
| State | 화면에 그릴 모든 것 | 불변 `data class` |
| Event | 한 번만 일어나야 하는 일 (이동, 스낵바, 권한 요청, 외부 앱 실행) | `sealed interface` |
| ViewModel | Action을 받아 State를 바꾸고 Event를 보냄 | `MviViewModel<S, A, E>` |
| Root | ViewModel 연결, Event 처리, 이동 콜백 연결 | 상태를 가진 컴포저블 (`internal`) |
| Screen | State를 받아 그리고 Action을 올림 | 상태 없는 컴포저블 (`private`) |

> 상태를 가진 쪽을 `Route`가 아니라 `Root`로 부르는 이유: Navigation 3에서 "route"는 NavKey(목적지)를 가리키는 말로 쓰이기 때문에 이름이 겹치지 않게 합니다.

### 6-2. Event 유무 기준

**한 번만 일어나야 하는 효과가 하나라도 있으면 Event를 만듭니다.** 화면 회전 · 재구성 후 다시 실행되면 안 되는 것들입니다.

| Event 필요 | Event 불필요 (`Nothing`) |
|---|---|
| 다른 화면으로 이동 | 시스템 뒤로 가기만 있는 상세 · 문서 화면 |
| 저장 후 뒤로 가기 | 표시 전용 화면 |
| 스낵바 · 토스트 | 화면 안에서만 상태가 바뀌는 화면 |
| 권한 요청, 사진 선택기, 공유 시트 | |
| 광고 표시, 외부 링크 열기 | |

> 다이얼로그 표시 여부처럼 **화면에 계속 남아 있어야 하는 것**은 Event가 아니라 State입니다.

### 6-3. 기반 클래스 (core:ui)

```kotlin
abstract class MviViewModel<S, A, E>(initialState: S) : ViewModel() {

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
```

```kotlin
// core:ui — 화면이 STARTED일 때만 이벤트 수집
@Composable
fun <E> ObserveEvents(events: Flow<E>, onEvent: suspend (E) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(events, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                events.collect { onEvent(it) }
            }
        }
    }
}
```

- Event는 `Channel`로 보내서 **한 번만 소비**됩니다. `SharedFlow(replay = 0)`는 수집자가 없을 때 이벤트가 사라질 수 있어 쓰지 않습니다.
- Event가 없는 화면은 세 번째 타입에 `Nothing`을 넣습니다.

### 6-4. 템플릿

```kotlin
// {Feature}Action.kt
sealed interface {Feature}Action {
    data object Refresh : {Feature}Action
    data class ItemClicked(val id: String) : {Feature}Action
    data class QueryChanged(val query: String) : {Feature}Action
}

// {Feature}State.kt
data class {Feature}State(
    val isLoading: Boolean = true,
    val query: String = "",
    val items: List<ItemUi> = emptyList(),
    val errorMessage: String? = null,
)

// {Feature}Event.kt
sealed interface {Feature}Event {
    data class NavigateToDetail(val id: String) : {Feature}Event
    data class ShowSnackbar(val message: String) : {Feature}Event
}

// {Feature}ViewModel.kt
class {Feature}ViewModel(
    private val repository: SomeRepository,
) : MviViewModel<{Feature}State, {Feature}Action, {Feature}Event>({Feature}State()) {

    init {
        repository.observeItems()
            .onEach { items -> setState { copy(isLoading = false, items = items.map { it.toUi() }) } }
            .catch { setState { copy(isLoading = false, errorMessage = it.message) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: {Feature}Action) {
        when (action) {
            {Feature}Action.Refresh -> refresh()
            is {Feature}Action.ItemClicked -> sendEvent({Feature}Event.NavigateToDetail(action.id))
            is {Feature}Action.QueryChanged -> setState { copy(query = action.query) }
        }
    }

    private fun refresh() = viewModelScope.launch {
        repository.refresh()
            .onFailure { sendEvent({Feature}Event.ShowSnackbar("새로고침에 실패했어요")) }
    }
}

// {Feature}Screen.kt
@Composable
internal fun {Feature}Root(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: {Feature}ViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is {Feature}Event.NavigateToDetail -> onNavigate({Feature}DetailKey(event.id))
            is {Feature}Event.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }

    {Feature}Screen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
private fun {Feature}Screen(
    state: {Feature}State,
    snackbarHostState: SnackbarHostState,
    onAction: ({Feature}Action) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // State만 보고 그린다. ViewModel · Koin · Navigator를 모른다.
}

@Preview
@Composable
private fun {Feature}ScreenPreview() {
    AppTheme {
        {Feature}Screen(
            state = {Feature}State(isLoading = false, items = previewItems),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onBack = {},
        )
    }
}
```

### 6-5. MVI 코드 규칙

`onAction` 단일 진입점, Action · Event의 `sealed interface` 규칙, State 작성 규칙, 이름 규칙 등 세부 코드 규칙은 [CODING_CONVENTIONS.md 5장](CODING_CONVENTIONS.md#5-mvi)에 정리되어 있습니다.

---

## 7. 의존성 주입 (Koin)

### 7-1. 모듈 구성

| Koin 모듈 | 위치 | 내용 |
|---|---|---|
| `appModule` | app | Navigator, 결과 핸들러, 전체 `includes` |
| `databaseModule` | core:database | Room DB, DAO |
| `datastoreModule` | core:datastore | DataStore |
| `networkModule` | core:network | HttpClient, API 서비스 |
| `dataModule` | core:data | Repository 바인딩 |
| `domainModule` | core:domain | 유스케이스 (`factoryOf`) |
| `{feature}Module` | feature:{feature}:impl | ViewModel, navigation 항목 |

```kotlin
// core:database
val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "app.db")
            .addMigrations(*ALL_MIGRATIONS)
            .build()
    }
    single { get<AppDatabase>().itemDao() }
}

// core:data
val dataModule = module {
    singleOf(::DefaultItemRepository) bind ItemRepository::class
}

// core:domain
val domainModule = module {
    factoryOf(::GetItemsUseCase)
}

// app
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@App)
            workManagerFactory()          // WorkManager 사용 시
            modules(appModule)
        }
    }
}
```

### 7-2. 범위 선택

| 정의 | 용도 |
|---|---|
| `single` / `singleOf` | DB, HttpClient, Repository, Navigator |
| `factory` / `factoryOf` | 유스케이스, 매퍼 |
| `viewModel` / `viewModelOf` | ViewModel (`koinViewModel()`으로 주입) |
| `worker` / `workerOf` | WorkManager Worker |

### 7-3. 구현 숨기기

- Repository 구현은 `internal class`로 두고 인터페이스로만 노출합니다.
- impl 모듈의 ViewModel · Screen도 `internal`이며, Koin 모듈 변수만 `public`입니다.

### 7-4. 검증 테스트 (필수)

Koin은 Hilt와 달리 **컴파일 시점에 의존성 누락을 잡지 못합니다.** 모든 모듈을 검증하는 테스트를 반드시 둡니다.

```kotlin
class KoinModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyAllModules() {
        appModule.verify(
            extraTypes = listOf(Context::class, SavedStateHandle::class)
        )
    }
}
```

---

## 8. 데이터 계층 (Repository · Room · Ktor)

### 8-1. 모델 3종

| 모델 | 위치 | 용도 |
|---|---|---|
| Entity | core:database | Room 테이블 |
| DTO | core:network | 서버 JSON (`@Serializable`) |
| Model | core:model | 앱 전체에서 쓰는 도메인 모델 |
| UiModel | feature impl | 화면 표시용 (선택) |

매퍼는 `core:data`에 확장 함수로 둡니다: `ItemEntity.toModel()`, `ItemDto.toEntity()`.

### 8-2. Repository

```kotlin
interface ItemRepository {
    fun observeItems(): Flow<List<Item>>
    fun observeItem(id: String): Flow<Item?>
    suspend fun save(item: Item): Result<Unit>
    suspend fun refresh(): Result<Unit>
}
```

- 읽기는 `Flow`로 노출해 데이터가 바뀌면 화면이 자동 갱신되게 합니다.
- 쓰기 · 1회성 작업은 `suspend` + `Result`로 반환합니다. 예외를 ViewModel까지 던지지 않습니다.

### 8-3. 오프라인 우선 패턴 (서버 동기화가 있는 앱)

```
읽기:  Screen ← ViewModel ← Repository.observe() ← Room
쓰기:  Screen → ViewModel → Repository.save() → Room (syncState = PENDING)
                                                   └→ SyncManager → 원격 저장소
수신:  원격 저장소 변경분 → SyncManager → Room upsert → 화면 자동 갱신
```

- 화면은 **Room만 구독**합니다. 네트워크 상태와 관계없이 즉시 반영됩니다.
- 원격 저장소는 인터페이스로 둡니다.

```kotlin
// core:data
interface RemoteSyncDataSource {
    suspend fun push(items: List<ItemDto>): Result<Unit>
    fun observeChanges(since: Long): Flow<List<ItemDto>>
}
// 구현: FirestoreRemoteSyncDataSource, ApiRemoteSyncDataSource 등
```

### 8-4. Ktor 클라이언트 (core:network)

```kotlin
val networkModule = module {
    single {
        HttpClient(OkHttp) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 30_000
            }
            install(Auth) {
                bearer {
                    loadTokens { get<TokenProvider>().current()?.let { BearerTokens(it, "") } }
                    refreshTokens { get<TokenProvider>().refresh()?.let { BearerTokens(it, "") } }
                }
            }
            if (BuildConfig.DEBUG) {
                install(Logging) { level = LogLevel.BODY }
            }
            defaultRequest { url(BuildConfig.API_BASE_URL) }
        }
    }
    singleOf(::ItemApi)
}

internal class ItemApi(private val client: HttpClient) {
    suspend fun getItems(): List<ItemDto> = client.get("items").body()
    suspend fun upload(id: String, bytes: ByteArray) {
        client.put("images/$id") {
            contentType(ContentType.Image.Any)
            setBody(bytes)
        }
    }
}
```

- 네트워크 호출은 `runCatching` 또는 공통 `safeCall { }` 헬퍼로 감싸 `Result`로 변환합니다.
- 토큰 · 헤더 같은 공통 처리는 플러그인으로 한 곳에서 합니다.

### 8-5. 디스패처

- Room · Ktor는 자체적으로 메인 스레드를 피하므로 Repository에서 따로 `withContext(IO)`를 감쌀 필요가 없습니다.
- CPU 작업(파싱, 이미지 처리)만 `Dispatchers.Default`로 옮기고, 디스패처는 Koin으로 주입해 테스트에서 교체합니다.

---

## 9. Gradle 구성

### 9-1. settings.gradle.kts

```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "{app}"

include(":app")
include(":core:model", ":core:common", ":core:domain", ":core:navigation")
include(":core:designsystem", ":core:ui")
include(":core:database", ":core:datastore", ":core:network", ":core:data")
include(":feature:home:api", ":feature:home:impl")
// 기능마다 api / impl 추가
```

### 9-2. Version Catalog (gradle/libs.versions.toml)

```toml
[versions]
# 앱 · SDK 값 (convention plugin이 읽음)
projectApplicationId = "{base.package}"
projectVersionCode = "1"
projectVersionName = "1.0.0"
projectCompileSdkVersion = "x"
projectMinSdkVersion = "x"
projectTargetSdkVersion = "x"

agp = "x.y.z"
kotlin = "x.y.z"
ksp = "x.y.z"
composeBom = "x.y.z"
navigation3 = "x.y.z"
lifecycle = "x.y.z"
koin = "x.y.z"
ktor = "x.y.z"
room = "x.y.z"
datastore = "x.y.z"
workmanager = "x.y.z"
serialization = "x.y.z"
coroutines = "x.y.z"
turbine = "x.y.z"

[libraries]
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }

androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "navigation3" }
androidx-navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
androidx-lifecycle-viewmodel-navigation3 = { module = "androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }

# Koin은 BOM으로 버전을 맞춘다
koin-bom = { module = "io.insert-koin:koin-bom", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android" }
koin-compose = { module = "io.insert-koin:koin-compose" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel" }
koin-compose-navigation3 = { module = "io.insert-koin:koin-compose-navigation3" }
koin-androidx-workmanager = { module = "io.insert-koin:koin-androidx-workmanager" }
koin-test-junit4 = { module = "io.insert-koin:koin-test-junit4" }

ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-auth = { module = "io.ktor:ktor-client-auth", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }

room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

# build-logic이 compileOnly로 참조하는 Gradle 플러그인
android-gradlePlugin = { module = "com.android.tools.build:gradle", version.ref = "agp" }
compose-gradlePlugin = { module = "org.jetbrains.kotlin:compose-compiler-gradle-plugin", version.ref = "kotlin" }
kotlin-gradlePlugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
ksp-gradlePlugin = { module = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }
room-gradlePlugin = { module = "androidx.room:room-gradle-plugin", version.ref = "room" }
detekt-gradlePlugin = { module = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin", version.ref = "detekt" }
ktlint-gradlePlugin = { module = "org.jlleitschuh.gradle:ktlint-gradle", version.ref = "ktlintGradle" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }

# convention plugins (build-logic). 버전은 "unspecified"로 둔다
{app}-android-application = { id = "{app}.android.application", version = "unspecified" }
{app}-android-library = { id = "{app}.android.library", version = "unspecified" }
{app}-android-compose = { id = "{app}.android.compose", version = "unspecified" }
{app}-jvm-library = { id = "{app}.jvm.library", version = "unspecified" }
{app}-jvm-ktor = { id = "{app}.jvm.ktor", version = "unspecified" }
{app}-feature-api = { id = "{app}.feature.api", version = "unspecified" }
{app}-feature-impl = { id = "{app}.feature.impl", version = "unspecified" }
{app}-koin = { id = "{app}.koin", version = "unspecified" }
{app}-room = { id = "{app}.room", version = "unspecified" }

[bundles]
# convention plugin이 한 번에 추가하는 묶음
compose = ["androidx-compose-ui", "androidx-compose-ui-graphics", "androidx-compose-ui-tooling-preview", "androidx-compose-material3"]
compose-debug = ["androidx-compose-ui-tooling", "androidx-compose-ui-test-manifest"]
navigation3 = ["androidx-navigation3-runtime", "androidx-navigation3-ui", "androidx-lifecycle-viewmodel-navigation3"]
koin-compose = ["koin-compose", "koin-compose-viewmodel", "koin-compose-navigation3"]
ktor = ["ktor-client-core", "ktor-client-okhttp", "ktor-client-content-negotiation", "ktor-client-auth", "ktor-client-logging", "ktor-serialization-kotlinx-json"]
room = ["room-runtime", "room-ktx"]
test = ["junit", "kotlinx-coroutines-test", "turbine"]
```

- SDK · 앱 ID · 버전은 `project*` 항목 한 곳에서 관리하고, 모듈 `build.gradle.kts`에 숫자를 쓰지 않습니다.
- 루트 `build.gradle.kts`에는 convention plugin이 내부에서 적용하는 플러그인(`android-application`, `android-library`, `kotlin-jvm`, `kotlin-compose`, `kotlin-serialization`, `ksp`, `room`, `detekt`, `ktlint`)을 모두 `apply false`로 선언합니다. build-logic은 이 플러그인들을 `compileOnly`로만 참조하므로, 실행 시 클래스는 루트 빌드의 클래스패스에서 옵니다.

### 9-3. Convention Plugin 구성

| 플러그인 ID | 적용 대상 | 하는 일 |
|---|---|---|
| `{app}.android.application` | app | 앱 ID · SDK · 버전(`project*`), Java 17, BuildConfig, flavor, release 빌드 타입 · 서명(`keystore.properties`), 공통 테스트 의존성, ktlint · detekt |
| `{app}.android.library` | Android 라이브러리 모듈 전체 | compileSdk · minSdk, Java 17, flavor(app과 같은 차원), testInstrumentationRunner, 공통 테스트 의존성, ktlint · detekt |
| `{app}.android.compose` | Compose 쓰는 모듈 (app · 라이브러리 공용) | Compose 컴파일러, Compose BOM, `compose` · `compose-debug` 번들, ui-test-junit4 |
| `{app}.jvm.library` | core:model, common, domain | `kotlin("jvm")`, Java 17, 공통 테스트 의존성, ktlint · detekt |
| `{app}.jvm.ktor` | 네트워크 쓰는 모듈 (core:network) | kotlinx.serialization 플러그인, `ktor` 번들(OkHttp 엔진), serialization-json, ktor-client-mock(테스트) |
| `{app}.koin` | Koin 쓰는 모듈 | Koin BOM, koin-android, koin-test-junit4 |
| `{app}.room` | core:database | Room 플러그인, KSP(`room.generateKotlin`), 스키마 경로 `schemas/`, `room` 번들, room-compiler(`ksp`만), room-testing |
| `{app}.feature.api` | feature:*:api | android.library + serialization + navigation3-runtime **만** |
| `{app}.feature.impl` | feature:*:impl | android.library + compose + koin + navigation3 + core 공통 의존성 |

```kotlin
// build-logic/convention/src/main/kotlin/FeatureImplConventionPlugin.kt
class FeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply {
            apply("{app}.android.library")
            apply("{app}.android.compose")
            apply("{app}.koin")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }
        dependencies {
            add("implementation", project(":core:ui"))
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:navigation"))
            add("implementation", project(":core:data"))
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:model"))

            add("implementation", libs.findLibrary("androidx-navigation3-runtime").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("koin-compose").get())
            add("implementation", libs.findLibrary("koin-compose-viewmodel").get())
            add("implementation", libs.findLibrary("koin-compose-navigation3").get())
        }
    }
}

// build-logic/convention/src/main/kotlin/FeatureApiConventionPlugin.kt
class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply {
            apply("{app}.android.library")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }
        dependencies {
            add("api", libs.findLibrary("androidx-navigation3-runtime").get())
            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
        }
    }
}
```

`libs`는 convention 모듈에서 `extensions.getByType<VersionCatalogsExtension>().named("libs")`로 가져오는 확장 프로퍼티로 정의합니다.

- 공통 테스트 의존성은 `test` 번들(junit, kotlinx-coroutines-test, Turbine)입니다.
- `{app}.android.compose`는 `{app}.android.application` 또는 `{app}.android.library` **다음에** 적용합니다 (`CommonExtension`을 찾아 `buildFeatures.compose`를 켬).
- `{app}.jvm.ktor`는 Kotlin 플러그인을 적용하지 않습니다. `{app}.jvm.library` 또는 `{app}.android.library`와 함께 씁니다.
- Timber · Coil 같은 선택 라이브러리는 convention plugin에 넣지 않고 쓰는 모듈에서 직접 추가합니다.

**build-logic 구성**

```
build-logic/
├─ settings.gradle.kts           # 루트 version catalog 공유
├─ gradle.properties             # parallel · caching · configureondemand
└─ convention/
   ├─ build.gradle.kts           # kotlin-dsl, compileOnly 플러그인, gradlePlugin 등록
   └─ src/main/kotlin/
      ├─ *ConventionPlugin.kt    # 플러그인 (기본 패키지)
      └─ {base.package}/convention/   # 공통 설정 함수 (KotlinAndroid, Flavors, BuildTypes, ProjectExt …)
```

```kotlin
// build-logic/settings.gradle.kts
dependencyResolutionManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
    // ktlint-gradle은 Gradle Plugin Portal에만 있으므로 gradlePluginPortal()도 추가
    versionCatalogs {
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}
rootProject.name = "build-logic"
include(":convention")

// build-logic/convention/build.gradle.kts
plugins { `kotlin-dsl` }
group = "{base.package}.buildlogic"
kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.{app}.android.application.get().pluginId   // ID는 catalog 한 곳에서
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        // 나머지 플러그인도 같은 방식으로 등록
    }
}
```

### 9-4. 기능 모듈 build.gradle.kts

```kotlin
// feature/{feature}/api/build.gradle.kts
plugins { alias(libs.plugins.{app}.feature.api) }
android { namespace = "{base.package}.feature.{feature}.api" }

// feature/{feature}/impl/build.gradle.kts
plugins { alias(libs.plugins.{app}.feature.impl) }
android { namespace = "{base.package}.feature.{feature}.impl" }

dependencies {
    implementation(projects.feature.{feature}.api)
    // 이동 대상 기능의 api만 추가
    implementation(projects.feature.other.api)
}
```

### 9-5. 빌드 변형 (선택)

| flavor | 용도 |
|---|---|
| `dev` | 개발 서버 · 에뮬레이터 연결, 로그 활성화, 앱 ID 접미사 `.dev` |
| `prod` | 실서버, 로그 비활성화 |

- flavor는 `{app}.android.application`과 `{app}.android.library`가 **같은 차원(`env`) · 같은 이름**으로 선언합니다. 이름이 맞아야 app과 라이브러리 변형이 맞물리고, 모든 모듈에 `testDevDebugUnitTest` 같은 작업이 생깁니다.
- API 주소 · 키는 `buildConfigField`로 flavor마다 다르게 주입하고, 비밀 값은 `local.properties` 또는 CI 비밀 변수에서 읽습니다.

**release 서명**

서명 정보는 루트의 `keystore.properties`(커밋 금지)에 두고 `{app}.android.application`이 읽습니다. 파일이 없으면 서명 없이 빌드합니다(CI · 다른 개발자 환경).

```properties
# keystore.properties
storeFile=/절대/경로/release.jks
storePassword=…
keyAlias=…
keyPassword=…
```

---

## 10. 이름 · 패키지 규칙

### 10-1. 패키지

| 모듈 | 패키지 |
|---|---|
| core:{name} | `{base.package}.core.{name}` |
| feature:{feature}:api | `{base.package}.feature.{feature}.api` |
| feature:{feature}:impl | `{base.package}.feature.{feature}.impl` |

### 10-2. 클래스 · 파일

| 대상 | 규칙 | 예 |
|---|---|---|
| NavKey | `{Feature}Key`, `{Feature}{Sub}Key` | `HomeKey`, `ItemDetailKey` |
| Action | `{Feature}Action` | `HomeAction` |
| State | `{Feature}State` | `HomeState` |
| Event | `{Feature}Event` | `HomeEvent` |
| ViewModel | `{Feature}ViewModel` | `HomeViewModel` |
| Root 컴포저블 | `{Feature}Root` | `HomeRoot` |
| Screen 컴포저블 | `{Feature}Screen` | `HomeScreen` |
| Koin 모듈 변수 | `{feature}Module` | `homeModule` |
| Repository | `{Name}Repository` / `Default{Name}Repository` | `ItemRepository` |
| 유스케이스 | 동사 + 명사 + `UseCase` | `GetUpcomingItemsUseCase` |
| Entity / DTO / UI 모델 | `{Name}Entity` / `{Name}Dto` / `{Name}Ui` | `ItemEntity` |
| Action 이름 | 사용자 행동 과거형 · 명사형 | `SaveClicked`, `TitleChanged` |
| Event 이름 | 일어날 일 명령형 | `NavigateBack`, `ShowSnackbar` |

### 10-3. 공개 범위

- impl 모듈의 모든 클래스는 기본 `internal`. Koin 모듈 변수만 `public`.
- core:data의 Repository 구현은 `internal`, 인터페이스만 `public`.

---

## 11. 테스트 전략

| 대상 | 방식 | 도구 |
|---|---|---|
| core:domain 규칙 · 유스케이스 | JVM 단위 테스트 | JUnit, coroutines-test |
| ViewModel | Action 입력 → State · Event 검증 | coroutines-test, Turbine, Fake Repository |
| Repository | Fake DAO · Fake API 또는 in-memory Room | Room in-memory, Ktor MockEngine |
| DAO · Migration | 계측 테스트 | Room testing, MigrationTestHelper |
| Koin 구성 | 모든 모듈 `verify()` | koin-test |
| Screen | Preview + 필요 시 UI 테스트 | Compose UI Test |

```kotlin
@Test
fun `항목 클릭 시 상세 이동 이벤트`() = runTest {
    val vm = HomeViewModel(FakeItemRepository())
    vm.event.test {
        vm.onAction(HomeAction.ItemClicked("a1"))
        assertEquals(HomeEvent.NavigateToDetail("a1"), awaitItem())
    }
}
```

- Fake는 테스트 전용 모듈(`core:testing`)에 모아 여러 모듈에서 재사용합니다.
- `Dispatchers.setMain(StandardTestDispatcher())`를 공통 Rule로 둡니다.

---

## 12. 새 기능 추가 절차

1. `settings.gradle.kts`에 `:feature:{feature}:api`, `:feature:{feature}:impl` 추가
2. 각 모듈에 `build.gradle.kts` 작성 (`{app}.feature.api` / `{app}.feature.impl` 플러그인, namespace)
3. **api:** `{Feature}Key` 정의 (외부에서 들어올 화면만)
4. **impl:** `{Feature}Action`, `{Feature}State`, (필요 시) `{Feature}Event`
5. **impl:** `{Feature}ViewModel` — `MviViewModel` 상속, `onAction` 구현
6. **impl:** `{Feature}Screen.kt` — `internal {Feature}Root` + `private {Feature}Screen` + Preview
7. **impl:** `di/{Feature}Module.kt` — `viewModelOf`, `navigation<{Feature}Key>`
8. 이동할 다른 기능이 있으면 그 기능의 **api만** impl 의존성에 추가
9. **app:** `build.gradle.kts`에 impl 의존성 추가, `appModule`의 `includes`에 `{feature}Module` 추가
10. 이 기능으로 들어올 다른 impl에 `{feature}:api` 의존성 추가 후 `onNavigate({Feature}Key)` 호출
11. ViewModel 테스트 작성, Koin `verify()` 테스트 통과 확인

---

## 13. 자주 하는 실수

| 실수 | 결과 | 해결 |
|---|---|---|
| `rememberViewModelStoreNavEntryDecorator` 누락 | 닫힌 화면의 ViewModel이 계속 살아 있음 | NavDisplay의 `entryDecorators`에 추가 |
| impl이 다른 impl 참조 | 순환 참조, 빌드 느려짐 | api만 참조 |
| api에 ViewModel · Compose 코드 | api 변경이 잦아져 모든 의존 모듈 재빌드 | api에는 키만 |
| ViewModel에서 Navigator 직접 호출 | 테스트 어려움, 이동 로직 분산 | Event로 보내고 Root에서 이동 |
| 다이얼로그 표시를 Event로 처리 | 회전 후 다이얼로그 사라짐 | 계속 보여야 하면 State |
| Event를 `SharedFlow`로 보냄 | 수집자가 없을 때 이벤트 유실 | `Channel` + `receiveAsFlow()` |
| NavKey에 큰 객체 전달 | 상태 저장 시 크기 초과, 데이터 불일치 | ID만 전달하고 화면에서 조회 |
| Koin `verify()` 테스트 없음 | 의존성 누락이 실행 중 크래시로 발견됨 | 검증 테스트 필수 |
| Koin 실험적 API 버전 미고정 | 업데이트 시 API 변경으로 빌드 실패 | 버전 고정 후 릴리스 노트 확인 후 갱신 |
| Repository가 예외를 그대로 던짐 | ViewModel마다 try/catch 반복 | `Result`로 감싸서 반환 |
| 키에 `@Serializable` 누락 | 백스택 저장 · 복원 실패 | 모든 NavKey에 `@Serializable` |

---

## 참고

- Navigation 3: https://developer.android.com/guide/navigation/navigation-3
- Navigation 3 모듈화 레시피 (Koin): https://developer.android.com/guide/navigation/navigation-3/recipes/modular-koin
- Koin Navigation 3: https://insert-koin.io/docs/reference/koin-compose/navigation3
- 앱 아키텍처 가이드: https://developer.android.com/topic/architecture
- Ktor Client: https://ktor.io/docs/client-create-and-configure.html
