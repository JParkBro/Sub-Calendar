# 인프라 · 출시

> **작업 시점: 출시 단계 = 2.0.0 (최후순위).** 개인용 단계(`module-map.md` 9장 1 ~ 9단계, 1.x)는 Room · DataStore만 쓰고 외부 설정이 없다. 이 문서의 Firebase · Hosting · AdMob · Play Console 작업과 로그인 · 클라우드 백업(`account-backup.md`)은 모든 기능을 만든 뒤 마지막에 한다. 예외로 2장(Android 자동 백업)은 외부 설정이 필요 없어 7단계(더보기)에서 한다.

## 1. 구성 요약

| 무엇 | 어떻게 | 비용 |
|---|---|---|
| 사용자 데이터 | 기기 Room + DataStore | 0 |
| 기기 변경 · 재설치 | Android 자동 백업(사용자 Google 계정) + JSON 내보내기 | 0 |
| 로그인 (2.0.0) | Firebase Auth: Google · 익명, 카카오는 Cloud Functions 커스텀 토큰 | 무료 한도 안 |
| 클라우드 백업 (2.0.0) | Firestore `users/{uid}/backups/latest` 문서 1개 | 무료 한도 안 |
| 오류 수집 | Firebase Crashlytics | 무료 |
| 사용 분석 | Firebase Analytics (화면 · 동작 이름만) | 무료 |
| 원격 설정 | Firebase Remote Config (광고 빈도 · 차단 스위치) | 무료 |
| 정책 페이지 | Firebase Hosting (`policy-site/public`) | 무료 한도 안 |
| 광고 · 결제 | AdMob, Play Billing | 수수료만 |

**Firebase 요금제는 Blaze입니다 (2026-11-10 결정).** 카카오 로그인의 커스텀 토큰 함수가 Cloud Functions(Blaze 전용)라서 올립니다. Blaze는 한도를 넘으면 과금되므로 Cloud Billing **예산 알림을 반드시 걸고**, Firestore · Functions를 무료 한도 안에서 쓰도록 설계합니다 (`account-backup.md` 4장). 1.x(개인용)는 Firebase를 쓰지 않습니다.

쓰지 않는 것: Cloud Storage, 실시간 동기화, FCM 서버 발송, Firestore를 쓰는 다른 기능, 자체 서버, Cloudflare. App Check는 2.0.0에서 Firestore · Functions 보호용으로 쓴다.

### Firebase 프로젝트

- 프로젝트 하나(예: `subcal-xxxxx`)에 Android 앱 2개 등록: `jpark.bro.subcal`, `jpark.bro.subcal.dev`.
- dev 빌드는 Analytics 수집을 끄고(`setAnalyticsCollectionEnabled(false)`), Crashlytics는 켠다.
- `google-services.json`은 커밋하지 않는다.

## 2. Android 자동 백업

- `android:allowBackup="true"`, `android:dataExtractionRules="@xml/data_extraction_rules"`(Android 12+), `android:fullBackupContent="@xml/backup_rules"`(11 이하).
- 포함: Room DB 파일(`subcal.db` · `-wal` · `-shm`), 설정 DataStore 파일(`datastore/settings.preferences_pb`). include 목록에 없는 파일(WorkManager DB, 캐시, 이후 생길 광고 기록 DataStore 등)은 백업하지 않는다. `widget_config`는 같은 DB 안이라 함께 백업되지만 복원 뒤 위젯 id가 달라 쓰이지 않으므로, 앱 시작 때 `WidgetUpdater`가 홈 화면에 놓여 있지 않은 위젯의 행을 지운다.
- 복원된 DB는 다음 실행에서 알림을 다시 예약한다 (`ReminderScheduler.start`가 앱 시작마다 확인하고, 같은 시각으로 예약된 작업이 있으면 그대로 둔다).
- 사용자가 기기 백업을 꺼 두면 동작하지 않으므로, 더보기 → 백업 내보내기 안내 문구에 "기기 변경 전 백업 파일을 저장해 두세요"를 둔다.

## 3. 정책 페이지 (`policy-site/`)

- 데이즈와 같은 방식: `policy-site/public/`에 `privacy.html` · `index.html` · `app-ads.txt`, `policy-site/firebase.json`에 Hosting 설정(`cleanUrls`).
- 앱에서는 `https://{project}.web.app/privacy`를 `SubCalWebView`로 연다 (테마 `?theme=light|dark`).
- 개인정보 처리방침에 적을 것: 사용자 입력은 기기에 저장 / 로그인하고 클라우드 백업을 쓰면 백업 내용(구독 · 메모 · 파티 멤버 이름 등)과 계정 식별자(Firebase uid · 카카오 회원번호)를 Firebase에 저장 / 계정 삭제 시 함께 삭제 / 백업 파일은 사용자가 관리 / 수집: 광고 ID(AdMob), 오류 · 사용 통계(Firebase, 개인 식별 정보 없음) / 문의처.
- 계정 삭제 안내 페이지(`delete-account.html`): Play 정책상 웹에서 삭제를 요청하는 방법 (`account-backup.md` 5장).
- 배포는 직접 실행하지 않고 사용자 확인 후: `npx firebase deploy --only hosting --project {project}` (`policy-site/` 안에서).

## 4. 출시 전 체크리스트

**코드**
- [ ] `core:domain` 9장 기대 값 테스트 전부 통과
- [ ] 백업 왕복 테스트, 자동 백업 복원 후 알림 재예약 확인
- [ ] 프리셋 요금을 공식 요금표로 확인하고 `version` · 기준 월 갱신
- [ ] Room 스키마 v1 고정, 이후 변경은 Migration (`README.md` 예외 종료)

**Firebase**
- [ ] Blaze 요금제 + Cloud Billing 예산 알림 (50 · 90 · 100%)
- [ ] Authentication: Google · 익명 사용 설정, Firestore(`asia-northeast3`) · 보안 규칙 배포, Functions(`kakaoCustomToken`) 배포, App Check(Play Integrity)
- [ ] SHA-1 · SHA-256 (업로드 · Play 앱 서명 · debug) 등록
- [ ] Remote Config 기본값: `ads_enabled = true`, 빈도 3종
- [ ] Hosting 배포, 개인정보 처리방침 URL 확인

**AdMob**
- [ ] 앱 등록, 배너 · 전면 광고 단위, `local.properties`에 prod 값
- [ ] app-ads.txt(개발자 웹사이트 루트), 결제 · 세금 정보
- [ ] GDPR 메시지 설정 (또는 배포 국가를 한국으로 제한)

**Kakao Developers**
- [ ] 앱 만들기, Android 플랫폼(패키지 2개 · 키 해시 3종), 카카오 로그인 활성화, 동의 항목은 회원번호만
- [ ] 네이티브 앱 키는 `local.properties`, REST API 키는 Functions 비밀값

**Play Console**
- [ ] 인앱 상품 `remove_ads`(비소모성 ₩3,300) 등록 · 활성화, 라이선스 테스터로 구매 · 복원 확인
- [ ] 앱 콘텐츠: 광고 포함, 데이터 보안(광고 ID · 오류 로그 · 앱 상호작용, 로그인 사용자의 사용자 ID · 백업 콘텐츠), 개인정보 처리방침 URL, 계정 삭제 URL
- [ ] 스토어 제목 예: "구독캘린더 - 구독료 관리, 정기결제 알림" (검색 키워드 포함)

**실기기**
- [ ] 결제 알림(설정 시각) · 체험 알림, 알림 권한 거절 시 동작
- [ ] 다크 모드 · 글꼴 2배 · 가로 화면
- [ ] 광고 빈도(설치 3일 보호는 디버그 설정으로 끄고 확인), 광고 제거 후 광고 사라짐
- [ ] 백업 내보내기 → 앱 삭제 → 재설치 → 불러오기
