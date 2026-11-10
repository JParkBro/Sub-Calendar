# 구독캘린더(SubCalendar) 프로젝트 문서

구독료 관리 앱 **구독캘린더**(가칭)에만 해당하는 문서입니다. 코드 구조 · 규칙 · 테스트 같은 범용 내용은 [`../common/`](../common/README.md)을 따르고, 이 폴더에는 이 앱의 결정 사항만 둡니다.

## 문서 목록

| 문서 | 내용 | 주로 보는 시점 |
|---|---|---|
| [product-spec.md](product-spec.md) | 기능 범위, 결제일 · 금액 계산 규칙, 화면 목록 · 동작, 설정 구조, 샘플 데이터 | 기획 · 디자인 · 개발 전반 |
| [theme-guide.md](theme-guide.md) | 인디고 테마 라이트/다크 2 팔레트, 토큰 27개(데이즈와 같은 이름), 타이포 · 모서리, 공통 컴포넌트 | 디자인 · UI 개발 |
| [module-map.md](module-map.md) | 모듈 목록, 내비게이션 키, 기능 간 의존, 화면별 Event, 개발 순서 | 개발 시작 · 기능 추가 |
| [data-model-erd.md](data-model-erd.md) | ERD, Room 테이블, 계산하는 값과 저장하는 값, 백업 JSON 형식 | 데이터 작업 |
| [ads-strategy.md](ads-strategy.md) | 광고 배치 · 빈도, 광고 제거 구매, AdMob 정책 | 출시 단계 |
| [infra.md](infra.md) | Firebase Spark 구성, 정책 페이지, 자동 백업, 출시 체크리스트 | 출시 단계 (자동 백업은 7단계) |
| `assets/theme-tokens.json` | 테마 색상 값 원본 (2 팔레트 × 27 토큰) | 디자인 시스템 코드 생성 |
| `design/{화면}/` | Stitch 시안 16개: `screen.png` + `code.html`. 화면별 폴더는 product-spec 3-2, 따르지 않을 부분은 11장. 색 · 글꼴 · 기능 범위는 이 폴더 문서가 기준이고 시안은 레이아웃 참고용 | 화면 구현 |
| `design/_stitch-design-system/DESIGN.md` | Stitch 디자인 시스템(Calm Ledger) 원본. 색 값은 `theme-tokens.json`이 기준 | 화면 구현 |

## AI 도구 지침 (Claude Code)

| 파일 | 범위 |
|---|---|
| `CLAUDE.md` | 프로젝트 전체: 문서 지도, 절대 규칙, Git, 비밀 정보, 언어 + Android 앱 규칙 · 명령 · 테스트 |
| `widget/CLAUDE.md` | Glance 위젯 모듈: 표시 규칙, 갱신, 설정 |

데이즈에 있던 `firebase/CLAUDE.md` · `firebase/functions/CLAUDE.md`는 없습니다. 이 앱은 보안 규칙 · 함수 · 에뮬레이터가 필요한 Firebase 서버 기능을 쓰지 않기 때문입니다 (`infra.md` 1장).

> 처음 읽을 때는 product-spec → theme-guide → module-map → data-model-erd 순서를 추천합니다.

## 핵심 결정 요약

**제품**
- 로그인 없음, 서버 없음. 설치하자마자 쓰는 로컬 앱.
- 결제 내역 · 누적 지출 · 절약액은 저장하지 않고 결제 규칙에서 계산 (데이즈의 "자동 기념일은 계산" 원칙과 같음).
- 해지는 삭제가 아니라 상태. 해지한 구독이 "아낀 돈"의 근거가 된다.
- 하단 탭은 홈 / 목록 / 캘린더 / 통계 / 더보기.
- 기능은 3단계: 1차 MVP(관리 · 캘린더 · 알림), 2차(분석 · 절약 · 무료체험 · 위젯), 3차(파티 정산 · 가성비 · 연말 리포트).
- **개인용 앱으로 먼저 만든다.** 1 ~ 3차는 Room · DataStore만 쓰고, 외부 설정이 필요한 Firebase · 광고 · 결제 · 정책 페이지 · 스토어 등록은 마지막 "출시 단계"에서 한다 (`module-map.md` 9장).

**기술**
- Room이 유일한 원본. 기기 변경은 Android 자동 백업 + 직접 내보내는 JSON 백업으로 해결.
- Firebase는 Spark(무료)에서 Crashlytics · Analytics · Remote Config · Hosting만. **넘어도 과금되지 않는 구성**이라 데이즈의 과금 방어(예산 알림 · 결제 자동 해제)가 필요 없다.
- 데이즈의 모듈 구조 · MVI · 디자인 시스템 패턴을 그대로 쓰고, 커플 · 동기화 · 로그인 관련 모듈은 뺀다.
- 모든 ID는 UUID, 날짜는 `yyyy-MM-dd`, 시각은 epoch millis, 금액은 Long 최소 단위.

**디자인**
- 테마는 인디고 하나 × 라이트/다크. 토큰 이름은 데이즈와 같아서 `core:designsystem` 코드를 옮겨 쓸 수 있다.
- 서비스 로고 이미지 대신 이니셜 아이콘.

**수익화**
- 통계 화면 맨 아래 인라인 배너 + 구독 저장 후 빈도 제한 전면광고 + 광고 제거 1회 구매 (`ads-strategy.md`).

## 공통 문서와 다르게 가는 부분

| 항목 | 공통 규칙 | 구독캘린더 | 이유 |
|---|---|---|---|
| Room 스키마 버전 | 스키마를 바꾸면 version을 올리고 Migration · 마이그레이션 테스트 추가 | **출시 전까지 version 1을 고쳐 쓰고** `schemas/…/1.json`을 덮어씀 | 출시 전에는 보존할 사용자 데이터가 없음. **첫 출시 빌드부터는 공통 규칙을 따름** |
| `core:network` (Ktor) | 공통 구조에 포함 | **만들지 않음** | 외부 HTTP가 없음. 환율 자동 갱신 같은 기능이 생기면 그때 추가 |
| 오프라인 우선 동기화 (ARCHITECTURE 8-3) | 서버 동기화 패턴 | **적용 안 함** | 서버가 없음 |
| detekt `TooManyFunctions` | 기본값(클래스 · 인터페이스 11개) | **override는 세지 않고 인터페이스는 15개까지** | Repository 인터페이스가 데이터 연산 목록이라 기능마다 늘어난다. 구현 · Fake는 인터페이스를 그대로 따른다 |

> 요금 · 정책 수치는 출시 전 공식 문서에서 다시 확인하세요.
