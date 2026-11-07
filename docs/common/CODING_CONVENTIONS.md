# 코딩 컨벤션 (범용)

> [ANDROID_ARCHITECTURE.md](ANDROID_ARCHITECTURE.md)가 **무엇을 어디에** 두는지 정한다면, 이 문서는 코드를 **어떻게** 쓰는지 정합니다.
> 자리표시자(`{Feature}` 등)는 아키텍처 문서와 같습니다.

## 목차

0. 적용 원칙
1. 자동 검사
2. Kotlin 공통
3. 파일 · 클래스 구성
4. 이름
5. MVI
6. Compose
7. 코루틴 · Flow
8. 에러 처리
9. 데이터 계층
10. 의존성 주입 (Koin)
11. 리소스
12. 주석
13. 테스트 코드
14. Git
15. 리뷰 체크리스트

규칙 앞의 표시는 강도를 뜻합니다.

| 표시 | 의미 |
|---|---|
| **[필수]** | 예외 없이 지킴. 리뷰에서 반드시 수정 |
| **[권장]** | 기본으로 지키되, 이유가 있으면 주석과 함께 예외 허용 |
| **[금지]** | 사용하지 않음 |
| **[허용]** | 기본 규칙의 예외로 명시적으로 허용되는 경우 |

---

## 0. 적용 원칙

1. **도구가 잡을 수 있는 규칙은 도구로 강제합니다.** 이 문서에는 도구가 못 잡는 규칙을 중심으로 적습니다.
2. 기존 코드와 이 문서가 다르면 **이 문서를 따르고**, 기존 코드는 해당 파일을 수정할 때 함께 고칩니다.
3. 규칙을 바꾸거나 추가할 때는 이 문서를 먼저 수정합니다.

---

## 1. 자동 검사

| 도구 | 역할 | 실행 시점 |
|---|---|---|
| ktlint | 포맷 (들여쓰기, 공백, import 순서) | 커밋 전 · CI |
| detekt | 코드 냄새, 복잡도, 금지 패턴 | CI |
| Android Lint | Android API 오용, 리소스 문제 | CI |
| Compose Lint (detekt 규칙 세트 또는 Slack compose-lints) | Compose 규칙 | CI |

- **[필수]** CI에서 위 검사가 실패하면 병합하지 않습니다.
- **[필수]** 포맷은 IDE 설정이 아니라 `.editorconfig`를 기준으로 합니다.
- **[필수]** ktlint · detekt는 build-logic의 `{app}.android.application` · `{app}.android.library` · `{app}.jvm.library`가 모든 모듈에 적용합니다. 모듈에서 따로 적용하지 않습니다.
  - ktlint: `org.jlleitschuh.gradle.ktlint` 플러그인, ktlint 버전은 catalog의 `ktlint`로 고정 (플러그인 기본값은 오래된 버전일 수 있음)
  - detekt: `io.gitlab.arturbosch.detekt`, 기본 규칙(`buildUponDefaultConfig`) 위에 `config/detekt/detekt.yml`의 값만 덮어씀
  - 명령: `./gradlew ktlintFormat` (자동 수정), `./gradlew ktlintCheck detekt` (검사)
- **[필수]** 기존 코드의 위반은 모듈별 `ktlint-baseline.xml` · `detekt-baseline.xml`로만 제외합니다. **새 코드의 위반을 baseline에 추가하지 않습니다.** 고친 위반은 baseline에서도 지웁니다 (`ktlintGenerateBaseline` · `detektBaseline`으로 다시 생성).
- **[필수]** `@Composable` 함수는 이름(PascalCase) · 길이 · 파라미터 수 규칙에서 제외하고, `@Preview`는 `MagicNumber` · `UnusedPrivateMember`에서 제외합니다 (`detekt.yml`).

```ini
# .editorconfig
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
indent_size = 4
max_line_length = 120
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
ktlint_code_style = ktlint_official
ktlint_function_naming_ignore_when_annotated_with = Composable
```

**detekt로 강제할 주요 금지 패턴** (`config/detekt/detekt.yml`)

| 규칙 | 이유 |
|---|---|
| `UnsafeCallOnNullableType` (`!!`) | 런타임 크래시 |
| `GlobalCoroutineUsage` (`GlobalScope`) | 수명 관리 불가 |
| `ForbiddenImport`: `android.util.Log` | 로그 래퍼 사용 강제 |
| `ElseCaseInsteadOfExhaustiveWhen` | sealed 분기 누락 방지 |
| `LongMethod`, `CyclomaticComplexMethod` | 함수 분리 유도 |

---

## 2. Kotlin 공통

### 2-1. 공개 범위

- **[필수]** 기본은 가장 좁게. 모듈 밖에서 쓰지 않으면 `internal`, 파일 밖에서 쓰지 않으면 `private`.
- **[필수]** feature impl 모듈은 Koin 모듈 변수 외에는 모두 `internal` 이하. `{Feature}Root`는 `internal`, `{Feature}Screen`은 `private`.
- **[권장]** core 모듈의 공개 API는 Explicit API 모드(`kotlin { explicitApi() }`)로 공개 범위를 명시.

### 2-2. null 안전

- **[금지]** `!!`. 대신 `?: return`, `?: error("이유")`, `requireNotNull(x) { "이유" }`.
- **[권장]** `lateinit`은 프레임워크가 주입하는 경우(테스트 `@Before` 등)에만.
- **[필수]** 플랫폼 타입(Java API 반환값)은 받는 즉시 nullable 여부를 명시.

### 2-3. 불변성

- **[필수]** `val` 우선. `var`는 지역 범위에서만.
- **[필수]** 공개 컬렉션 타입은 읽기 전용(`List`, `Map`). `MutableList`를 외부에 노출하지 않음.
- **[권장]** 상태 클래스는 `data class` + 모든 프로퍼티 `val`.

### 2-4. 분기

- **[필수]** `sealed` · `enum`에 대한 `when`은 **`else` 없이 모든 분기 나열.** 새 타입이 추가되면 컴파일 에러로 누락을 잡습니다.
- **[권장]** `if`, `when`, `try`는 식(expression)으로 사용.

```kotlin
// ✅
val label = when (status) {
    Status.ACTIVE -> "사용 중"
    Status.PAUSED -> "일시 중지"
}

// ❌ 새 Status가 추가돼도 알 수 없음
val label = when (status) {
    Status.ACTIVE -> "사용 중"
    else -> "기타"
}
```

### 2-5. 함수

- **[권장]** 함수 하나는 한 가지 일. 30줄을 넘으면 분리를 검토.
- **[권장]** 파라미터 4개 이상이면 이름 있는 인자로 호출.
- **[권장]** Boolean 파라미터로 동작을 바꾸는 함수보다 함수를 둘로 나눔.
- **[필수]** 기본값이 있는 파라미터는 뒤쪽에.

### 2-6. 상수 · 매직 넘버

- **[필수]** 의미 있는 숫자 · 문자열은 상수로. 파일 전용이면 `private const val`, 클래스 전용이면 `companion object` 안.
- **[필수]** 시간 단위는 `Duration`으로 (`5.seconds`, `30.minutes`).

### 2-7. 날짜 · 시간

- **[필수]** `java.time` 사용. `Date`, `Calendar` 금지.
- **[필수]** 날짜만 필요하면 `LocalDate`, 순간이면 `Instant`. 시간대 변환은 표시 직전에만.
- **[필수]** 현재 시각은 `Clock`을 주입받아 사용. 테스트에서 고정할 수 있게 합니다.

### 2-8. 로그

- **[필수]** `android.util.Log` 직접 사용 금지. 공통 로거(`core:common`의 `AppLogger` 등) 사용.
- **[금지]** 토큰, 이메일, 전화번호, 사용자 입력 원문 등 개인정보 로그.
- **[필수]** 릴리스 빌드에서 디버그 로그가 출력되지 않게 구성.

---

## 3. 파일 · 클래스 구성

### 3-1. 파일

- **[필수]** 공개 최상위 클래스 하나당 파일 하나. 파일명 = 클래스명.
- **[허용]** 작은 sealed 계층, 그 클래스 전용 private 헬퍼는 같은 파일에.
- **[필수]** `{Feature}Screen.kt`에 `{Feature}Root` + `{Feature}Screen` + Preview를 함께 둠 (5-5 참고).
- **[권장]** 확장 함수 모음은 `{대상}Ext.kt` (예: `LocalDateExt.kt`), 매퍼는 `{Name}Mapper.kt`.

### 3-2. 클래스 내부 순서

```kotlin
class Example(
    private val dependency: Dependency,      // 1. 생성자 프로퍼티
) : Base() {

    private val _state = MutableStateFlow(…)  // 2. 프로퍼티 (private backing → public)
    val state = _state.asStateFlow()

    init { … }                                // 3. init 블록

    override fun onAction(…) { … }            // 4. override 함수
    fun publicFunction() { … }                // 5. 공개 함수

    private fun helper() { … }                // 6. private 함수 (호출 순서대로)

    companion object {                        // 7. companion object (맨 아래)
        private const val TIMEOUT = 5_000L
    }
}
```

### 3-3. import

- **[금지]** 와일드카드 import (`import foo.*`).
- **[필수]** 사용하지 않는 import 제거 (ktlint가 처리).

---

## 4. 이름

### 4-1. 기본

| 대상 | 규칙 | 예 |
|---|---|---|
| 클래스 · 인터페이스 · 객체 | PascalCase | `ItemRepository` |
| 함수 · 변수 | camelCase | `loadItems`, `itemCount` |
| 상수 (`const val`, 최상위 불변 값) | UPPER_SNAKE_CASE | `MAX_RETRY` |
| 패키지 | 소문자, 언더스코어 없음 | `com.example.app.feature.home.impl` |
| Composable | PascalCase (명사) | `ItemCard`, `HomeScreen` |
| 테스트 함수 | 백틱 문장 (한국어 허용) | `` `저장 실패 시 스낵바 이벤트` `` |

### 4-2. 의미 규칙

| 대상 | 규칙 | 예 |
|---|---|---|
| Boolean | `is`, `has`, `can`, `should` 접두사 | `isLoading`, `hasError` |
| private 백킹 필드 | `_` 접두사 + 공개 이름 | `_state` → `state` |
| Flow를 반환하는 함수 | `observe` 접두사 | `observeItems()` |
| 한 번 가져오는 suspend 함수 | `get`, `fetch`, `load` | `getItem(id)` |
| 저장 · 삭제 | `save`, `upsert`, `delete` | `upsertItem(item)` |
| 변환 | `to{Target}` | `toModel()`, `toUi()` |
| 콜백 파라미터 | `on` + 이벤트 | `onClick`, `onItemSelected` |
| 컬렉션 | 복수형 | `items`, `selectedIds` |

### 4-3. 약어

- **[필수]** 약어도 단어처럼 PascalCase · camelCase: `UserId`, `HttpClient`, `parseJson` (✗ `UserID`, `HTTPClient`).
- **[금지]** 의미가 불분명한 축약: `mgr`, `tmp`, `cnt`, `btn`.

---

## 5. MVI

### 5-1. ViewModel

- **[필수]** `MviViewModel<S, A, E>`를 상속합니다.
- **[필수]** **`onAction(action: A)`가 화면에서 들어오는 유일한 진입점입니다.** 화면용 공개 함수를 따로 만들지 않습니다.
- **[필수]** 공개 멤버는 `state`, `event`, `onAction` 세 가지뿐입니다.
- **[필수]** `onAction`의 `when`은 `else` 없이 모든 Action을 나열하고, 각 분기는 한 줄 위임 또는 짧은 처리만 합니다. 긴 로직은 `private fun handle{Action}()`으로 분리합니다.
- **[금지]** ViewModel에서 `Context`, `Navigator`, `Activity`, Compose 타입(`Color`, `Dp`, `@Composable`) 참조.
- **[금지]** ViewModel에서 다른 ViewModel 참조.
- **[필수]** 데이터 구독은 `init`에서 시작하거나 `stateIn(WhileSubscribed(5_000))`으로 구성합니다.
- **[필수]** 같은 작업이 겹치면 안 되는 경우(검색, 저장) `Job`을 보관하고 이전 작업을 취소합니다.

```kotlin
internal class ItemEditViewModel(
    private val saveItem: SaveItemUseCase,
) : MviViewModel<ItemEditState, ItemEditAction, ItemEditEvent>(ItemEditState()) {

    private var saveJob: Job? = null

    override fun onAction(action: ItemEditAction) {
        when (action) {
            is ItemEditAction.TitleChanged -> setState { copy(title = action.value) }
            is ItemEditAction.DateSelected -> setState { copy(date = action.date) }
            ItemEditAction.SaveClicked -> handleSave()
            ItemEditAction.BackClicked -> sendEvent(ItemEditEvent.NavigateBack)
        }
    }

    private fun handleSave() {
        if (saveJob?.isActive == true) return
        saveJob = viewModelScope.launch {
            setState { copy(isSaving = true) }
            saveItem(currentState.toInput())
                .onSuccess { sendEvent(ItemEditEvent.NavigateBack) }
                .onFailure { sendEvent(ItemEditEvent.ShowError(it.toUiText())) }
            setState { copy(isSaving = false) }
        }
    }
}
```

### 5-2. Action

- **[필수]** `sealed interface {Feature}Action`.
- **[필수]** 인자 없으면 `data object`, 있으면 `data class`.
- **[필수]** 이름은 **사용자가 한 일**: `SaveClicked`, `TitleChanged`, `ItemSwiped`, `RetryClicked`.
- **[금지]** 결과 · 명령형 이름: `Save`, `NavigateToDetail` (이건 Event).
- **[권장]** 화면 진입 시 1회 로딩은 `ScreenEntered` 같은 Action을 만들지 않고 ViewModel의 `init`에서 처리합니다. 인자가 필요한 초기화(NavKey의 ID 등)는 ViewModel 생성 파라미터로 받습니다.

### 5-3. State

- **[필수]** `data class {Feature}State`, 모든 프로퍼티 `val`, **모든 프로퍼티에 기본값.**
- **[필수]** 컬렉션은 `List` · `Set` · `Map` (가능하면 `kotlinx.collections.immutable`의 `ImmutableList`).
- **[금지]** State 안에 `Flow`, `LiveData`, `MutableState`, 람다, Android 객체.
- **[권장]** 도메인 모델 대신 화면용 UI 모델(`{Name}Ui`)을 담습니다.
- **[권장]** 파생 값은 State에 중복 저장하지 말고 계산 프로퍼티로: `val canSave get() = title.isNotBlank() && !isSaving`.
- **[필수]** 화면에 계속 보여야 하는 것(다이얼로그 표시 여부, 선택 상태, 입력값)은 State에 둡니다.

```kotlin
internal data class ItemEditState(
    val title: String = "",
    val date: LocalDate? = null,
    val isSaving: Boolean = false,
    val showDiscardDialog: Boolean = false,
) {
    val canSave: Boolean get() = title.isNotBlank() && date != null && !isSaving
}
```

### 5-4. Event

- **[필수]** `sealed interface {Feature}Event`, 인자 없으면 `data object`, 있으면 `data class`.
- **[필수]** 이름은 **일어날 일(명령형)**: `NavigateBack`, `NavigateToDetail`, `ShowSnackbar`, `OpenImagePicker`, `ShowInterstitialAd`.
- **[필수]** 한 번만 실행되어야 하는 효과만 Event로. 계속 보여야 하는 것은 State.
- **[필수]** Event가 없는 화면은 `Event` 파일을 만들지 않고 타입 인자에 `Nothing`.
- **[필수]** Event는 `Channel` 기반으로 보내고 Root에서 `ObserveEvents`로 수집.

### 5-5. Root · Screen

화면 파일 하나에 **상태를 가진 Root**와 **상태 없는 Screen**을 함께 둡니다.

```kotlin
// {Feature}Screen.kt

@Composable
internal fun {Feature}Root(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: {Feature}ViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is {Feature}Event.NavigateToDetail -> onNavigate({Feature}DetailKey(event.id))
            {Feature}Event.NavigateBack -> onBack()
        }
    }

    {Feature}Screen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun {Feature}Screen(
    state: {Feature}State,
    onAction: ({Feature}Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    // State만 보고 그리고, 사용자 입력은 onAction으로만 올린다.
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun {Feature}ScreenPreview() {
    AppTheme {
        {Feature}Screen(state = {Feature}State(isLoading = false), onAction = {})
    }
}
```

| 대상 | 공개 범위 | 하는 일 | 하지 않는 일 |
|---|---|---|---|
| `{Feature}Root` | `internal` | ViewModel 주입, `state` 수집, `event` 처리, 이동 콜백 연결, Screen 호출 | 레이아웃 그리기 |
| `{Feature}Screen` | `private` | State를 그리고 Action을 올림 | ViewModel · Koin · Navigator 참조 |
| Preview | `private` | Screen을 여러 State로 미리보기 | – |

- **[필수]** Root의 공개 범위는 `internal`. 같은 모듈의 Koin `navigation<Key>` 블록에서만 호출합니다.
- **[필수]** Screen의 공개 범위는 `private`. **같은 파일의 Root와 Preview에서만** 호출합니다.
- **[필수]** Root는 연결만 합니다. `Scaffold`, `Column` 같은 레이아웃은 Screen 안에 둡니다.
- **[필수]** Screen은 `state`, `onAction`, `modifier`를 받습니다. 스낵바 상태처럼 Root에서 만든 UI 객체가 필요할 때만 파라미터를 추가합니다.
- **[필수]** Root의 Event 처리 `when`도 `else` 없이 모든 분기 나열.
- **[금지]** Screen 이하 컴포저블에 ViewModel 전달.
- **[권장]** Screen이 길어지면 `components/` 폴더의 `internal` 컴포저블로 나눕니다. 이 컴포저블들은 State의 일부와 콜백만 받습니다.
- **[필수]** 이름은 `Root`를 씁니다. Navigation 3에서 "route"는 목적지(NavKey)를 가리키는 말이라 `Route`와 혼동을 피합니다.

> **Screen을 `private`으로 둘 때의 테스트:** 같은 파일의 Preview가 화면 상태별 견본 역할을 합니다. 스크린샷 테스트는 Preview를 자동으로 찾아 촬영하는 방식(예: Roborazzi + ComposablePreviewScanner의 private Preview 포함 옵션)으로 진행하고, 상호작용 UI 테스트가 꼭 필요한 화면은 Root에 테스트용 Koin 모듈(Fake Repository)을 주입해 검증합니다. 자세한 내용은 [TESTING.md 5-10, 5-11](TESTING.md)을 참고하세요.

### 5-6. 복잡한 화면

- **[권장]** State 계산이 복잡하면 순수 함수로 분리하고 단위 테스트합니다.

```kotlin
internal object ItemEditReducer {
    fun reduce(state: ItemEditState, action: ItemEditAction): ItemEditState = when (action) {
        is ItemEditAction.TitleChanged -> state.copy(title = action.value.take(MAX_TITLE))
        …
    }
}
```

---

## 6. Compose

### 6-1. 파라미터 순서

```kotlin
@Composable
fun ItemCard(
    item: ItemUi,                         // 1. 필수 데이터
    onClick: () -> Unit,                  // 2. 필수 콜백
    modifier: Modifier = Modifier,        // 3. modifier (첫 번째 선택 파라미터)
    showBadge: Boolean = true,            // 4. 기타 선택 파라미터
    trailing: @Composable () -> Unit = {},// 5. 슬롯 · content 람다는 마지막
)
```

- **[필수]** 화면에 무언가를 그리는 컴포저블은 `modifier: Modifier = Modifier`를 받습니다.
- **[필수]** 받은 `modifier`는 **가장 바깥 요소에 한 번만** 적용합니다. 다른 자식에 재사용하지 않습니다.
- **[금지]** `modifier` 기본값으로 `Modifier.padding(…)` 등 지정.

### 6-2. 상태

- **[필수]** 상태 끌어올리기: 재사용 컴포저블은 값과 콜백을 받고, 내부에 비즈니스 상태를 두지 않습니다.
- **[필수]** `remember`는 계산 비용이 있거나 재구성 간 유지해야 할 때만. 키가 있으면 `remember(key)`.
- **[필수]** Flow 수집은 `collectAsStateWithLifecycle()`.
- **[필수]** 저장해야 하는 UI 상태(스크롤, 입력 중 텍스트)는 `rememberSaveable`.

### 6-3. 부수 효과

- **[필수]** `LaunchedEffect` 키는 효과가 다시 실행되어야 하는 값으로. `LaunchedEffect(Unit)`은 정말 1회만 필요할 때만.
- **[금지]** 컴포저블 본문에서 직접 suspend 호출, 상태 변경, 로그 등 부수 효과.

### 6-4. 성능 · 안정성

- **[권장]** UI 모델에 `@Immutable` 또는 `@Stable`. 리스트는 `ImmutableList`.
- **[필수]** `LazyColumn` 항목에 안정적인 `key` 지정.
- **[권장]** 자주 바뀌는 값(스크롤 위치 등)은 람다로 전달해 읽는 시점을 늦춤.

### 6-5. 디자인 시스템

- **[금지]** 하드코딩된 색(`Color(0xFF…)`), 글꼴 크기, 모서리 값. 디자인 시스템 토큰만 사용.
- **[권장]** 여백은 디자인 시스템의 spacing 토큰 사용.
- **[필수]** 새 공통 컴포넌트는 `core:designsystem`에 추가하고, 기능 모듈에서 복제하지 않습니다.

### 6-6. Preview

- **[필수]** 모든 `{Feature}Screen`에 Preview. 최소 기본 상태 1개 + 라이트/다크.
- **[권장]** 로딩 · 빈 상태 · 에러 상태 Preview를 `PreviewParameterProvider`로.
- **[필수]** Preview 함수는 `private`, 이름은 `{Name}Preview`.

---

## 7. 코루틴 · Flow

- **[금지]** `GlobalScope`, `runBlocking`(테스트 제외).
- **[필수]** ViewModel은 `viewModelScope`, 앱 수명 작업은 주입받은 `applicationScope`.
- **[필수]** 디스패처는 주입받아 사용 (`Dispatchers.IO` 직접 참조 금지). 테스트에서 교체하기 위함.
- **[필수]** Room · Ktor 호출을 `withContext(IO)`로 다시 감싸지 않습니다 (이미 메인 스레드를 피함).
- **[필수]** `suspend` 함수는 어느 스레드에서 불러도 안전해야 합니다 (main-safe).
- **[필수]** **`CancellationException`을 삼키지 않습니다.** `runCatching`은 취소 예외까지 잡으므로 suspend 블록에는 공통 헬퍼를 사용합니다.

```kotlin
// core:common
suspend inline fun <T> suspendRunCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}
```

- **[권장]** 화면용 Flow는 `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 초기값)`.
- **[필수]** Flow 체인에는 `catch`를 두어 예외로 스트림이 끊기지 않게 합니다.

---

## 8. 에러 처리

- **[필수]** Repository · UseCase의 1회성 작업은 `Result<T>` 반환. 예외를 ViewModel까지 던지지 않습니다.
- **[권장]** 화면에서 구분해야 하는 실패는 도메인 에러 타입으로 정의합니다.

```kotlin
sealed interface DataError {
    data object Network : DataError
    data object Unauthorized : DataError
    data object NotFound : DataError
    data class Unknown(val cause: Throwable) : DataError
}
```

- **[필수]** 사용자에게 보여줄 문구 변환은 UI 계층(ViewModel의 `toUiText()` 매퍼)에서. Data 계층은 문구를 모릅니다.
- **[필수]** 사용자에게 보이는 에러에는 다음 행동(재시도 등)을 함께 제공합니다.
- **[금지]** 빈 `catch` 블록. 무시해야 한다면 이유를 주석으로.

---

## 9. 데이터 계층

### 9-1. Repository

- **[필수]** 스트림은 `observe…(): Flow<T>`, 1회성은 `suspend … : Result<T>`.
- **[필수]** Repository는 도메인 모델(`core:model`)만 반환. Entity · DTO를 밖으로 노출하지 않습니다.
- **[필수]** 구현 클래스는 `internal`, 이름은 `Default{Name}Repository` (또는 저장소별 `Offline{Name}Repository`).

### 9-2. 매퍼

- **[필수]** 확장 함수로 작성, `{Name}Mapper.kt`에 모음: `ItemEntity.toModel()`, `Item.toEntity()`, `ItemDto.toEntity()`.
- **[금지]** 매퍼 안에서 I/O, 현재 시각 조회 등 부수 효과.

### 9-3. Room

| 대상 | 규칙 |
|---|---|
| 테이블 · 컬럼명 | snake_case, `@ColumnInfo(name = …)` 명시 |
| DAO 함수 | `observe…`(Flow), `get…`(suspend), `upsert…`, `delete…` |
| 마이그레이션 | 스키마 변경 시 반드시 Migration 작성 + 스키마 JSON 커밋 |
| 금지 | `fallbackToDestructiveMigration()` (릴리스 빌드) |

### 9-4. 네트워크 DTO

- **[필수]** `@Serializable`, 서버 필드명은 `@SerialName`으로 명시.
- **[필수]** 서버가 생략할 수 있는 필드는 nullable 또는 기본값.
- **[금지]** DTO를 UI · 도메인 계층에서 사용.

---

## 10. 의존성 주입 (Koin)

- **[필수]** 생성자 주입만 사용합니다.
- **[금지]** 일반 클래스에서 `KoinComponent` + `get()` / `inject()` (서비스 로케이터). 예외는 Koin이 생성하지 않는 Android 진입점(위젯 리시버 등)뿐.
- **[권장]** 정의는 `singleOf(::X)`, `factoryOf(::X)`, `viewModelOf(::X)` 형태로. 인터페이스 바인딩은 `bind`.
- **[필수]** 모듈을 추가 · 수정하면 Koin `verify()` 테스트가 통과해야 합니다.
- **[필수]** 실험적 API(`@KoinExperimentalAPI`)는 `@OptIn`을 모듈 정의 파일에만 둡니다.

---

## 11. 리소스

- **[금지]** 사용자에게 보이는 문자열 하드코딩. 모두 `strings.xml`.
- **[필수]** 문자열 ID: `{feature}_{screen}_{purpose}` → `home_empty_title`, `item_edit_save_button`.
- **[필수]** 공통 문자열(확인, 취소, 재시도)은 `core:designsystem` 또는 `core:ui`의 `common_*`.
- **[필수]** 개수 · 복수형은 `plurals`, 숫자 · 날짜 포맷은 리소스 포맷 인자로.
- **[필수]** 아이콘 · 이미지에 `contentDescription` 지정. 장식용이면 `null`.
- **[권장]** ViewModel에서 문구가 필요하면 `UiText`(리소스 ID 또는 문자열을 감싼 sealed 타입)로 전달.

---

## 12. 주석

- **[필수]** 주석은 **왜**를 적습니다. 무엇을 하는지는 코드로 드러냅니다.
- **[필수]** core 모듈의 공개 API에는 KDoc.
- **[필수]** TODO 형식: `// TODO(작성자): 내용 [이슈 번호]`. 이슈 번호 없는 TODO는 남기지 않습니다.
- **[금지]** 주석 처리된 코드 커밋.

---

## 13. 테스트 코드

테스트 종류, 모듈 구성, 템플릿은 [TESTING.md](TESTING.md)에서 다루고, 여기에는 코드 작성 규칙만 둡니다.

- **[필수]** 테스트 이름은 백틱 문장으로 **조건과 기대 결과**를 적습니다: `` `제목이 비어 있으면 저장 버튼 비활성` ``.
- **[필수]** 본문은 준비(given) · 실행(when) · 검증(then) 순서로, 빈 줄로 구분합니다.
- **[필수]** 테스트 하나는 동작 하나만 검증합니다.
- **[권장]** Mock 라이브러리보다 **Fake 구현**(인터페이스를 직접 구현한 테스트용 클래스)을 우선합니다. Fake는 `core:testing`에 모아 재사용합니다.
- **[필수]** 현재 시각 · 디스패처 · UUID 생성기는 주입받아 테스트에서 고정합니다.
- **[금지]** `Thread.sleep`, 실제 네트워크 호출, 테스트 간 공유 상태.
- **[필수]** 테스트 파일은 대상과 같은 패키지, 이름은 `{대상}Test.kt`.

## 14. Git

브랜치, 커밋 · PR 제목(`[Type] scope: 요약`), 리뷰 코멘트, 머지, 버전 · 릴리스 규칙은 [GIT_CONVENTIONS.md](GIT_CONVENTIONS.md)를 따릅니다.

---

## 15. 리뷰 체크리스트

**구조**
- [ ] impl이 다른 impl을 참조하지 않는다
- [ ] api에 NavKey 외의 코드가 없다
- [ ] 공개 범위가 가능한 한 좁다 (`internal` · `private`)

**MVI**
- [ ] ViewModel 공개 멤버는 `state`, `event`, `onAction`뿐이다
- [ ] Action · Event는 `sealed interface`, 인자 없으면 `data object`
- [ ] `when`에 `else`가 없다 (sealed · enum)
- [ ] 계속 보여야 하는 것은 State, 한 번만 일어나는 것은 Event
- [ ] ViewModel이 Context · Navigator · Compose 타입을 모른다
- [ ] `{Feature}Root`는 `internal`, `{Feature}Screen`은 `private`이며 같은 파일에 있다
- [ ] Root에는 레이아웃이 없고, Screen은 ViewModel을 모른다

**Compose**
- [ ] `modifier` 파라미터가 있고 가장 바깥에 한 번만 적용된다
- [ ] 하드코딩된 색 · 문자열이 없다
- [ ] Screen Preview(라이트 · 다크)가 있다
- [ ] LazyColumn 항목에 `key`가 있다

**안정성**
- [ ] `!!`, `GlobalScope`가 없다
- [ ] `CancellationException`을 삼키지 않는다
- [ ] Repository가 `Result`를 반환하고 예외를 던지지 않는다
- [ ] 개인정보를 로그에 남기지 않는다

**테스트**
- [ ] ViewModel의 주요 Action에 대한 테스트가 있다
- [ ] Koin `verify()` 테스트가 통과한다
- [ ] DB 스키마 변경 시 Migration과 테스트가 있다
