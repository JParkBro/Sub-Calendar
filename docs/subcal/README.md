# 구독캘린더(SubCalendar) 프로젝트 문서

구독 서비스의 결제일 · 금액을 캘린더로 관리하는 앱 **구독캘린더**에만 해당하는 문서입니다. 코드 구조 · 규칙 · 테스트 같은 범용 내용은 [`../common/`](../common/README.md)을 따르고, 이 폴더에는 구독캘린더의 결정 사항만 둡니다.

## 프로젝트 값

`docs/common/` 문서의 자리표시자는 아래 값으로 읽습니다.

| 자리표시자 | 값 |
|---|---|
| `{app}` (convention plugin ID 접두사) | `subcal` (예: `subcal.android.library`) |
| `{base.package}` | `jpark.bro.subcal` |
| 앱 이름 (UI) | 구독캘린더 |

## 문서 목록

| 문서 | 내용 | 상태 |
|---|---|---|
| `product-spec.md` | 기능 범위, 구독 · 결제일 계산 규칙, 화면 목록 · 동작, 설정 구조, 샘플 데이터 | 작성 예정 |
| `module-map.md` | 모듈 목록, 내비게이션 키, 기능 간 의존, 개발 순서 | 작성 예정 |
| `data-model-erd.md` | ERD, Room Entity, (서버를 쓰면) 서버 배치 | 작성 예정 |
| `theme-guide.md` | 테마 · 색상 토큰, 사용 규칙, Compose 적용 | 작성 예정 |

> 처음 작성 순서는 product-spec → module-map → data-model-erd → theme-guide를 추천합니다.

## AI 도구 지침 (Claude Code)

저장소 루트의 `CLAUDE.md`가 이 문서들을 기준으로 작업 지침을 요약합니다. 문서의 결정이 바뀌면 `CLAUDE.md`도 함께 고칩니다.

## 핵심 결정 요약

**기술**
- Kotlin · Jetpack Compose · Navigation 3 · Koin · Room · DataStore · WorkManager, 기능별 api/impl 멀티 모듈 (`docs/common/`).
- 원본 데이터는 Room. 화면은 Room만 구독.
- 모든 ID는 클라이언트 생성 UUID 문자열, 날짜는 `yyyy-MM-dd`(LocalDate), 시각은 epoch millis.
- 빌드 변형은 `dev` / `prod` flavor × `debug` / `release`. dev는 applicationId에 `.dev`가 붙음.
- 대상 플랫폼은 Android.

**미정** (결정되면 이 목록에서 옮긴다)
- 서버 사용 여부 (로그인 · 백업 · 동기화)
- 수익화 (광고 · 구매)
- 테마 · 색상

## 공통 문서와 다르게 가는 부분

공통 규칙에서 벗어나는 결정을 이유와 함께 기록합니다.

| 항목 | 공통 규칙 | 구독캘린더 | 이유 |
|---|---|---|---|
| Room 스키마 버전 | 스키마를 바꾸면 version을 올리고 Migration · 마이그레이션 테스트 추가 | **출시 전까지 version 1을 고쳐 쓰고** `schemas/…/1.json`을 덮어씀. 개발 기기에서 스키마가 안 맞으면 앱 데이터를 지우고 다시 설치 | 출시 전에는 보존할 사용자 데이터가 없어 Migration이 비용만 듦. **첫 출시 빌드부터는 공통 규칙을 따름** |
