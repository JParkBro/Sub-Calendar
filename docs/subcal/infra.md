# 인프라 · 출시

> **작업 시점: 출시 단계 (최후순위).** 개인용 단계(`module-map.md` 9장 1 ~ 9단계)는 Room · DataStore만 쓰고 외부 설정이 없다. 이 문서의 Firebase · Hosting · AdMob · Play Console 작업은 모든 기능을 만든 뒤 마지막에 한다. 예외로 2장(Android 자동 백업)은 외부 설정이 필요 없어 7단계(더보기)에서 한다.

## 1. 구성 요약

| 무엇 | 어떻게 | 비용 |
|---|---|---|
| 사용자 데이터 | 기기 Room + DataStore | 0 |
| 기기 변경 · 재설치 | Android 자동 백업(사용자 Google 계정) + JSON 내보내기 | 0 |
| 오류 수집 | Firebase Crashlytics | 무료 |
| 사용 분석 | Firebase Analytics (화면 · 동작 이름만) | 무료 |
| 원격 설정 | Firebase Remote Config (광고 빈도 · 차단 스위치) | 무료 |
| 정책 페이지 | Firebase Hosting (`policy-site/public`) | Spark 무료 한도 안 |
| 광고 · 결제 | AdMob, Play Billing | 수수료만 |

**Firebase 요금제는 Spark(무료)로 고정합니다.** Spark는 한도를 넘으면 기능이 멈출 뿐 과금되지 않아서, 데이즈의 예산 알림 · 결제 자동 해제 함수 같은 과금 방어가 필요 없습니다. Blaze가 필요한 기능(Functions, Storage 등)을 쓰고 싶어지면 먼저 사용자와 상의합니다.

쓰지 않는 것: Firebase Auth, Firestore, Cloud Functions, Cloud Storage, FCM 서버 발송, App Check, 자체 서버, Cloudflare.

### Firebase 프로젝트

- 프로젝트 하나(예: `subcal-xxxxx`)에 Android 앱 2개 등록: `jpark.bro.subcal`, `jpark.bro.subcal.dev`.
- dev 빌드는 Analytics 수집을 끄고(`setAnalyticsCollectionEnabled(false)`), Crashlytics는 켠다.
- `google-services.json`은 커밋하지 않는다.

## 2. Android 자동 백업

- `android:allowBackup="true"`, `android:dataExtractionRules="@xml/data_extraction_rules"`(Android 12+), `android:fullBackupContent="@xml/backup_rules"`(11 이하).
- 포함: Room DB 파일, 설정 DataStore 파일. 제외: 광고 기록 DataStore, `widget_config`가 든 파일이 따로라면 그 파일, 캐시.
- 복원된 DB는 다음 실행에서 알림을 다시 예약한다 (`ReminderScheduler.start`가 앱 시작마다 확인하고, 같은 시각으로 예약된 작업이 있으면 그대로 둔다).
- 사용자가 기기 백업을 꺼 두면 동작하지 않으므로, 더보기 → 백업 내보내기 안내 문구에 "기기 변경 전 백업 파일을 저장해 두세요"를 둔다.

## 3. 정책 페이지 (`policy-site/`)

- 데이즈와 같은 방식: `policy-site/public/`에 `privacy.html` · `index.html` · `app-ads.txt`, `policy-site/firebase.json`에 Hosting 설정(`cleanUrls`).
- 앱에서는 `https://{project}.web.app/privacy`를 `SubCalWebView`로 연다 (테마 `?theme=light|dark`).
- 개인정보 처리방침에 적을 것: 사용자 입력은 기기에만 저장, 서버로 보내지 않음 / 백업 파일은 사용자가 관리 / 수집: 광고 ID(AdMob), 오류 · 사용 통계(Firebase, 개인 식별 정보 없음) / 문의처.
- 배포는 직접 실행하지 않고 사용자 확인 후: `npx firebase deploy --only hosting --project {project}` (`policy-site/` 안에서).

## 4. 출시 전 체크리스트

**코드**
- [ ] `core:domain` 9장 기대 값 테스트 전부 통과
- [ ] 백업 왕복 테스트, 자동 백업 복원 후 알림 재예약 확인
- [ ] 프리셋 요금을 공식 요금표로 확인하고 `version` · 기준 월 갱신
- [ ] Room 스키마 v1 고정, 이후 변경은 Migration (`README.md` 예외 종료)

**Firebase**
- [ ] Spark 요금제인지 확인 (결제 계정 연결 안 됨)
- [ ] Remote Config 기본값: `ads_enabled = true`, 빈도 3종
- [ ] Hosting 배포, 개인정보 처리방침 URL 확인

**AdMob**
- [ ] 앱 등록, 배너 · 전면 광고 단위, `local.properties`에 prod 값
- [ ] app-ads.txt(개발자 웹사이트 루트), 결제 · 세금 정보
- [ ] GDPR 메시지 설정 (또는 배포 국가를 한국으로 제한)

**Play Console**
- [ ] 인앱 상품 `remove_ads`(비소모성 ₩3,300) 등록 · 활성화, 라이선스 테스터로 구매 · 복원 확인
- [ ] 앱 콘텐츠: 광고 포함, 데이터 보안(광고 ID · 오류 로그 · 앱 상호작용, 사용자 입력 수집 없음), 개인정보 처리방침 URL
- [ ] 스토어 제목 예: "구독캘린더 - 구독료 관리, 정기결제 알림" (검색 키워드 포함)

**실기기**
- [ ] 결제 알림(설정 시각) · 체험 알림, 알림 권한 거절 시 동작
- [ ] 다크 모드 · 글꼴 2배 · 가로 화면
- [ ] 광고 빈도(설치 3일 보호는 디버그 설정으로 끄고 확인), 광고 제거 후 광고 사라짐
- [ ] 백업 내보내기 → 앱 삭제 → 재설치 → 불러오기
