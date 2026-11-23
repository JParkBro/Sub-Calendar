# 인앱 업데이트 (Play In-App Updates)

Play 스토어에 새 버전이 있으면 **앱을 켤 때 확인 시트 → 수락하면 백그라운드 다운로드 → 다운로드가 끝나면 재시작 확인 시트 → 설치**하는 구조. FLEXIBLE 방식만 쓴다.

기준 구현은 AniPick(`../anipick/docs/in-app-update.md`)이다. 구독캘린더 규칙(MVI · 바텀시트 · `strings.xml`)에 맞춰 바꾼 부분은 맨 아래 "AniPick과 다른 점"에 적는다.

외부 콘솔 설정이 필요 없어 개인용 단계(1.x)에 넣는다 (`module-map.md` 9장의 출시 단계 작업이 아님).

## 한눈에 보기

```
UpdateViewModel init ── 업데이트 확인 (앱을 켤 때 한 번)
   │  DOWNLOADED                                   → ReadyToInstall
   │  UPDATE_AVAILABLE && isFlexibleUpdateAllowed
   │    && 다운로드 중 아님 && 오늘 답한 적 없음       → UpdateAvailable(info)
   ▼
UpdatePrompt.UpdateAvailable ──▶ 시트 "새 버전이 나왔어요"  [업데이트] [나중에 → None]
   │  어느 쪽이든 답한 날(update_prompt_answered_on)을 저장 → 같은 날에는 다시 묻지 않음
   │ 업데이트
   ▼
UpdateEvent.StartUpdateFlow(info) ──▶ MainActivity: startUpdateFlowForResult(FLEXIBLE)
   │  Play 화면에서 동의 → 백그라운드 다운로드 (앱은 계속 쓸 수 있음)
   ▼
InstallStateUpdatedListener: DOWNLOADED ──┐
onResume ── UpdateAction.AppResumed: DOWNLOADED ──┤
                                                 ▼
UpdatePrompt.ReadyToInstall ──▶ 시트 "업데이트 준비 완료"  [재시작] [나중에 → None, 다음 onResume에 다시 뜸]
   │ 재시작
   ▼
UpdateEvent.CompleteUpdate ──▶ MainActivity: appUpdateManager.completeUpdate() → 재시작 · 설치
```

## 정해 둔 것

- **FLEXIBLE만 쓴다.** IMMEDIATE(전체 화면, 끝날 때까지 앱 사용 불가)는 쓰지 않는다.
- **Play 화면을 바로 띄우지 않고 앱 시트로 먼저 묻는다.** 다운로드 후 재시작도 한 번 더 묻는다. 확인 없이 업데이트 · 재시작하지 않는다.
- **새 버전 안내는 앱을 켤 때만, 하루 한 번.** "업데이트" · "나중에" 중 하나라도 답하면 그날은 다시 묻지 않고, 다음 날 앱을 켤 때 다시 묻는다. 앱으로 돌아올 때(`onResume`)는 새 버전을 확인하지 않는다.
- **재시작 확인은 앱으로 돌아올 때마다.** 다운로드가 끝난 업데이트는 설치를 계속 유도한다 (Play 권장).
- **판단 · 상태는 `UpdateViewModel`, 실행은 `MainActivity`.** `startUpdateFlowForResult()`는 Activity에 묶인 `ActivityResultLauncher`가 필요하고 `completeUpdate()`는 앱을 재시작시키므로, ViewModel은 Event만 보낸다.
- **확인 UI는 `SubCalBottomSheet`.** 디자인 시스템에 다이얼로그 컴포넌트가 없고, 다른 확인(복원 · 해지)도 시트로 묻는다.
- **시트는 MainActivity의 `AppTheme` 안, `SubCalApp` 뒤에 둔다.** 어느 화면에 있든 뜬다.
- **실패는 조용히 무시한다 (`suspendRunCatching` → `getOrNull()`).** 업데이트 확인 실패가 앱 사용을 막으면 안 된다. Play에서 설치하지 않은 빌드(Android Studio 설치 등)는 실패하거나 `UPDATE_NOT_AVAILABLE`이 온다.
- **app 모듈에만 둔다** (`jpark.bro.subcal.update`). feature 모듈은 이 기능을 모른다.
- 문구는 `app/src/main/res/values/strings.xml`의 `app_update_*`.

## 파일 구성

```
app/src/main/kotlin/jpark/bro/subcal/
├── update/
│   ├── UpdateState.kt      # UpdateState(prompt) + UpdatePrompt: None / UpdateAvailable(info) / ReadyToInstall
│   ├── UpdateAction.kt     # AppResumed + 시트 버튼 4개
│   ├── UpdateEvent.kt      # MainActivity가 실행할 것 2개
│   ├── UpdateViewModel.kt  # MviViewModel 상속
│   └── UpdateSheet.kt      # 확인 시트 2종 + Preview
├── di/AppModule.kt         # AppUpdateManager single + UpdateViewModel
└── MainActivity.kt         # 런처, onResume, Event 처리, UpdateSheet
app/src/test/kotlin/jpark/bro/subcal/update/UpdateViewModelTest.kt
```

## 1. 의존성

`gradle/libs.versions.toml`: `appUpdate = "2.1.0"`, `app-update` · `app-update-ktx`.
`-ktx`는 `requestAppUpdateInfo()`(suspend)와 `isFlexibleUpdateAllowed` 확장 때문에 쓴다.
`app/build.gradle.kts`: 위 둘 + `core:common`(suspendRunCatching) · `core:ui`(MviViewModel · ObserveEvents), 테스트에 `bundles.robolectric`.

## 2. State / Action / Event

- 시트 두 개가 동시에 뜨지 않도록 Boolean 여러 개가 아니라 sealed interface `UpdatePrompt` 하나로 둔다.
- Action: `AppResumed`, `UpdateConfirmed`, `UpdateDismissed`, `InstallConfirmed`, `InstallDismissed`.
- Event: `StartUpdateFlow(info)`, `CompleteUpdate`.

## 3. ViewModel

- **새 버전 확인은 `init`에서 한 번.** ViewModel은 화면 회전 · 다크 모드 변경에도 남으므로, "나중에"를 고른 뒤 회전해도 다시 묻지 않는다.
- **`AppResumed`는 다운로드 완료만 다시 본다** (`offerNewVersion = false`). 앱으로 돌아올 때마다 새 버전 안내를 띄우지 않는다.
- **하루 한 번**: `UpdateConfirmed` · `UpdateDismissed`에서 오늘 날짜(`LocalDate.now(clock)`)를 `SettingsRepository.setUpdatePromptAnsweredOn()`으로 저장한다. `init`에서 저장된 날이 오늘이면 새 버전 안내를 건너뛴다. "업데이트"도 저장하는 이유: Play 화면에서 취소한 뒤 같은 날 앱을 다시 켤 때 또 묻지 않도록. 저장에 실패하면 무시한다 (다음에 켤 때 한 번 더 묻는 것뿐).
- **다운로드 중에는 새 버전 안내를 띄우지 않는다** (`PENDING` · `DOWNLOADING` · `INSTALLING`). 다운로드 중에 앱을 껐다 켠 경우.
- 확인 결과가 `DOWNLOADED`면 새 버전 안내보다 **재시작 확인이 먼저**다 (다운로드해 둔 채 앱을 껐다 켠 경우).
- 리스너(`InstallStateUpdatedListener`)는 ViewModel 생명주기에 건다. `init`에서 등록, `onCleared()`에서 해제.
- 버튼을 누르면 `prompt`를 바로 `None`으로 돌려 시트를 닫는다.

## 4. DI (Koin)

```kotlin
single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
viewModelOf(::UpdateViewModel)
```

ViewModel(리스너 · 확인)과 MainActivity(Play 화면 · 재시작)가 **같은 `AppUpdateManager` 인스턴스**를 써야 하므로 `single`. `SettingsRepository` · `Clock`은 `dataModule`에 있다. `KoinModulesTest`가 통과해야 한다.

## 5. MainActivity 연결

- `updateFlowLauncher = registerForActivityResult(StartIntentSenderForResult()) {}`를 **필드**로 둔다 (STARTED 전에 등록해야 함). 결과는 쓰지 않는다. Play 화면에서 취소하면 다음 실행 때 다시 묻는다.
- `setContent`의 `AppTheme` 안에서 `ObserveEvents(updateViewModel.event)`로 Event를 받아 `startUpdateFlowForResult(info, updateFlowLauncher, AppUpdateOptions.newBuilder(FLEXIBLE).build())` · `completeUpdate()`를 호출한다. `when`에 `else`를 쓰지 않는다.
- `UpdateSheet(prompt, updateViewModel::onAction)`를 `SubCalApp` 뒤에 둔다.
- `onResume()`에서 `updateViewModel.onAction(UpdateAction.AppResumed)`.

## 6. 알고 있는 동작 (버그 아님, 바꿀 때 참고)

| 상황 | 동작 | 바꾸려면 |
|---|---|---|
| "새 버전이 나왔어요"에서 "나중에" | 그날은 앱을 껐다 켜도 묻지 않고, 다음 날 앱을 켤 때 다시 묻는다 (회전 · 다크 모드 변경에도 안 뜸) | 간격을 바꾸려면 `answeredToday()`의 비교를 바꾼다 |
| 앱을 백그라운드에 둔 채 날이 바뀜 | 앱을 완전히 껐다 켜기 전까지 새 버전 안내가 뜨지 않는다 (`onResume`에서는 새 버전을 확인하지 않음) | `AppResumed`에서도 `offerNewVersion = true`로 확인한다 |
| "업데이트" 후 Play 화면에서 취소 | 그날은 다시 묻지 않고 다음 날 앱을 켤 때 묻는다 | – |
| "업데이트 준비 완료"에서 "나중에" | 앱으로 돌아올 때(`onResume`)마다 다시 뜬다. 다운로드된 업데이트는 설치를 계속 유도하라는 Play 권장 동작 | 덜 보이게 하려면 시트 대신 `SubCalToast`(동작 "재시작")로 바꾼다 |
| 다운로드 중 앱 종료 | Play가 다운로드를 계속하고, 다음 실행 때 `DOWNLOADED`를 잡아 재시작 확인을 띄운다 | – |
| 온보딩 화면 | 온보딩 중에도 시트가 뜬다 | 막으려면 MainActivity에서 `startKey`가 온보딩이면 그리지 않는다 |
| 디버그 빌드 | 확인이 실패하거나 업데이트 없음으로 와서 아무것도 안 뜬다 | – |

## 7. 확인 방법

- **단위 테스트**: `UpdateViewModelTest`가 `FakeAppUpdateManager`(Robolectric) · `FakeSettingsRepository` · `SampleData.clock`(2026.10.30)으로 상태 전이와 하루 한 번 규칙을 검증한다. `setUpdateAvailable()` → `startUpdateFlowForResult()` → `userAcceptsUpdate()` → `downloadStarts()` → `downloadCompletes()`.
  - app 모듈 Robolectric 테스트는 `@Config(application = Application::class)`를 붙인다. `SubCalApplication`이 Koin을 시작해 테스트마다 충돌한다.
- **실기기**: 실제 시트 · 다운로드는 **Play에서 설치한 빌드**에서만 동작한다. 실기기 확인은 사용자가 한다.
  1. versionCode가 낮은 빌드와 높은 빌드를 둘 다 **내부 앱 공유(Internal app sharing)** 에 올린다.
  2. 기기의 Play 스토어 설정에서 "내부 앱 공유"를 켠다.
  3. 낮은 빌드 링크로 설치 → 높은 빌드 링크를 열기만 하고(설치 X) → 앱 실행.
  4. "새 버전이 나왔어요" → 업데이트 → 다운로드 → "업데이트 준비 완료" → 재시작 순서로 뜨는지 본다.
- 내부 테스트 트랙으로도 확인할 수 있다. 어느 쪽이든 **versionCode가 올라가 있어야** 업데이트로 인식된다 (CLAUDE.md "출시 번들(AAB) 만들기"는 항상 versionCode를 1 올린다).

## AniPick과 다른 점

| 항목 | AniPick | 구독캘린더 |
|---|---|---|
| 확인 UI | `AppDialog` | `SubCalBottomSheet` (`UpdateSheet.kt`) |
| ViewModel | `ViewModel` + 공개 함수 `checkForUpdate()` · `checkDownloadedUpdate()` | `MviViewModel`. 공개 멤버는 `state` · `event` · `onAction`뿐 → 새 버전 확인은 `init`, 다운로드 확인은 `UpdateAction.AppResumed` |
| 회전 시 재안내 | `onCreate`마다 확인해 다시 뜸 | `init`에서 한 번만 확인해 다시 안 뜸 |
| 새 버전 안내 빈도 | 앱을 켤 때마다 | 하루 한 번 (답한 날을 DataStore `update_prompt_answered_on`에 저장) |
| 시작 시 `DOWNLOADED` | 새 버전 안내가 먼저 뜰 수 있음 | 재시작 확인을 먼저 띄움 |
| 예외 처리 | `runCatching` | `suspendRunCatching` (CancellationException을 삼키지 않음) |
| 문구 | Compose 리터럴 | `strings.xml` `app_update_*` |
| 이벤트 수집 | `ObserveAsEvents` | `core:ui`의 `ObserveEvents` |

## 확장 (지금은 안 함)

- **강제 업데이트**: 특정 versionCode 미만을 막아야 하면 Remote Config(출시 단계 `core:firebase`)에 최소 versionCode를 두고, 그 미만이면 `AppUpdateType.IMMEDIATE` + 닫을 수 없는 시트로 간다. `updatePriority()`는 출시 후 값을 못 바꾸므로 기준으로 쓰지 않는다.
