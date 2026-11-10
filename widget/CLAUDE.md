# widget 모듈 (Glance)

기획 원문: `docs/subcal/product-spec.md` 7장. 시안: `docs/subcal/design/widget/`. 데이즈 `widget` 모듈의 구조(크기별 `GlanceAppWidget` · 리시버 · 설정 Activity · 자정 Worker)를 그대로 따르고 표시 내용만 바꾼다. 2차 범위.

## 구조

| 파일 | 역할 |
|---|---|
| `SmallPaymentWidget` · `SmallPaymentWidgetReceiver` | 2×2: 가장 가까운 결제 1건 |
| `MediumSummaryWidget` · `MediumSummaryWidgetReceiver` | 4×2: 이번 달 구독료 + 다가오는 결제 2건 |
| `res/xml/widget_small_info.xml` · `widget_medium_info.xml` | `appwidget-provider`. `widgetFeatures="reconfigurable|configuration_optional"` |
| `WidgetConfigActivity` | 배경 선택(테마 · 다크 · 화이트) + 미리보기 + 저장 |
| `WidgetUpdater` | 구독 · 앱 설정 · 위젯 설정 흐름을 지켜보다 전체 위젯 갱신 + 자정 예약 (앱 시작 때 `start()`) |
| `WidgetTimeChangedReceiver` | 기기 시각 · 시간대 변경 시 갱신 |
| `WidgetDisplay` | `buildWidgetDisplay`: 홈과 같은 함수로 이번 달 구독료 · 다가오는 결제 |
| `WidgetMidnightWorker` | 매일 자정 갱신 후 다음 자정으로 다시 예약 |
| `di/WidgetModule.kt` | `widgetModule` |

## 규칙

- **표시 데이터는 `core:data` Repository + `core:domain` 계산만 쓴다.** 위젯 안에서 결제일 · 합계를 따로 계산하지 않는다 (홈과 숫자가 달라지면 안 됨).
- feature 모듈은 widget을 모른다. 데이터가 바뀌면 `WidgetUpdater`가 Room · DataStore 흐름으로 알아채므로 feature에서 갱신을 부르지 않는다. 놓인 위젯 목록 · 위젯 추가 요청이 필요한 더보기 위젯 설정은 `core:data`의 `HomeWidgets` 인터페이스로 하고 구현을 이 모듈에 둔다(위젯 2/2 PR).
- 금액은 `formatMoney`, 서비스 아이콘은 Glance용 `ServiceIconGlance`(원 + 이니셜, `AppFixedColors.service`)로 그린다. 브랜드 로고 이미지 금지.
- 색은 `AppColors` 토큰 값을 Glance `ColorProvider`(라이트 · 다크 쌍)로 바꿔 쓴다. 하드코딩 금지.
- 문자열은 이 모듈 `strings.xml` (`widget_{size}_{purpose}`).
- 위젯 리시버 · Worker는 Android 진입점이라 `KoinComponent`로 꺼낼 수 있다 (여기서만 예외).

## 표시

| 크기 | 내용 | 비었을 때 | 탭 |
|---|---|---|---|
| 작게 | 아이콘 · 이름 · D-day(`danger` 규칙) · 금액 | "다가오는 결제가 없어요" | 그 구독 상세 (`AppIntents.EXTRA_OPEN_SUBSCRIPTION_ID`) |
| 중간 | "{M}월 구독료" + 금액, 다가오는 결제 2행 | "구독을 등록해 보세요" | 홈 |

- 다가오는 결제 = 홈과 같은 함수(`upcomingPayments(limit)`), 체험 PENDING 제외.
- 광고 제거 여부와 상관없이 위젯에는 광고가 없다.

## 갱신

- 구독 · 가격 변경 · 일시정지 · 해지 · 설정(월 시작일 · 환율) · 위젯 설정 변경 → `WidgetUpdater`가 흐름으로 알아채 `updateAll()`. 화면 모드는 위젯이 기기 모드를 따르므로 갱신할 필요 없음
- 매일 자정 (`WidgetMidnightWorker`), `ACTION_TIME_CHANGED` · `ACTION_TIMEZONE_CHANGED` · `ACTION_DATE_CHANGED`
- 백업 불러오기 · 모든 데이터 삭제 후
- 위젯을 지우면 `widget_config` 행도 지운다 (`onDeleted`)

## 설정

- 놓을 때는 설정 없이 바로 놓이고 기본값(테마 배경) 저장 (`saveConfigIfAbsent`).
- 길게 눌러 설정 · 더보기 → 위젯 설정 목록에서 `WidgetConfigActivity`를 연다. 넘어온 appWidgetId가 이 앱 위젯인지 확인한다.
- 더보기 → 위젯 설정: 놓인 위젯 목록, 없으면 추가 방법 안내 + "위젯 추가"(`requestPinAppWidget`, 런처가 지원하지 않으면 버튼 숨김).

## 테스트

- 크기별 Glance Preview (라이트 · 다크, 빈 상태)
- 자정 예약 시각 계산 (자정 · 서머타임 · 시간대 변경)
- 홈과 위젯의 "이번 달 구독료" · 다가오는 결제가 같은지 (같은 Fake 데이터로 비교)

## 완료 전 확인

```bash
./gradlew :widget:ktlintCheck :widget:detekt :widget:testDevDebugUnitTest
```
