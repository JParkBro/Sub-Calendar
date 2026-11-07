# 구독캘린더 (SubCalendar)

구독 서비스의 결제일 · 금액을 캘린더로 관리하는 앱. Android 전용.

## 저장소 구조

| 경로 | 내용 | 세부 지침 |
|---|---|---|
| 루트 (`app/`, `build-logic/`, 이후 `core/`, `feature/`) | Android 앱 (Kotlin, Compose, 멀티 모듈) | 이 파일의 "Android 앱" 섹션 |
| `docs/common/` | 범용 규칙 (아키텍처, 컨벤션, 테스트, Git) | – |
| `docs/subcal/` | 이 프로젝트의 기획 · 데이터 · 디자인 결정 | – |

## 문서 지도

작업 전에 관련 문서를 읽는다. 문서와 코드가 다르면 **문서가 기준**이고, 의도적으로 바꿔야 하면 먼저 사용자에게 확인한다.
`docs/subcal/`에서 "작성 예정"인 문서가 필요한 작업이면, 추측해서 구현하지 말고 먼저 사용자와 결정해서 문서를 쓴다.

| 작업 | 먼저 읽을 문서 |
|---|---|
| 화면 · 기능 구현 | `docs/subcal/product-spec.md`, `docs/subcal/module-map.md` |
| 모듈 추가 · 구조 변경 | `docs/common/ANDROID_ARCHITECTURE.md`, `docs/subcal/module-map.md` |
| 코드 작성 전반 | `docs/common/CODING_CONVENTIONS.md` |
| 테스트 작성 | `docs/common/TESTING.md` |
| UI · 색상 | `docs/subcal/theme-guide.md` |
| DB · 데이터 모델 | `docs/subcal/data-model-erd.md` |
| 브랜치 · 커밋 · PR | `docs/common/GIT_CONVENTIONS.md` |

## 절대 어기면 안 되는 결정

- **Room이 원본.** 화면은 Room만 구독한다.
- **ID는 클라이언트 생성 UUID 문자열**, 날짜는 `yyyy-MM-dd`(LocalDate), 시각은 epoch millis. 자동 증가 ID 금지.
- **금액은 부동소수(`Float`/`Double`)로 저장 · 계산하지 않는다.** 정수 최소 단위 + 통화 코드로 다룬다 (정확한 형식은 `data-model-erd.md`에서 결정).
- **색은 테마 토큰으로만.** `Color(0xFF…)` 하드코딩 금지.
- 서버 · 로그인 · 광고 · 결제는 `docs/subcal/README.md`에서 "미정"이다. 결정 전에 임의로 추가하지 않는다.

## 작업 방식

- 여러 모듈에 걸친 변경, DB 스키마 변경은 **먼저 계획을 보여주고** 진행한다.
- 기획이 문서에 없거나 모호하면 추측해서 구현하지 말고 질문한다.
- 결정이 바뀌면 관련 `docs/` 문서도 같은 PR에서 고친다. 공통 규칙에서 벗어나는 결정은 `docs/subcal/README.md`의 "공통 문서와 다르게 가는 부분"에 기록한다.
- 작업을 끝내기 전에 "완료 전 확인"을 실행한다.

## Git

- 브랜치: `{type}/{이슈번호}-{영문-요약}` (예: `feat/12-calendar-month-view`)
- 커밋 · PR 제목: `[Type] scope: 요약` (예: `[Feat] calendar: 월간 결제일 표시 추가`)
  - Type: `Feat` `Fix` `Design` `Refactor` `Perf` `Test` `Docs` `Build` `CI` `Chore` `Revert`
  - 요약은 한국어, "~ 추가 / 수정 / 제거 / 변경"으로 끝낸다
- `main`에 직접 커밋 · push하지 않는다. force push는 본인 작업 브랜치에만 `--force-with-lease`.
- 훅을 우회하는 `--no-verify`를 쓰지 않는다.
- 포맷 변경과 로직 변경을 한 커밋에 섞지 않는다.

## 비밀 정보

다음을 커밋하거나 코드에 넣지 않는다. 필요하면 환경 변수 · `local.properties` · `keystore.properties` · GitHub Secrets를 쓴다.

- 키스토어(`*.jks`, `*.keystore`)와 비밀번호 (`keystore.properties`)
- 서비스 계정 JSON, API 비밀 키, 토큰, `google-services.json`
- `local.properties`, `keystore.properties`, `.env`

개인정보(토큰, 이메일, 사용자 입력 원문)를 로그에 남기지 않는다.

## 언어

- 사용자에게 하는 설명, 커밋 메시지, PR 본문, 문서: **한국어**
- 코드 식별자, 패키지, 파일명: 영어
- 앱 UI 문자열: `strings.xml`에 한국어로 추가 (하드코딩 금지)

## Android 앱

규칙 원문: `docs/common/ANDROID_ARCHITECTURE.md`, `docs/common/CODING_CONVENTIONS.md`, `docs/common/TESTING.md`
이 프로젝트의 모듈 · 키 · 의존 관계: `docs/subcal/module-map.md`

### 스택

Kotlin · Jetpack Compose · Navigation 3 · Koin(`koin-compose-navigation3`) · Room · DataStore · WorkManager

### 빌드 설정

- 기본 패키지 · applicationId: `jpark.bro.subcal` (dev는 `.dev` 접미사)
- 버전 · SDK 값은 `gradle/libs.versions.toml`의 `project*` 항목에서만 바꾼다.
- 모듈 설정은 `build-logic/`의 convention plugin으로만 한다. 플러그인 ID 접두사는 `subcal.`

| 플러그인 | 용도 |
|---|---|
| `subcal.android.application` | app (flavor · 서명 · ktlint · detekt 포함) |
| `subcal.android.library` | Android 라이브러리 모듈 |
| `subcal.android.compose` | Compose 사용 모듈 (application/library 다음에 적용) |
| `subcal.jvm.library` | 순수 Kotlin 모듈 (`core:model`, `core:common`, `core:domain`) |
| `subcal.jvm.ktor` | Ktor 클라이언트 |
| `subcal.koin` | Koin |
| `subcal.room` | Room + KSP + 스키마 디렉터리 |
| `subcal.feature.api` | `feature:*:api` |
| `subcal.feature.impl` | `feature:*:impl` (`core:ui` · `designsystem` · `navigation` · `data` · `domain` · `model` · `testing`이 있어야 함) |

- Gradle 데몬은 JDK 21로 고정되어 있다 (`gradle/gradle-daemon-jvm.properties`). detekt 1.23은 JDK 25에서 실행되지 않는다.

### 명령어

> 빌드 변형은 `dev` / `prod` flavor × `debug` / `release`. 작업 이름이 실제와 다르면 `./gradlew tasks`로 확인하고 이 표를 고친다.

| 목적 | 명령 |
|---|---|
| 디버그 빌드 | `./gradlew :app:assembleDevDebug` |
| 전체 단위 테스트 | `./gradlew testDevDebugUnitTest` |
| 특정 모듈 테스트 | `./gradlew :feature:{name}:impl:testDevDebugUnitTest` |
| 포맷 자동 수정 | `./gradlew ktlintFormat` |
| 정적 분석 | `./gradlew ktlintCheck detekt lintDevDebug` |
| DB 마이그레이션 테스트 | `./gradlew :core:database:connectedDevDebugAndroidTest` (기기 필요, 모듈 생성 후) |

### 모듈 규칙

- 기능은 `feature:{name}:api`(NavKey만) / `feature:{name}:impl`(구현)로 나눈다.
- **impl은 다른 기능의 api만 참조한다. impl → impl 금지, api → api 금지.**
- api에는 `@Serializable` NavKey만 둔다. 외부에서 들어올 화면의 키만 공개하고, 내부 화면 키는 impl에 `internal`.
- feature는 `core:data`(Repository)로만 데이터에 접근한다. `core:database`, `core:network`를 직접 참조하지 않는다.
- `core:model`, `core:common`, `core:domain`은 순수 Kotlin(JVM) 모듈. Android 의존을 넣지 않는다.
- 결제일 계산 · 반복 주기 규칙은 `core:domain`에만 둔다. 화면 · 위젯 · 알림이 모두 이것을 쓴다.

### 화면 (MVI)

화면 하나 = 아래 파일 묶음 (`feature/{name}/impl/.../{name}/impl/`)

```
{Feature}Action.kt     sealed interface — 사용자가 한 일 (SaveClicked, TitleChanged)
{Feature}State.kt      data class — 모든 프로퍼티 val + 기본값
{Feature}Event.kt      sealed interface — 일회성 효과 (NavigateBack, ShowSnackbar). 없으면 파일 생략, 타입 인자는 Nothing
{Feature}ViewModel.kt  MviViewModel<S, A, E> 상속
{Feature}Screen.kt     internal {Feature}Root + private {Feature}Screen + private Preview(@ScreenPreviews)
di/{Feature}Module.kt  viewModelOf + navigation<{Feature}Key>
```

- ViewModel 공개 멤버는 `state`, `event`, `onAction`뿐. 화면 입력은 **`onAction` 하나로만** 받는다.
- `onAction`과 Root의 Event 처리 `when`에는 **`else`를 쓰지 않는다.**
- 인자 없는 Action · Event는 `data object`, 있으면 `data class`.
- ViewModel은 `Context`, `Navigator`, Compose 타입을 모른다. **화면 이동은 Event → Root에서 처리.**
- 계속 보여야 하는 것(다이얼로그 표시, 입력값)은 State, 한 번만 일어나는 것은 Event.
- Root는 ViewModel 연결 · State 수집 · Event 처리만 한다. 레이아웃은 Screen에.
- Screen은 `state`, `onAction`, `modifier: Modifier = Modifier`를 받고 ViewModel을 모른다.

### Navigation 3 · Koin

- 이동은 `Navigator`(`core:navigation`)의 `goTo` / `goBack` / `replaceAll`.
- `NavDisplay`의 `entryDecorators`에 `rememberViewModelStoreNavEntryDecorator()`를 빼지 않는다.
- Koin은 생성자 주입만. 일반 클래스에서 `KoinComponent` · `get()` 사용 금지 (위젯 리시버 등 Android 진입점만 예외).
- `@KoinExperimentalAPI` 사용 시 `@OptIn`은 모듈 정의 파일에만.
- Koin 모듈을 추가 · 수정하면 `KoinModulesTest`(`verify()`)가 통과해야 한다.
- 온보딩 완료 등 흐름이 끝난 뒤 목적지는 impl이 아니라 app의 결과 핸들러가 결정한다.

### 데이터

- Repository: 스트림은 `observe…(): Flow<T>`, 1회성은 `suspend … : Result<T>`. 예외를 ViewModel까지 던지지 않는다.
- suspend 블록 예외 처리는 `runCatching` 대신 `suspendRunCatching` (CancellationException을 삼키지 않기 위해).
- Room 컬럼명은 snake_case + `@ColumnInfo(name = …)`. 스키마를 바꾸면 Migration + 스키마 JSON 커밋 + 마이그레이션 테스트. 단, **출시 전까지는 version 1을 고쳐 쓴다** (`docs/subcal/README.md` 예외 항목).
- 현재 시각은 주입받은 `Clock`, 디스패처도 주입받는다.

### UI

- 색은 테마 토큰만 사용 (토큰 정의는 `theme-guide.md` 작성 후 `core:designsystem`에).
- 버튼 · 칩 · 뱃지 · 카드 · 입력창 · 시트 등은 `core:designsystem`의 공통 컴포넌트를 쓴다. 기능 모듈에 비슷한 컴포넌트를 새로 만들지 않는다.
- Preview: 화면은 `@ScreenPreviews`(라이트 · 다크 · 글꼴 2배 · 가로), 공통 컴포넌트는 `@ComponentPreviews`(라이트 · 다크 · 글꼴 2배).
- 금액 · 날짜 숫자에 tabular-nums.
- 문자열은 `strings.xml` (`{feature}_{screen}_{purpose}`), 아이콘에 `contentDescription`.
- `LazyColumn` 항목에 `key`.
- 테스트에서 찾을 요소에 `Modifier.testTag("snake_case")`.

### 테스트

코드를 바꾸면 함께 작성 · 수정한다.

| 바꾼 것 | 필요한 테스트 |
|---|---|
| `core:domain` 규칙 | 경계값 포함 단위 테스트 (월말 결제일 31일 → 2월, 윤년, 연 결제) |
| ViewModel | 주요 Action의 State · Event 테스트 (성공 · 실패) |
| Screen UI | Preview 추가 · 수정 |
| DB 스키마 | Migration 테스트 |
| Koin 모듈 | `verify()` 통과 |

- Fake는 `core:testing`에 두고 재사용한다. Mock보다 Fake 우선.
- 테스트 이름은 백틱 한국어 문장: `` `이름이 비어 있으면 저장 버튼 비활성` ``.
- 샘플 데이터는 `docs/subcal/product-spec.md`의 시나리오를 쓴다.

### 새 기능(화면) 추가 순서

1. `settings.gradle.kts`에 `:feature:{name}:api`, `:feature:{name}:impl` 추가
2. api: NavKey / impl: Action · State · (Event) · ViewModel · Screen.kt · di 모듈
3. 이동할 기능의 api만 impl 의존성에 추가
4. app: impl 의존성 + `appModule`의 `includes`에 모듈 추가
5. ViewModel 테스트, Preview, Koin verify
6. `docs/subcal/module-map.md`의 키 · 의존 표 갱신

### 완료 전 확인

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck detekt
./gradlew testDevDebugUnitTest      # 변경 범위가 작으면 해당 모듈만
```

ktlint · detekt 위반은 코드를 고쳐서 해결한다. `ktlint-baseline.xml` · `detekt-baseline.xml`에 새 위반을 추가하지 않는다.
