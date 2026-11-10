# 모듈 구성 (구독캘린더 적용)

공통 문서 [ANDROID_ARCHITECTURE.md](../common/ANDROID_ARCHITECTURE.md)의 구조를 이 앱에 적용한 결과입니다. 패턴 · 규칙 설명은 공통 문서를 따르고, 이 문서는 **실제 모듈 · 키 · 의존 관계**만 정리합니다.

## 1. 자리표시자 값

| 자리표시자 | 값 |
|---|---|
| `{app}` | `subcal` |
| `{base.package}` | `jpark.bro.subcal` |

## 2. 저장소 구조

```
subcalendar/
├─ app/ core/ feature/ widget/ build-logic/   # Android 앱 (루트가 Gradle 프로젝트)
├─ policy-site/      # Firebase Hosting: 개인정보 처리방침, app-ads.txt
├─ e2e/              # Maestro 흐름 (도입할 때 추가)
└─ docs/
   ├─ common/        # 범용 문서 (데이즈와 동일)
   └─ subcal/          # 이 프로젝트 문서
```

## 3. Android 모듈

```
subcalendar/
├─ app/
├─ build-logic/
├─ core/
│  ├─ model/          [JVM] Subscription, SubscriptionInput, PriceChange, Pause, SubscriptionRecord, Payment, Preset, AppSettings, NotificationSettings, PartyMember, UsageLog, Category, Money, BillingCycle
│  ├─ common/         [JVM] suspendRunCatching, IdGenerator (Result는 kotlin.Result, Clock은 java.time.Clock을 주입)
│  ├─ domain/         [JVM] 결제일 생성, 결제 이력, 기간 집계, 누적 · 아낀 돈, 중복, 가성비, 파티 몫, 알림 대상
│  ├─ navigation/     Navigator, 탭별 백스택 (홈 / 목록 / 캘린더 / 통계 / 더보기), AppIntents
│  ├─ designsystem/   AppColors(27 토큰) · 2 팔레트, 서비스 색, 공통 컴포넌트 SubCal*
│  ├─ ui/             MviViewModel, ObserveEvents, MoneyText · formatMoney, 카테고리 · 결제 주기 표시, PaymentRow, DatePicker
│  ├─ database/       Room (subscription, price_change, pause, usage_log, party_member, party_settlement, widget_config)
│  ├─ datastore/      설정(화면 모드 · 알림 · 월 시작일 · 환율 · 정렬), 온보딩 완료, 광고 노출 기록, 광고 제거 캐시
│  ├─ firebase/       Crashlytics · Analytics 래퍼, Remote Config(광고 빈도 · 차단 스위치)  ※ 출시 단계
│  ├─ data/           Repository: Subscription, Party, Usage, Settings, Preset, Backup / 프리셋 JSON 읽기
│  ├─ notification/   결제 · 체험 · 정산 · 월초 요약 로컬 알림 (WorkManager, `ReminderScheduler` · `ReminderWorker` · `PaymentNotifier`)
│  ├─ ads/            AdMob 배너 · 전면광고, 맞춤 광고 동의(UMP), 빈도 규칙  ※ 출시 단계
│  ├─ billing/        Play Billing 광고 제거 (비소모성)  ※ 출시 단계
│  └─ testing/        Fake, MainDispatcherRule, 샘플 데이터(product-spec 9장)
├─ feature/
│  ├─ onboarding/    api/ impl/
│  ├─ home/          api/ impl/
│  ├─ subscription/  api/ impl/   목록 · 서비스 선택 · 입력 · 상세
│  ├─ calendar/      api/ impl/
│  ├─ insight/       api/ impl/   통계 · 아낀 돈 · 가성비 · 연말 리포트
│  ├─ trial/         api/ impl/
│  ├─ party/         api/ impl/
│  └─ settings/      api/ impl/   더보기 · 알림 설정 · 백업 · 광고 제거 · 정책
└─ widget/           Glance 위젯, 위젯 설정 Activity
```

데이즈에서 가져오지 않는 모듈: `core:network`(외부 HTTP 없음), `core:sync`(서버 없음), `feature:couple` · `feature:notifications`(알림 목록 없음). 데이즈의 `core:firebase`는 Auth · Firestore · App Check였지만 여기서는 Crashlytics · Analytics · Remote Config만 담는다.

`core:notification`의 Worker는 알림 설정과 마지막 발송일을 `core:datastore`에서 직접 읽는다 (화면이 아닌 백그라운드 작업이고, 마지막 발송일은 알림만 쓰는 내부 상태). 구독은 `core:data`로 읽는다.

`core:firebase` · `core:ads` · `core:billing`은 출시 단계(9장 10단계)에 추가한다. `core:ads` · `core:billing`은 Activity가 필요한 광고 · 결제 창이라 feature impl이 직접 참조한다 (창은 Root가 Event로 띄우고 결과를 Action으로 넘김. 데이터 접근은 그대로 `core:data`).

## 4. 내비게이션 키

### 4-1. api에 공개하는 키

| 모듈 | 키 | 인자 |
|---|---|---|
| `onboarding:api` | `OnboardingKey` | – |
| `home:api` | `HomeKey` (탭) | – |
| `subscription:api` | `SubscriptionListKey` (탭) | `category: Category?` |
| | `SubscriptionDetailKey` | `id: String` |
| | `ServicePickerKey` | – |
| | `SubscriptionEditKey` | `id: String?`, `presetId: String?`, `planIndex: Int?`, `startAsTrial: Boolean = false` |
| `calendar:api` | `CalendarKey` (탭) | – |
| `insight:api` | `StatsKey` (탭) | – |
| | `SavingsKey` | `tab: SavingsTab = CONSIDER` |
| | `ValueAnalysisKey` | – |
| | `YearReportKey` | `year: Int` |
| `trial:api` | `TrialKey` | – |
| `party:api` | `PartyKey` | `subscriptionId: String` |
| | `PartyListKey` | – |
| `settings:api` | `MoreKey` (탭) | – |
| | `NotificationSettingsKey` | – |

`category: Category`를 쓰는 `subscription:api`는 `build.gradle.kts`에 `api(projects.core.model)`을 추가한다 (`subcal.feature.api` 플러그인에는 없음, 데이즈 `feature:item:api`와 같은 방식). `SavingsTab`은 `insight:api`의 `@Serializable` enum이다.

### 4-2. impl 내부 키 (internal)

| 모듈 | 키 |
|---|---|
| `subscription:impl` | `PaymentHistoryKey(id)` |
| `settings:impl` | `WidgetSettingsKey`, `RemoveAdsKey`, `PolicyListKey`, `PolicyDocKey(type)`, `MoreAppsKey` |

### 4-3. app에서 결정하는 목적지

| 상황 | 결과 타입 (api) | app의 처리 |
|---|---|---|
| 앱 시작 | – | 온보딩 미완료 → `OnboardingKey`, 완료 → `HomeKey` |
| 온보딩 완료 · 건너뛰기 | `OnboardingResult.Done` | `replaceAll(HomeKey)` |
| 모든 데이터 삭제 · 백업 불러오기 | `DataReset` | `replaceAll(HomeKey)` (삭제는 `OnboardingKey`) |
| 알림 · 위젯 탭 | `AppIntents.EXTRA_OPEN_SUBSCRIPTION_ID` | `[HomeKey, SubscriptionDetailKey]` 백스택 |
| 체험 알림 탭 | `AppIntents.EXTRA_OPEN_TRIAL` | `[HomeKey, TrialKey]` |

탭 전환(홈의 "전체보기" → 캘린더 탭, 카테고리 칩 → 목록 탭)은 `Navigator.switchTab(key)`로 하고, 대상 탭의 백스택 맨 아래 키를 인자와 함께 교체한다. `SubscriptionListKey`는 인자가 있어 `Navigator`는 탭을 키 값이 아니라 키 클래스로 구분한다.

## 5. 기능 간 의존 관계

| impl | 의존하는 api |
|---|---|
| `onboarding:impl` | – (결과만 알리고 app이 목적지 결정) |
| `home:impl` | `subscription`, `calendar`, `trial`, `insight`(연말 리포트 배너) |
| `subscription:impl` | `party` |
| `calendar:impl` | `subscription` |
| `insight:impl` | `subscription` |
| `trial:impl` | `subscription` |
| `party:impl` | – |
| `settings:impl` | `trial`, `party`, `insight` |
| `widget` | – (`AppIntents`로 앱을 열고 app이 백스택을 만든다) |

## 6. 화면별 MVI 구성

| 화면 | Event | 주요 Event |
|---|---|---|
| 온보딩 | 있음 | 완료 결과 전달 |
| 홈 | 있음 | 상세 · 서비스 선택 · 무료체험 이동, 탭 전환 |
| 목록 | 있음 | 상세 · 서비스 선택 이동 |
| 서비스 선택 | 있음 | 입력 화면 이동 |
| 구독 입력 · 수정 | 있음 | 저장 후 뒤로, 알림 권한 요청, 전면광고, 삭제 후 뒤로 |
| 구독 상세 | 있음 | 수정 · 파티 이동, 해지 주소 열기, 뒤로(삭제됨) |
| 결제 이력 | 없음 (`Nothing`) | – |
| 캘린더 | 있음 | 상세 이동 |
| 통계 | 있음 | 아낀 돈 · 가성비 이동, 탭 전환 |
| 아낀 돈 · 해지 고민 | 있음 | 상세 이동, 해지 주소 열기 |
| 가성비 분석 | 있음 | 상세 이동 |
| 연말 리포트 | 있음 | 공유 시트 |
| 무료체험 | 있음 | 입력 이동, 결과 저장 스낵바 |
| 파티 정산 | 있음 | 공유 시트, 스낵바 |
| 더보기 | 있음 | 하위 화면 이동, 파일 저장 · 선택 창, 메일 앱 |
| 알림 설정 | 있음 | 시스템 알림 설정 열기 |
| 광고 제거 | 있음 | 결제 창, 결과 스낵바 |
| 위젯 설정 | 있음 | 위젯 추가 요청 실패 안내 |
| 정책 문서 | 없음 (`Nothing`) | – |

## 7. Koin 모듈

| 모듈 | 정의 위치 |
|---|---|
| `appModule` | app — Navigator, 결과 핸들러, 전체 `includes` |
| `databaseModule`, `datastoreModule` | core |
| `firebaseModule` | core:firebase — `CrashReporter`, `AnalyticsLogger`, `RemoteFlags` (출시 단계) |
| `dataModule` | core:data (`core:domain`은 순수 함수만 있어 Koin 모듈이 없다) |
| `notificationModule` | core |
| `adsModule`, `billingModule` | core (출시 단계) |
| `{feature}Module` × 8 | 각 impl |
| `widgetModule` | widget |

## 8. 빌드 변형

Firebase · AdMob 열은 출시 단계(9장 10단계)부터 적용한다. 그 전에는 flavor가 앱 ID만 나눈다.

| flavor | Firebase | AdMob | 앱 ID |
|---|---|---|---|
| `dev` | 같은 Spark 프로젝트, Analytics 수집 끔 | Google 샘플 (테스트 광고) | `jpark.bro.subcal.dev` |
| `prod` | Spark 프로젝트 | `admob.appId` · `admob.bannerAdUnitId` · `admob.interstitialAdUnitId` (`local.properties`, 없으면 샘플) | `jpark.bro.subcal` |

## 9. 개발 순서

`build-logic/`(패키지 `jpark.bro.subcal.convention`, 플러그인 ID `subcal.*`)은 이미 옮겼다. 데이즈 저장소(`../Days`)의 `core:common`, `core:navigation`, `core:ui`(MviViewModel), `core:designsystem` 구조를 복사하고, 패키지를 `jpark.bro.subcal`로 바꾼다.

1. **기반**: `core:model`, `core:common` (프로젝트 골격 build-logic · flavor · ktlint · detekt는 완료)
2. **계산**: `core:domain` 결제일 · 기간 집계 · 누적 + 경계값 테스트 (product-spec 2장, 9장 기대 값을 테스트로)
3. **디자인 시스템**: `core:designsystem` 인디고 2 팔레트, `SubCal*` 컴포넌트, 서비스 색, `SubCalServiceIcon`, `core:ui` `MoneyText`
4. **데이터**: `core:database` v1(subscription · price_change · pause · widget_config) + DAO 테스트, `core:datastore`, `core:data`(Subscription · Settings · Preset Repository), 프리셋 JSON
5. **1차 화면**: `feature:subscription`(목록 · 선택 · 입력 · 상세) → `feature:home` → `feature:calendar` → `feature:onboarding`
6. **알림**: `core:notification`(ReminderScheduler · ReminderWorker · 채널 `payment`), `feature:settings` 골격(더보기 탭은 알림 메뉴만) + 알림 설정 화면, 첫 저장 때 알림 권한 요청
7. **더보기 (로컬 메뉴)**: `feature:settings`(알림 · 월 시작 기준일 · 달러 환율 · 기본 결제 수단 · 화면 모드 · 백업 내보내기/불러오기 · 문의하기 · 오픈소스 라이선스 · 앱 버전 · 모든 데이터 삭제), Android 자동 백업 규칙
8. **2차 기능**: `feature:insight`(통계 · 아낀 돈), `feature:trial`, 체험 알림, `widget`, 월초 요약
9. **3차 기능**: `usage_log` · `party_member` · `party_settlement` 테이블, `feature:party`, 가성비 분석, 연말 리포트
10. **출시 단계 (최후순위)**: 아래 "출시 단계 작업" 전부

### 개인용 단계와 출시 단계

1 ~ 9단계는 **개인용 단계**다. 저장소는 Room · DataStore(Preferences)만 쓰고, 사용자가 외부 콘솔 · 계정에서 설정할 일이 없어야 한다. 외부 설정이 필요한 작업은 모두 10단계로 미룬다.

| 출시 단계 작업 | 사용자가 외부에서 할 일 |
|---|---|
| `core:firebase`(Crashlytics · Analytics · Remote Config) | Firebase 프로젝트 생성, 앱 2개 등록, `google-services.json` 받기 |
| `core:ads`(배너 · 전면 · UMP), 구독 저장 후 전면광고, 통계 배너 | AdMob 앱 · 광고 단위 등록, `local.properties`에 prod 값, GDPR 메시지 |
| `core:billing`, 광고 제거 화면 | Play Console 인앱 상품 `remove_ads` 등록 |
| `policy-site/`, 개인정보 처리방침 웹뷰 | Firebase Hosting 배포 |
| 더보기의 광고 제거 · 앱 평가하기 · 다른 앱 둘러보기 · 개인정보 처리방침 메뉴 | Play 스토어 등록 |
| 출시 체크리스트 (`infra.md` 4장) | Play Console 앱 콘텐츠 · 스토어 정보 |

- 개인용 단계 빌드에는 출시 단계 메뉴 · 광고 자리를 넣지 않는다 (비활성 표시도 하지 않음).
- 출시 단계 모듈이 없어도 앱이 빌드 · 실행되어야 한다. 화면 · ViewModel이 광고 · 결제 · Firebase를 미리 참조하지 않는다.
