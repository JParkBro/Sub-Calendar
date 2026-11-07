# 테스트 가이드 (범용)

> [ANDROID_ARCHITECTURE.md](ANDROID_ARCHITECTURE.md)의 구조(MVI · api/impl · Koin · Ktor · Room)를 기준으로, 무엇을 어떤 방식으로 테스트하는지 정합니다.
> 테스트 코드 작성 규칙(이름, given-when-then, Fake 우선)은 [CODING_CONVENTIONS.md 13장](CODING_CONVENTIONS.md#13-테스트-코드)을 따릅니다.

## 목차

1. 테스트 종류와 우선순위
2. 실행 위치와 시점
3. 테스트 모듈 구성 (`core:testing`)
4. 계층별 테스트 대상
5. 템플릿
6. CI 구성
7. 기준과 체크리스트

---

## 1. 테스트 종류와 우선순위

```
          ▲ 느림 · 적게
         ╱ ╲      E2E (실제 기기에서 핵심 흐름)
        ╱───╲     UI · 스크린샷 (화면 단위)
       ╱─────╲    통합 (DB, 네트워크 연결부)
      ╱───────╲   단위 (규칙, ViewModel, 매퍼)
     ▔▔▔▔▔▔▔▔▔▔▔  ▼ 빠름 · 많이
```

| 우선순위 | 종류 | 검사 대상 | 도구 |
|---|---|---|---|
| 1 | 도메인 단위 테스트 | 계산 규칙, 유스케이스 | JUnit, coroutines-test |
| 2 | ViewModel 테스트 | Action → State · Event | coroutines-test, Turbine |
| 3 | 구성 검증 | Koin 의존성 그래프 | koin-test `verify()` |
| 3 | DB 마이그레이션 테스트 | 앱 업데이트 시 데이터 유지 | Room MigrationTestHelper |
| 4 | Repository · DAO 통합 테스트 | DB 쿼리, API 파싱, 매핑 | Room in-memory, Ktor MockEngine, Robolectric |
| 5 | 스크린샷 테스트 | 화면이 의도치 않게 바뀌지 않았는지 | Roborazzi + ComposablePreviewScanner |
| 6 | UI 상호작용 테스트 | 복잡한 입력 · 흐름이 있는 화면 | Compose UI Test |
| 7 | E2E 테스트 | 가입 · 핵심 기능 같은 필수 흐름 2~3개 | Maestro |
| 선택 | 성능 테스트 | 시작 시간, 스크롤 | Macrobenchmark, Baseline Profile |

**원칙**
- 버그가 생기면 치명적이고 작성이 쉬운 것(도메인 규칙, ViewModel)부터 채웁니다.
- 화면 단위 UI 테스트는 스크린샷 테스트와 ViewModel 테스트가 대부분을 대신하므로, 꼭 필요한 화면에만 둡니다.

---

## 2. 실행 위치와 시점

### 2-1. 소스 세트

| 소스 세트 | 실행 환경 | 넣는 테스트 |
|---|---|---|
| `src/test` | PC의 JVM (+ Robolectric) | 단위, ViewModel, Repository, DAO(Robolectric), 스크린샷, Koin verify |
| `src/androidTest` | 기기 · 에뮬레이터 | 마이그레이션, 기기 의존 UI 테스트 |
| 별도 모듈 `:benchmark` | 실제 기기 | 성능 |
| 저장소 `e2e/` 폴더 | 기기 · 에뮬레이터 | Maestro 흐름 (YAML) |

> 가능하면 `src/test`(JVM)에 둡니다. 기기가 필요 없어 빠르고 CI 비용이 적습니다.

### 2-2. 실행 시점

| 시점 | 실행 |
|---|---|
| 커밋 전 (로컬) | 변경한 모듈의 단위 테스트, ktlint |
| PR마다 (CI) | 전체 JVM 테스트, Koin verify, 스크린샷 비교, detekt, Lint |
| `main` 병합 후 · 매일 밤 | 기기 테스트(마이그레이션 등), E2E |
| 출시 전 | 전체 + E2E + 성능 |

---

## 3. 테스트 모듈 구성 (`core:testing`)

여러 모듈에서 재사용하는 테스트 도구를 한 곳에 모읍니다.

```
core/testing/src/main/kotlin/{base.package}/core/testing/
├─ rule/MainDispatcherRule.kt      # Dispatchers.Main 교체
├─ fake/Fake{Name}Repository.kt    # Repository Fake 구현
├─ fake/FakeClock.kt               # 시각 고정
├─ fake/FakeIdGenerator.kt         # UUID 고정
├─ data/Test{Name}s.kt             # 테스트 데이터 빌더
└─ di/testModules.kt               # Root UI 테스트용 Koin 모듈
```

- 각 모듈은 `testImplementation(projects.core.testing)`으로 사용합니다.
- **Fake는 실제 인터페이스를 구현**하므로 인터페이스가 바뀌면 컴파일 에러로 바로 드러납니다. Mock 라이브러리는 Fake로 표현하기 어려운 경우(호출 횟수 검증 등)에만 씁니다.

---

## 4. 계층별 테스트 대상

| 계층 | 반드시 테스트 | 테스트하지 않음 |
|---|---|---|
| core:domain | 모든 계산 규칙, 경계값(0, 음수, 윤년, 월말) | – |
| core:data (Repository) | 매핑, 오프라인 우선 흐름, 에러 → `Result` 변환 | 라이브러리 자체 동작 |
| core:database (DAO) | 정렬 · 필터가 있는 쿼리, soft delete 제외 조건 | 단순 insert/get |
| core:database (Migration) | **모든 버전 간 마이그레이션** | – |
| core:network | DTO 파싱(필드 누락 · 추가), 에러 응답 처리 | Ktor 내부 |
| feature impl (ViewModel) | 주요 Action마다 State 변화와 Event | 단순 setter성 Action 하나하나 |
| feature impl (Screen) | Preview 기반 스크린샷 | 레이아웃 픽셀 단위 단언 |
| app | Koin `verify()`, 시작 목적지 결정 로직 | – |

---

## 5. 템플릿

### 5-1. MainDispatcherRule

```kotlin
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

### 5-2. Fake Repository

```kotlin
class FakeItemRepository : ItemRepository {
    private val items = MutableStateFlow<List<Item>>(emptyList())
    var saveResult: Result<Unit> = Result.success(Unit)

    fun emit(vararg values: Item) { items.value = values.toList() }

    override fun observeItems(): Flow<List<Item>> = items
    override fun observeItem(id: String): Flow<Item?> = items.map { list -> list.find { it.id == id } }
    override suspend fun save(item: Item): Result<Unit> = saveResult.onSuccess {
        items.update { list -> list.filterNot { it.id == item.id } + item }
    }
    override suspend fun refresh(): Result<Unit> = Result.success(Unit)
}
```

### 5-3. 테스트 데이터 빌더

```kotlin
fun testItem(
    id: String = "item-1",
    title: String = "테스트 항목",
    date: LocalDate = LocalDate.of(2026, 1, 1),
) = Item(id = id, title = title, date = date /* 나머지는 기본값 */)
```

테스트마다 필요한 값만 바꿔 씁니다: `testItem(title = "")`.

### 5-4. 도메인 규칙

```kotlin
class DaysBetweenTest {

    @Test
    fun `시작일을 1일로 세면 당일은 1일`() {
        val result = daysSince(start = LocalDate.of(2026, 1, 1), today = LocalDate.of(2026, 1, 1), countFirstDay = true)

        assertEquals(1, result)
    }

    @Test
    fun `윤년 2월 29일을 넘어가는 계산`() { … }
}
```

### 5-5. ViewModel

```kotlin
class ItemEditViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeItemRepository()
    private fun createViewModel() = ItemEditViewModel(SaveItemUseCase(repository))

    @Test
    fun `제목을 입력하면 State에 반영`() = runTest {
        val vm = createViewModel()

        vm.onAction(ItemEditAction.TitleChanged("여행"))

        assertEquals("여행", vm.state.value.title)
    }

    @Test
    fun `저장 성공 시 뒤로 가기 이벤트`() = runTest {
        val vm = createViewModel()
        vm.onAction(ItemEditAction.TitleChanged("여행"))
        vm.onAction(ItemEditAction.DateSelected(LocalDate.of(2026, 5, 1)))

        vm.event.test {
            vm.onAction(ItemEditAction.SaveClicked)

            assertEquals(ItemEditEvent.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `저장 실패 시 에러 이벤트, 저장 중 상태 해제`() = runTest {
        repository.saveResult = Result.failure(IOException())
        val vm = createViewModel()
        vm.onAction(ItemEditAction.TitleChanged("여행"))
        vm.onAction(ItemEditAction.DateSelected(LocalDate.of(2026, 5, 1)))

        vm.event.test {
            vm.onAction(ItemEditAction.SaveClicked)

            assertIs<ItemEditEvent.ShowError>(awaitItem())
        }
        assertFalse(vm.state.value.isSaving)
    }
}
```

**ViewModel 테스트 규칙**
- Action을 넣고 **State 값** 또는 **Event**를 검증합니다. private 함수를 직접 테스트하지 않습니다.
- Event는 Turbine의 `test { }`로, State는 `state.value` 또는 `state.test { }`로 확인합니다.
- `init`에서 구독하는 데이터는 Fake에 값을 먼저 넣고 ViewModel을 만듭니다.

### 5-6. Repository + Room (Robolectric)

```kotlin
@RunWith(AndroidJUnit4::class)
class DefaultItemRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DefaultItemRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DefaultItemRepository(db.itemDao(), FakeRemoteSyncDataSource(), FakeClock())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `삭제된 항목은 목록에 나오지 않음`() = runTest {
        repository.save(testItem(id = "a"))
        repository.delete("a")

        assertTrue(repository.observeItems().first().isEmpty())
    }
}
```

### 5-7. 네트워크 (Ktor MockEngine)

```kotlin
class ItemApiTest {

    private fun clientReturning(json: String, status: HttpStatusCode = HttpStatusCode.OK) = HttpClient(MockEngine {
        respond(json, status, headersOf(HttpHeaders.ContentType, "application/json"))
    }) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Test
    fun `서버가 모르는 필드를 보내도 파싱 성공`() = runTest {
        val api = ItemApi(clientReturning("""[{"id":"a","title":"t","newField":1}]"""))

        assertEquals("a", api.getItems().single().id)
    }
}
```

### 5-8. DB 마이그레이션 (androidTest)

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    @Test
    fun migrate1To2_기존데이터유지() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("INSERT INTO item (id, title) VALUES ('a', '기존')")
            close()
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2)

        db.query("SELECT title FROM item WHERE id = 'a'").use {
            assertTrue(it.moveToFirst())
            assertEquals("기존", it.getString(0))
        }
    }

    @Test
    fun 모든마이그레이션_처음부터최신까지() {
        helper.createDatabase(DB_NAME, 1).close()
        helper.runMigrationsAndValidate(DB_NAME, LATEST_VERSION, true, *ALL_MIGRATIONS)
    }
}
```

- Room 스키마 JSON(`schemas/`)을 반드시 커밋합니다. 마이그레이션 테스트가 이 파일을 기준으로 동작합니다.

### 5-9. Koin 구성 검증

```kotlin
class KoinModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `모든 Koin 모듈의 의존성이 해결됨`() {
        appModule.verify(extraTypes = listOf(Context::class, SavedStateHandle::class))
    }
}
```

- `verify()`는 정의된 클래스의 **생성자 타입**을 검사합니다. `single { X(값) }`처럼 값을 직접 넣거나 `parametersOf`로 받는 타입(화면 키 등)은 `extraTypes`에 넣습니다.
- 기능 내부(`internal`) 키는 app에서 참조할 수 없으므로 `Class.forName("…Key").kotlin`으로 넣습니다. 키 이름을 바꾸면 이 테스트도 함께 고칩니다.
- `navigation<Key> { … }` 안에서 `get()`으로 꺼내는 의존성은 람다라 검사되지 않습니다. 실행 확인으로 보완합니다.

### 5-10. 스크린샷 테스트 (private Screen 대응)

`{Feature}Screen`이 `private`이므로 테스트에서 직접 호출하지 않고, **같은 파일의 Preview를 자동으로 찾아 촬영**합니다.

```kotlin
// build.gradle.kts (스크린샷을 찍을 모듈 또는 app)
plugins { id("io.github.takahirom.roborazzi") }

roborazzi {
    generateComposePreviewRobolectricTests {
        enable = true
        packages = listOf("{base.package}")
        includePrivatePreviews = true   // private Preview 포함
    }
}
```

| 명령 | 용도 |
|---|---|
| `./gradlew recordRoborazziDevDebug` | 기준 이미지 생성 · 갱신 (의도한 UI 변경 후) |
| `./gradlew verifyRoborazziDevDebug` | 기준 이미지와 비교, 다르면 실패 (CI) |
| `./gradlew compareRoborazziDevDebug` | 차이 이미지 생성 (리뷰용) |

- Preview는 라이트 · 다크, 로딩 · 빈 상태 · 에러 상태를 만들어 두면 그대로 스크린샷 케이스가 됩니다.
- 테마가 여러 개인 앱은 디자인 시스템 컴포넌트 Preview에 테마별 케이스를 두면, 테마 토큰 변경의 영향이 한 번에 드러납니다.
- 플러그인 옵션 이름은 Roborazzi 버전에 따라 다를 수 있으니 적용 시 문서를 확인합니다.

### 5-11. Root UI 상호작용 테스트

상호작용 검증이 꼭 필요한 화면만, **Root를 띄우고 테스트용 Koin 모듈로 Fake를 주입**합니다.

```kotlin
@RunWith(AndroidJUnit4::class)
class ItemEditRootTest {

    @get:Rule val composeRule = createComposeRule()

    private val repository = FakeItemRepository()
    private val testModule = module {
        single<ItemRepository> { repository }
        factoryOf(::SaveItemUseCase)
        viewModelOf(::ItemEditViewModel)
    }

    @Test
    fun `제목 입력 후 저장하면 뒤로 이동`() {
        var backCalled = false
        composeRule.setContent {
            KoinApplication(application = { modules(testModule) }) {
                AppTheme { ItemEditRoot(onNavigate = {}, onBack = { backCalled = true }) }
            }
        }

        composeRule.onNodeWithTag("title_input").performTextInput("여행")
        composeRule.onNodeWithTag("date_picker_confirm").performClick()
        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.waitUntil { backCalled }
    }
}
```

- 테스트에서 찾을 요소에는 `Modifier.testTag("…")`를 붙입니다. 태그는 `snake_case`.
- Root가 `internal`이므로 같은 모듈의 테스트에서 호출할 수 있습니다.

### 5-12. E2E (Maestro)

```yaml
# e2e/flows/create_item.yaml
appId: {base.package}.dev
---
- launchApp:
    clearState: true
- tapOn: "시작하기"
- tapOn:
    id: "add_button"
- inputText: "테스트 항목"
- tapOn: "저장"
- assertVisible: "테스트 항목"
```

- E2E는 dev 빌드로, 실제 서버 대신 에뮬레이터 · 테스트 서버에 연결합니다.
- 흐름은 2~3개로 유지합니다. 많아지면 유지 비용이 급격히 커집니다.

---

## 6. CI 구성

```yaml
# .github/workflows/pr.yml
name: PR
on: pull_request

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4

      - name: Lint & static analysis
        run: ./gradlew ktlintCheck detekt lintDevDebug

      - name: Unit tests
        run: ./gradlew testDevDebugUnitTest

      - name: Screenshot tests
        run: ./gradlew verifyRoborazziDevDebug

      - uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: reports
          path: |
            **/build/reports
            **/build/outputs/roborazzi
```

- 액션 · JDK 버전은 적용 시점의 최신 권장 버전을 사용합니다.
- 기기 테스트(마이그레이션)와 E2E는 별도 워크플로로 `main` 병합 후 또는 예약 실행합니다.

---

## 7. 기준과 체크리스트

### 7-1. 커버리지 기준 (권장)

| 대상 | 기준 |
|---|---|
| core:domain | 라인 커버리지 90% 이상 |
| ViewModel | 주요 Action(저장, 삭제, 이동, 실패 처리) 100% |
| Migration | 모든 버전 경로 100% |
| 그 외 | 수치 목표 없음. 버그가 난 곳에 회귀 테스트 추가 |

커버리지 숫자보다 **"버그가 나면 이 테스트가 잡는가"**를 기준으로 판단합니다.

### 7-2. 버그 수정 규칙

- **[필수]** 버그를 고칠 때는 그 버그를 재현하는 테스트를 먼저 작성하고, 실패를 확인한 뒤 수정합니다.

### 7-3. 기능 추가 시 체크리스트

- [ ] 새 도메인 규칙에 경계값 포함 단위 테스트
- [ ] ViewModel 주요 Action 테스트 (성공 · 실패)
- [ ] Screen Preview (라이트 · 다크, 상태별) → 스크린샷 기준 이미지 갱신
- [ ] Koin 모듈 추가 시 `verify()` 통과
- [ ] DB 스키마 변경 시 Migration + 마이그레이션 테스트
- [ ] 핵심 흐름이 바뀌었다면 E2E 흐름 갱신
