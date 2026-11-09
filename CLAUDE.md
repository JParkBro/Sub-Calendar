# 구독캘린더 (SubCalendar)

구독료 관리 앱. 넷플릭스 · 유튜브 프리미엄 같은 구독을 등록하면 결제일을 알려주고, 지출을 분석해 해지 · 절약을 돕는다. **서버 없이 기기 안에서만 동작**하고 로그인이 없다. Android 전용.

> 앱 표시 이름 "구독캘린더"는 가칭이다. 패키지 · 코드 약칭은 `subcal`로 고정한다.

## 저장소 구조

| 경로 | 내용 | 세부 지침 |
|---|---|---|
| 루트 (`app/`, `build-logic/`, 이후 `core/`, `feature/`, `widget/`) | Android 앱 (Kotlin, Compose, 멀티 모듈) | 이 파일의 "Android 앱" 섹션 |
| `widget/` | Glance 홈 화면 위젯 (2차, 아직 Gradle 모듈 아님) | `widget/CLAUDE.md` |
| `policy-site/` | Firebase Hosting(`public/`): 개인정보 처리방침 · app-ads.txt (출시 단계에서 생성) | `docs/subcal/infra.md` 3장 |
| `docs/common/` | 범용 규칙 (아키텍처, 컨벤션, 테스트, Git). 데이즈와 같은 문서 | – |
| `docs/subcal/` | 이 프로젝트의 기획 · 데이터 · 인프라 결정 | – |

## 문서 지도

작업 전에 관련 문서를 읽는다. 문서와 코드가 다르면 **문서가 기준**이고, 의도적으로 바꿔야 하면 먼저 사용자에게 확인한다.

| 작업 | 먼저 읽을 문서 |
|---|---|
| 화면 · 기능 구현 | `docs/subcal/product-spec.md`, `docs/subcal/module-map.md`, 시안 `docs/subcal/design/{화면}/` (폴더 목록: product-spec 3-2, 따르지 않을 부분: 11장) |
| 결제일 · 금액 계산 | `docs/subcal/product-spec.md` 2장 |
| 모듈 추가 · 구조 변경 | `docs/common/ANDROID_ARCHITECTURE.md`, `docs/subcal/module-map.md` |
| 코드 작성 전반 | `docs/common/CODING_CONVENTIONS.md` |
| 테스트 작성 | `docs/common/TESTING.md` |
| UI · 색상 | `docs/subcal/theme-guide.md`, 값 원본 `docs/subcal/assets/theme-tokens.json`, 레이아웃 참고 `docs/subcal/design/` |
| DB · 데이터 모델 · 백업 | `docs/subcal/data-model-erd.md` |
| Firebase · 정책 페이지 · 출시 (출시 단계) | `docs/subcal/infra.md` |
| 광고 · 광고 제거 구매 (출시 단계) | `docs/subcal/ads-strategy.md` |
| 위젯 | `widget/CLAUDE.md`, `docs/subcal/product-spec.md` 7장 |
| 브랜치 · 커밋 · PR | `docs/common/GIT_CONVENTIONS.md` |

## 절대 어기면 안 되는 결정

- **서버를 만들지 않는다.** 사용자 데이터를 받는 백엔드 · Firestore · Cloud Functions · Cloud Storage를 쓰지 않는다. Firebase는 **Spark(무료) 요금제**로 Crashlytics · Analytics · Remote Config · Hosting만 쓴다. Blaze로 올리지 않는다 (`docs/subcal/infra.md`).
- **Room이 유일한 원본.** 화면은 Room만 구독한다. 기기 밖으로 나가는 사용자 데이터는 사용자가 직접 내보낸 백업 파일과 Android 자동 백업뿐이다.
- **결제 내역 · 누적 지출 · 절약액은 저장하지 않고 계산한다.** 저장하는 것은 구독의 결제 규칙, 가격 변경, 일시정지 기간, 해지일뿐이다. 계산 규칙은 `core:domain`에만 둔다.
- **금액은 `Long` 최소 단위**(원, 센트)와 통화 코드로 저장한다. `Double` · `Float`으로 금액을 저장하거나 더하지 않는다.
- **ID는 클라이언트 생성 UUID 문자열**, 날짜는 `yyyy-MM-dd`(LocalDate), 시각은 epoch millis. 자동 증가 ID 금지 (위젯 id만 예외).
- **서비스 브랜드 로고 이미지를 앱에 넣지 않는다.** 서비스는 이니셜 + 지정 색 아이콘으로 표시한다 (상표 문제). Stitch 시안에 로고가 있어도 따르지 않는다.
- **색은 테마 토큰으로만.** `Color(0xFF…)` 하드코딩 금지.
- 문서의 "범위 밖" 기능(은행 · 카드 연동 자동 등록, 로그인 · 클라우드 동기화, 가족 공유 계정 동기화, 결제 대행)을 임의로 추가하지 않는다.

## 작업 방식

- **개인용 앱으로 먼저 만든다.** 저장소는 Room · DataStore(Preferences)만 쓴다. Firebase · AdMob · Play Billing · 정책 페이지처럼 사용자가 외부 콘솔에서 설정해야 하는 작업은 모든 기능을 만든 뒤 마지막 "출시 단계"에서 한다 (`docs/subcal/module-map.md` 9장). 그 전에 이런 작업이 필요해 보이면 먼저 사용자에게 확인한다.
- 여러 모듈에 걸친 변경, DB 스키마 변경, 계산 규칙 변경은 **먼저 계획을 보여주고** 진행한다.
- 기획이 문서에 없거나 모호하면 추측해서 구현하지 말고 질문한다.
- 결정이 바뀌면 관련 `docs/` 문서도 같은 PR에서 고친다. 공통 규칙에서 벗어나는 결정은 `docs/subcal/README.md`의 "공통 문서와 다르게 가는 부분"에 기록한다.
- 작업을 끝내기 전에 "완료 전 확인"을 실행한다.

## Git

- 브랜치: `{type}/{이슈번호}-{영문-요약}` (예: `feat/12-home-upcoming-payments`)
- 커밋 · PR 제목: `[Type] scope: 요약` (예: `[Feat] home: 다가오는 결제 카드 추가`)
  - Type: `Feat` `Fix` `Design` `Refactor` `Perf` `Test` `Docs` `Build` `CI` `Chore` `Revert`
  - 요약은 한국어, "~ 추가 / 수정 / 제거 / 변경"으로 끝낸다
- `main`에 직접 커밋 · push하지 않는다. force push는 본인 작업 브랜치에만 `--force-with-lease`.
- 훅을 우회하는 `--no-verify`를 쓰지 않는다.
- 포맷 변경과 로직 변경을 한 커밋에 섞지 않는다.

## 비밀 정보

다음을 커밋하거나 코드에 넣지 않는다. 필요하면 `local.properties` · `keystore.properties` · GitHub Secrets를 쓴다.

- 키스토어(`*.jks`, `*.keystore`)와 비밀번호 (`keystore.properties`)
- `google-services.json`, AdMob 앱 ID · 광고 단위 ID(prod)
- `local.properties`, `keystore.properties`, `.env`

구독 이름 · 금액 · 메모 · 파티 멤버 이름 같은 사용자 입력을 로그 · Analytics 이벤트 · Crashlytics 키에 남기지 않는다. Analytics 이벤트에는 화면 이름과 동작 종류만 넣는다.

## 언어

- 사용자에게 하는 설명, 커밋 메시지, PR 본문, 문서: **한국어**
- 코드 식별자, 패키지, 파일명: 영어
- 앱 UI 문자열: `strings.xml`에 한국어로 추가 (하드코딩 금지)

## Android 앱

규칙 원문: `docs/common/ANDROID_ARCHITECTURE.md`, `docs/common/CODING_CONVENTIONS.md`, `docs/common/TESTING.md`
이 프로젝트의 모듈 · 키 · 의존 관계: `docs/subcal/module-map.md`

### 스택

Kotlin · Jetpack Compose · Navigation 3 · Koin(`koin-compose-navigation3`) · Room · DataStore · WorkManager · Glance · kotlinx.serialization(백업 JSON) · Firebase(Crashlytics, Analytics, Remote Config) · AdMob · Play Billing

Ktor · Firebase Auth · Firestore는 쓰지 않는다. 외부 HTTP가 필요해지면 먼저 사용자에게 확인한다.

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
| `subcal.koin` | Koin |
| `subcal.room` | Room + KSP + 스키마 디렉터리 |
| `subcal.feature.api` | `feature:*:api` |
| `subcal.feature.impl` | `feature:*:impl` (`core:ui` · `designsystem` · `navigation` · `data` · `domain` · `model` · `testing`이 있어야 함) |
| `subcal.jvm.ktor` | 사용하지 않음 (외부 HTTP 없음) |

- Gradle 데몬은 JDK 21로 고정되어 있다 (`gradle/gradle-daemon-jvm.properties`). detekt 1.23은 JDK 25에서 실행되지 않는다.

### 명령어

> 빌드 변형은 `dev` / `prod` flavor × `debug` / `release`. 작업 이름이 실제와 다르면 `./gradlew tasks`로 확인하고 이 표를 고친다.

| 목적 | 명령 |
|---|---|
| 디버그 빌드 | `./gradlew :app:assembleDevDebug` (`core:firebase` 추가 후에는 `app/google-services.json` 필요, 커밋하지 않음) |
| 전체 단위 테스트 | `./gradlew testDevDebugUnitTest` |
| 특정 모듈 테스트 | `./gradlew :core:domain:test` · `./gradlew :feature:home:impl:testDevDebugUnitTest` |
| 포맷 자동 수정 | `./gradlew ktlintFormat` |
| 정적 분석 | `./gradlew ktlintCheck detekt lintDevDebug` |
| DB 마이그레이션 테스트 | `./gradlew :core:database:connectedDevDebugAndroidTest` (기기 필요) |

### 모듈 규칙

- 기능은 `feature:{name}:api`(NavKey만) / `feature:{name}:impl`(구현)로 나눈다.
- **impl은 다른 기능의 api만 참조한다. impl → impl 금지, api → api 금지.**
- api에는 `@Serializable` NavKey만 둔다. 외부에서 들어올 화면의 키만 공개하고, 내부 화면 키는 impl에 `internal`.
- feature는 `core:data`(Repository)로만 데이터에 접근한다. `core:database`, `core:datastore`, `core:firebase`를 직접 참조하지 않는다.
- `core:model`, `core:common`, `core:domain`은 순수 Kotlin(JVM) 모듈. Android 의존을 넣지 않는다.
- 결제일 · 금액 · 절약액 · 가성비 계산은 `core:domain`에만 둔다. 화면 · 위젯 · 알림이 모두 이것을 쓴다.

### 화면 (MVI)

화면 하나 = 아래 파일 묶음 (`feature/{name}/impl/.../{name}/impl/`)

```
{Feature}Action.kt     sealed interface — 사용자가 한 일 (SaveClicked, AmountChanged)
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

- 이동은 `Navigator`(`core:navigation`)의 `goTo` / `goBack` / `replaceAll` / `switchTab`.
- `NavDisplay`의 `entryDecorators`에 `rememberViewModelStoreNavEntryDecorator()`를 빼지 않는다.
- Koin은 생성자 주입만. 일반 클래스에서 `KoinComponent` · `get()` 사용 금지 (위젯 리시버 · Worker 등 Android 진입점만 예외).
- `@KoinExperimentalAPI` 사용 시 `@OptIn`은 모듈 정의 파일에만.
- Koin 모듈을 추가 · 수정하면 `KoinModulesTest`(`verify()`)가 통과해야 한다.
- 온보딩 완료 후 목적지는 impl이 아니라 app의 결과 핸들러가 결정한다.

### 데이터

- Repository: 스트림은 `observe…(): Flow<T>`, 1회성은 `suspend … : Result<T>`. 예외를 ViewModel까지 던지지 않는다.
- suspend 블록 예외 처리는 `runCatching` 대신 `suspendRunCatching` (CancellationException을 삼키지 않기 위해).
- 삭제는 soft delete(`deleted_at`). 백업 · 복원과 실수 복구를 위해 행을 바로 지우지 않는다. 30일 지난 soft delete 행은 정리 Worker가 지운다.
- 구독 "해지"는 삭제가 아니다. `status = CANCELLED` + `cancelled_on`으로 남겨 절약 계산에 쓴다.
- Room 컬럼명은 snake_case + `@ColumnInfo(name = …)`. 스키마를 바꾸면 Migration + 스키마 JSON 커밋 + 마이그레이션 테스트. 단, **출시 전까지는 version 1을 고쳐 쓴다** (`docs/subcal/README.md` 예외 항목).
- 스키마를 바꾸면 백업 JSON 형식(`BackupV{n}`)과 복원 매퍼도 함께 고친다 (`data-model-erd.md` 6장).
- 현재 날짜 · 시각은 주입받은 `Clock`, 디스패처도 주입받는다.

### UI

- 색은 `AppThemeTokens.colors.*`만 사용. 보조 버튼 · 트랙 · 비활성 칩은 `fill`.
- 버튼 · 칩 · 뱃지 · 카드 · 입력창 · 시트 등은 `core:designsystem`의 `component/SubCal*`를 쓴다. 기능 모듈에 비슷한 컴포넌트를 새로 만들지 않는다 (목록: `theme-guide.md` 7장).
- Preview: 화면은 `@ScreenPreviews`(라이트 · 다크 · 글꼴 2배 · 가로), 공통 컴포넌트는 `@ComponentPreviews`(라이트 · 다크 · 글꼴 2배).
- 금액 표시는 `core:ui`의 `MoneyText` · `formatMoney`만 쓴다 (천 단위 쉼표, 통화별 소수 자리, tabular-nums). 화면에서 직접 포맷하지 않는다.
- D-day 색: 오늘 · 3일 이내 결제는 `danger`, 나머지 `textPrimary`, 지난 결제 `textTertiary`.
- 서비스 아이콘은 `SubCalServiceIcon(name, colorKey)` 하나로 그린다 (이니셜 + 지정 색).
- 문자열은 `strings.xml` (`{feature}_{screen}_{purpose}`), 아이콘에 `contentDescription`.
- `LazyColumn` 항목에 `key`.
- 테스트에서 찾을 요소에 `Modifier.testTag("snake_case")`.

### 테스트

코드를 바꾸면 함께 작성 · 수정한다.

| 바꾼 것 | 필요한 테스트 |
|---|---|
| `core:domain` 규칙 | 경계값 포함 단위 테스트 (월말 · 2월 29일 · 일시정지 경계 · 가격 변경일 당일 · 월 시작 기준일) |
| ViewModel | 주요 Action의 State · Event 테스트 (성공 · 실패) |
| Screen UI | Preview 추가 · 수정 |
| DB 스키마 | Migration 테스트 + 백업 왕복(내보내기 → 복원) 테스트 |
| Koin 모듈 | `verify()` 통과 |

- Fake는 `core:testing`에 두고 재사용한다. Mock보다 Fake 우선.
- 테스트 이름은 백틱 한국어 문장: `` `31일 결제는 4월에 30일로 당겨진다` ``.
- 샘플 데이터는 `docs/subcal/product-spec.md` 9장 시나리오(오늘 = 2026.10.30)를 쓴다.

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
