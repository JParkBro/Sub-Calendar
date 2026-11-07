# 공통 개발 문서

다른 Android 프로젝트에도 **그대로 복사해서** 쓸 수 있는 범용 문서입니다. 특정 앱의 기능 · 데이터 · 디자인 내용은 들어 있지 않습니다.

| 문서 | 답하는 질문 | 바뀌는 빈도 |
|---|---|---|
| [ANDROID_ARCHITECTURE.md](ANDROID_ARCHITECTURE.md) | 무엇을 **어디에** 두나 (모듈, 계층, 의존 방향, 내비게이션) | 낮음 |
| [CODING_CONVENTIONS.md](CODING_CONVENTIONS.md) | 코드를 **어떻게** 쓰나 (이름, MVI, Compose, 코루틴, Git) | 보통 |
| [TESTING.md](TESTING.md) | 무엇을 **어떻게 검증**하나 (테스트 종류, 템플릿, CI) | 보통 |
| [GIT_CONVENTIONS.md](GIT_CONVENTIONS.md) | 변경을 **어떻게 합치고 출시**하나 (브랜치, 커밋 · PR, 리뷰, 릴리스) | 낮음 |

## 기술 스택 요약

Kotlin · Jetpack Compose · Navigation 3 · Koin · Ktor · Room · DataStore · WorkManager · MVI · 기능별 api/impl 멀티 모듈

## 새 프로젝트에 적용하는 방법

1. 이 폴더(`common/`)를 새 프로젝트의 `docs/common/`에 복사합니다.
2. 문서 안의 자리표시자를 프로젝트 값으로 바꿉니다.

   | 자리표시자 | 의미 | 예 |
   |---|---|---|
   | `{app}` | 프로젝트 약칭 (convention plugin ID 접두사) | `myapp` |
   | `{base.package}` | 기본 패키지 | `com.example.myapp` |
   | `{feature}` | 기능 이름 (소문자) | `home` |
   | `{Feature}` | 기능 이름 (클래스 접두사) | `Home` |

3. 프로젝트 전용 결정(기능 목록, 데이터 모델, 디자인)은 `docs/{project}/`에 따로 작성하고, 공통 문서와 다르게 가는 부분이 있으면 그 문서에 **예외로 명시**합니다.
4. 공통 규칙 자체를 바꾸고 싶으면 이 폴더 문서를 수정하고, 다른 프로젝트에도 반영할지 판단합니다.

## 핵심 규칙 한눈에

- 기능은 `feature:{feature}:api`(NavKey만) / `impl`(구현)로 나누고, **impl → 다른 기능의 api만** 참조합니다.
- 화면은 `{Feature}Action` · `State` · `Event`(선택) · `ViewModel` · `Screen.kt`(internal Root + private Screen)로 구성합니다.
- ViewModel은 `onAction` 단일 진입점, 공개 멤버는 `state` · `event` · `onAction`뿐입니다.
- Action · Event는 `sealed interface`, `when`에 `else`를 쓰지 않습니다.
- 화면 이동은 ViewModel이 아니라 Root가 Event를 받아 처리합니다.
- Koin `verify()` 테스트와 DB 마이그레이션 테스트는 필수입니다.
- 커밋 · PR 제목은 `[Type] scope: 요약`, `main`에는 Squash merge로만 합칩니다.
