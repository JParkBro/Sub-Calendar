# 테마 컬러 시스템 가이드

> 색상 값의 원본은 `assets/theme-tokens.json`입니다. 토큰 이름 · 역할 · 컴포넌트 매핑은 **데이즈(Days)의 theme-guide와 같습니다.** 이 문서는 값과 이 앱에서 달라지는 규칙만 적습니다. 데이즈의 `core:designsystem`을 옮겨 쓸 때 팔레트 값만 바꾸면 됩니다. 데이즈 문서는 이 저장소 밖 `../Days/docs/days/theme-guide.md`에 있습니다. 화면 레이아웃 시안은 `design/{화면}/`(목록: [product-spec.md](product-spec.md) 3-2)이고, 시안의 색 · 글꼴은 따르지 않습니다.

테마는 **인디고 하나 × 라이트/다크 = 2 팔레트**입니다. 돈을 다루는 앱이라 차분하고 믿음직한 인상을 목표로 하고, 강조색은 화면에서 꼭 봐야 할 곳에만 씁니다.

## 1. 테마 전환

| 설정 | 동작 |
|---|---|
| 화면 모드 | 라이트 · 다크 · 시스템(기본). 더보기 → 설정 → 화면 모드 |
| 테마 선택 | 없음 (팔레트 하나). 나중에 테마를 추가해도 토큰 이름은 그대로 |

## 2. 토큰 (27개, 데이즈와 같은 이름)

브랜드 `primary` · `primary-pressed` · `on-primary` · `primary-soft` · `on-primary-soft`, 보조 `accent` · `accent-soft` · `on-accent-soft`, 표면 `bg` · `surface` · `surface-raised` · `fill` · `overlay`, 선 `border` · `border-strong`, 텍스트 `text-primary` · `text-secondary` · `text-tertiary`, 상태 `danger` · `danger-soft` · `success` · `success-soft` · `warning` · `warning-soft`, 캘린더 `cal-sunday` · `cal-saturday`, 효과 `shadow`.

이 앱에서 용도가 바뀌는 토큰:

| 토큰 | 데이즈 | 구독캘린더 |
|---|---|---|
| `primary` | 대표 디데이 숫자, 활성 탭, FAB | 이번 달 금액, 활성 탭, FAB, 주 버튼 |
| `primary-soft` | 히어로 카드, "우리 둘이" 뱃지 | 이번 달 카드 배경, 선택된 프리셋 카드, 강조 요약 카드("연 n원") |
| `accent` | 진행바 끝, 일정 마커 | 진행바 끝(무료체험), 차트 보조색 |
| `accent-soft` | "매년" · 카테고리 뱃지 | 주기 뱃지("매년" · "3개월마다"), 카테고리 뱃지 |
| `danger` | 7일 이내 디데이 | 3일 이내 결제 D-day, 체험 종료 1일 이내 테두리, 가격 인상, "사용 안 함" |
| `success` | 연결됨 | 아낀 돈 금액, "받음" 정산 칩, 가격 인하 |
| `warning` | 코드 만료 임박 | 중복 구독 · "결제됐나요?" 안내 박스 |

## 3. 팔레트 값

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#3D4FD6` | `#8E9BFF` |
| `primary-pressed` | `#3141B8` | `#A7B1FF` |
| `on-primary` | `#FFFFFF` | `#111640` |
| `primary-soft` | `#ECEEFC` | `#242A58` |
| `on-primary-soft` | `#2A3699` | `#D4D9FF` |
| `accent` | `#14A38F` | `#3FCDB8` |
| `accent-soft` | `#E2F5F1` | `#123A35` |
| `on-accent-soft` | `#0B6457` | `#A3EDE2` |
| `bg` | `#F6F7F9` | `#111318` |
| `surface` | `#FFFFFF` | `#1A1D24` |
| `surface-raised` | `#FFFFFF` | `#242833` |
| `fill` | `#EEF0F3` | `#2A2E38` |
| `overlay` | `#0F121E73` | `#000000A6` |
| `border` | `#E4E7EC` | `#2C313C` |
| `border-strong` | `#C2C7D0` | `#4A505D` |
| `text-primary` | `#161A23` | `#EDEFF3` |
| `text-secondary` | `#5A616E` | `#A9AFBB` |
| `text-tertiary` | `#848A95` | `#737A88` |
| `danger` | `#C62828` | `#FF8A80` |
| `danger-soft` | `#FDECEC` | `#3B1F1E` |
| `success` | `#127235` | `#6DD58C` |
| `success-soft` | `#E6F4EA` | `#17331F` |
| `warning` | `#B45309` | `#F5B35B` |
| `warning-soft` | `#FEF3E2` | `#3A2A14` |
| `cal-sunday` | `#C62828` | `#FF8A80` |
| `cal-saturday` | `#3156C4` | `#93ABFF` |
| `shadow` | `#3D4FD61F` | `#00000066` |

`overlay` · `shadow`는 8자리(`#RRGGBBAA`) 값입니다.

## 4. 명도 대비

WCAG: 일반 텍스트 4.5:1, 큰 텍스트 · UI 그래픽 3:1.

| 조합 | 용도 | 라이트 | 다크 |
|---|---|---|---|
| `on-primary` / `primary` | 버튼 글자 | 6.39 | 6.83 |
| `on-primary-soft` / `primary-soft` | 요약 카드 라벨 | 8.73 | 9.80 |
| `on-accent-soft` / `accent-soft` | 주기 · 카테고리 뱃지 | 6.24 | 9.37 |
| `text-primary` / `bg` | 본문 | 16.24 | 16.14 |
| `text-secondary` / `surface` | 보조 텍스트 | 6.23 | 7.66 |
| `text-tertiary` / `surface` | 비활성 · 플레이스홀더 | 3.47 | 3.91 |
| `primary` / `surface` | 카드 위 금액 | 6.39 | 6.65 |
| `danger` / `surface` | 임박 D-day | 5.62 | 7.39 |
| `danger` / `danger-soft` | 임박 뱃지 | 4.92 | 6.57 |
| `success` / `success-soft` | 받음 칩 | 5.31 | 7.55 |
| `warning` / `warning-soft` | 주의 안내 | 4.58 | 7.55 |

`text-tertiary`는 읽어야 하는 정보에 쓰지 않습니다. `accent`는 라이트에서 흰 배경 대비가 낮아 글자색으로 쓰지 않습니다.

## 5. 테마와 무관한 고정 색

### 서비스 색 10종

서비스 아이콘(`SubCalServiceIcon`) 배경색입니다. 라이트 · 다크 공통, 이니셜은 흰색(`#FFFFFF`) 굵게. 모두 흰 글자 대비 4.5:1 이상입니다. 프리셋은 브랜드와 비슷한 계열을 고르되 **브랜드 색을 그대로 복제하지 않습니다**.

| `color_key` | 값 | 흰 글자 대비 |
|---|---|---|
| `RED` | `#D93025` | 4.77 |
| `ORANGE` | `#C2570C` | 4.50 |
| `AMBER` | `#A86200` | 4.76 |
| `GREEN` | `#188038` | 5.02 |
| `TEAL` | `#00796B` | 5.32 |
| `BLUE` | `#1A73E8` | 4.51 |
| `PURPLE` | `#8430CE` | 6.37 |
| `PINK` | `#C2185B` | 5.87 |
| `BROWN` | `#795548` | 6.55 |
| `BLACK` | `#202124` | 16.10 |

이니셜 규칙: 한글 이름은 첫 글자 1자(넷플릭스 → 넷), 영문은 첫 두 단어의 머리글자 대문자(YouTube Premium → YP), 한 단어면 첫 글자(Spotify → S).

### 차트 색

통계 도넛 · 막대는 카테고리 순서대로 `primary` → `accent` → `BLUE` → `AMBER` → `PURPLE` → `PINK` → `TEAL` → `text-tertiary`(기타). 색만으로 구분하지 않고 범례에 이름 · 비율을 함께 씁니다.

## 6. 사용 규칙

1. **primary는 화면당 한두 곳.** 홈은 이번 달 금액과 FAB, 상세는 다음 결제일 정도.
2. **금액 색.** 기본 `text-primary`. 이번 달 대표 금액만 `primary`. 아낀 돈은 `success`. 지난달 대비 증가는 `danger`, 감소는 `success`이고 "+" · "−" 기호를 함께 표시.
3. **D-day 색.** 오늘 ~ 3일 이내 `danger`, 나머지 `text-primary`, 지난 결제 · 해지한 구독 `text-tertiary`.
4. **숫자는 tabular-nums.** 금액 · D-day · 횟수 모두. `MoneyText`가 처리.
5. **soft 배경에는 짝이 되는 on 색**, 보조 요소는 `fill` (데이즈 5.2 · 5.3과 같음).
6. **해지 · 일시정지 구독**은 아이콘에 50% 투명도, 이름은 `text-secondary`.
7. **하드코딩 금지.** 위젯도 같은 값을 `AppColors`에서 가져온다.

## 7. 타이포그래피 · 모서리 · 간격 · 공통 컴포넌트

타이포그래피(Pretendard, 스타일 12종), 모서리 · 간격 값은 **데이즈 theme-guide 9 · 10장과 같습니다.** 이 앱에서 쓰는 이름만 바꿉니다: `displayHeroMobile` → 이번 달 금액, `metricTabular` → 목록 금액.

공통 컴포넌트는 데이즈의 `Days*`를 `SubCal*`로 이름만 바꿔 옮기고, 아래를 추가 · 제외합니다. 경로는 `core/designsystem/.../component/`.

| 구분 | 컴포넌트 |
|---|---|
| 그대로 | `SubCalPrimaryButton` · `SubCalSecondaryButton` · `SubCalOutlineButton` · `SubCalDangerButton`, `SubCalIconButton` · `SubCalFab`, `SubCalFilterChip` · `SubCalSegmentedControl`, `SubCalCard` · `SubCalSection` · `SubCalListItem` · `SubCalDivider`, `SubCalTextField`, `SubCalSwitch` · `SubCalCheckbox`, `SubCalProgressBar`, `SubCalTopBar` · `SubCalBottomBar`(탭 5개), `SubCalBottomSheet`, `SubCalInfoBox` · `SubCalToast` |
| 바꿈 | `SubCalBadge(tone = CYCLE · CATEGORY · SOON · SUCCESS · PAUSED · CANCELLED)`, `DdayText` → `SubCalDdayText(daysUntil)` (6장 3번 색 규칙), `SubCalPrimaryButton(onLongClick)`(사용 체크 "+1" 길게 눌러 날짜 선택), `SubCalToast(actionLabel)`(사용 기록 "실행 취소". `SubCalToastHost`가 스낵바의 actionLabel을 넘긴다), `SubCalProgressBar(color)`(가성비 "사용 안 함"은 `danger` 한 색), `SubCalAmountField(onCurrencyChange = null)`(통화 선택 숨김, 파티 몫 입력) |
| 추가 | `SubCalServiceIcon(name, colorKey, size, dimmed)`, `SubCalAmountField`(숫자 키패드 · 천 단위 쉼표 · 통화 선택), `SubCalDonutChart` · `SubCalBarChart`(Canvas, 외부 차트 라이브러리 없음) |
| 제외 | `DaysKakaoButton`, `DaysItemIcon`, 커플 · 사진 관련 컴포넌트 |

`core:ui`: `MoneyText(money, style, tone)` · `formatMoney` · `formatKrwInManwon`(만 원 단위), `PaymentRow`(아이콘 · 이름 · 날짜 · D-day · 금액 한 줄), `SubCalDatePickerDialog`, `SubCalWebView`(정책 문서 · 다른 앱 둘러보기).

## 8. Android(Compose) 적용

데이즈와 같은 구조입니다. `AppColors`(27 토큰), 팔레트 값 `ThemeTokens.kt`(인디고 라이트 · 다크), `AppFixedColors.service(colorKey)`, `AppThemeTokens.colors`. `AppTheme(mode)`가 `MaterialTheme`의 colorScheme도 토큰에서 채워 시스템 컴포넌트(날짜 선택기 등) 색을 맞춥니다. 데이즈의 `AppThemeType`(테마 선택)은 팔레트가 하나라 옮기지 않습니다.
