# 계정 · 클라우드 백업 (2.0.0)

> **작업 시점: 2.0.0 (출시 단계).** 1.x(개인용 단계)는 로그인 · 서버가 없다. 이 문서는 2026-11-10에 정한 2.0.0 범위이고, "결정 필요"로 표시한 항목은 구현 전에 사용자에게 확인한다.

## 1. 결정

| 항목 | 정한 내용 |
|---|---|
| 목적 | **클라우드 백업 · 복원.** 기기를 바꾸거나 다시 설치해도 로그인하면 데이터를 되찾는다 |
| 데이터 원본 | **Room이 여전히 유일한 원본.** 서버에는 백업 사본만 둔다. 화면은 Room만 구독한다 |
| 실시간 동기화 | 하지 않는다 (여러 기기 동시 사용 · 충돌 처리 없음) |
| 로그인 필수 여부 | 아니다. 로그인 없이 설치 즉시 쓰는 원칙은 그대로. 로그인은 더보기 → 계정에서 선택 |
| 로그인 종류 | Google, 카카오, 익명(나중에 Google · 카카오에 연결) |
| Firebase 요금제 | **Blaze** (카카오 로그인에 Cloud Functions가 필요). 예산 알림 필수 (4장) |
| 백업 형식 | 기존 백업 JSON(`data-model-erd.md` 6장) 그대로. 파일 내보내기와 같은 `BackupRepository.export` / `restore`를 쓴다 |

## 2. 로그인

| 종류 | 방식 |
|---|---|
| Google | Credential Manager(`androidx.credentials` + Google ID 토큰) → `GoogleAuthProvider.getCredential` → Firebase Auth |
| 카카오 | Kakao SDK로 로그인(카카오톡 앱 또는 카카오계정) → 액세스 토큰을 Cloud Functions `kakaoCustomToken`에 보냄 → 함수가 카카오 API(`/v2/user/me`)로 토큰을 확인하고 uid `kakao:{카카오 회원번호}`로 Firebase 커스텀 토큰 발급 → `signInWithCustomToken` |
| 익명 | `signInAnonymously`. 나중에 Google은 `linkWithCredential`로 같은 uid에 연결 |

- 카카오를 Identity Platform OIDC로 붙이는 방법도 있지만 OIDC는 무료 MAU 한도가 매우 작아(공식 요금표 확인 필요) 커스텀 토큰 함수를 쓴다.
- 익명 → 카카오는 커스텀 토큰이라 uid를 이어 붙일 수 없다. 카카오로 로그인하면 새 uid가 되고, 기기의 Room 데이터를 그 계정에 다시 백업한다 (원본이 기기라 잃는 데이터는 없음).
- **결정 필요:** 익명 계정은 앱을 지우면 다시 찾을 수 없어 클라우드 백업의 목적(재설치 복원)을 이루지 못한다. 익명을 "로그인 전 임시 상태"로만 쓸지, 익명 백업을 아예 막을지 정한다.
- 로그아웃: Firebase · 카카오 모두 로그아웃. 기기 데이터는 지우지 않는다.

## 3. 클라우드 백업 · 복원

| 항목 | 내용 |
|---|---|
| 저장 위치 | Firestore `users/{uid}/backups/latest` 문서 1개: `json`(백업 JSON 문자열), `exportedAt`, `appVersion`, `subscriptionCount` |
| 크기 | Firestore 문서 한도 1MiB 안. 구독 수백 개 · 사용 기록 수천 건 수준이면 충분. 넘으면 백업을 막고 파일 내보내기를 안내 |
| 백업 | 더보기 → 계정 → "지금 백업". **결정 필요:** 자동 백업(예: 데이터가 바뀐 날 하루 1회 WorkManager) 여부 |
| 복원 | 로그인 직후 서버 백업이 있고 기기에 데이터가 없으면 "백업에서 불러올까요?(구독 n개, 날짜)". 더보기 → 계정 → "백업에서 불러오기"는 기존 불러오기처럼 **덮어쓰기만**(확인 후) |
| 보안 규칙 | `users/{uid}/**`는 `request.auth.uid == uid`일 때만 읽기 · 쓰기. 그 밖은 모두 거부 |
| App Check | Play Integrity로 Firestore · Functions 보호 (Blaze라 남용 비용 방어) |

## 4. 비용 방어 (Blaze)

- Cloud Billing 예산 알림: 월 예산(예: ₩5,000)의 50 · 90 · 100%에서 메일.
- 쓰는 유료 가능 서비스는 Firestore · Cloud Functions뿐. 둘 다 무료 한도 안에서 쓰는 설계(사용자당 백업 문서 1개, 로그인할 때만 함수 호출).
- 함수는 최소 인스턴스 0, 리전 `asia-northeast3`(서울), 시간 제한 짧게.
- **결정 필요:** 데이즈처럼 예산 초과 시 결제 자동 해제 함수를 둘지.

## 5. 계정 삭제 (Play 정책)

계정을 만들 수 있는 앱은 **앱 안 계정 삭제**와 **웹에서 삭제를 요청하는 링크**를 함께 제공해야 한다 (Play Console 데이터 보안 · 계정 삭제 항목).

- 앱: 더보기 → 계정 → 계정 삭제 → Firestore `users/{uid}` 전체와 Firebase Auth 사용자 삭제(카카오는 연결 끊기 `unlink`도). 기기 데이터는 그대로.
- 웹: 정책 페이지(`policy-site/`)에 삭제 요청 방법 안내 페이지. **결정 필요:** 요청 받는 방법(문의 메일 등).

## 6. 화면 · 모듈 (구현 때 확정)

| 위치 | 내용 |
|---|---|
| 더보기 → 계정 | 로그인 안 함: "로그인하고 클라우드에 백업" (Google · 카카오 · 익명 버튼). 로그인함: 계정 종류 · 마지막 백업 시각, 지금 백업, 백업에서 불러오기, 로그아웃, 계정 삭제 |
| 온보딩 | 로그인 단계를 넣지 않는다 (설치 즉시 사용). 다시 설치한 사용자를 위해 첫 화면에 작은 "백업에서 불러오기" 링크 → 로그인 후 복원 (**결정 필요**) |
| 모듈 | `core:auth`(Firebase Auth · Credential Manager · Kakao SDK 래퍼), `core:firebase`에 Firestore 추가, `core:data` `AccountRepository` · `CloudBackupRepository`, `feature:account`, 루트 `firebase/`(보안 규칙 · Functions · 에뮬레이터 설정) |

## 7. 외부 콘솔 작업 (사용자)

| 콘솔 | 할 일 |
|---|---|
| Firebase | Blaze로 변경, Authentication에서 Google · 익명 사용 설정, Firestore 만들기(`asia-northeast3`), Functions 배포, App Check(Play Integrity) |
| Google Cloud | 예산 알림, OAuth 동의 화면(앱 이름 · 지원 메일 · 개인정보 처리방침 URL) |
| SHA 인증서 | 업로드 키 · Play 앱 서명 키 · debug 키의 SHA-1 · SHA-256을 Firebase 두 앱(`jpark.bro.subcal` · `.dev`)에 등록 (Google 로그인 · App Check) |
| Kakao Developers | 앱 만들기, 플랫폼 Android에 패키지 2개와 키 해시(업로드 · Play 앱 서명 · debug) 등록, 카카오 로그인 활성화, 동의 항목(회원번호만, 닉네임 · 이메일 받지 않음), 네이티브 앱 키(`local.properties`) · REST API 키(Functions 비밀값) |
| Play Console | 데이터 보안 다시 작성(아래), 계정 삭제 URL, 앱 액세스(로그인 없이 모든 기능 사용 가능) |

데이터 보안에 새로 생기는 항목: 사용자 ID(Firebase uid · 카카오 회원번호), 앱이 만든 콘텐츠(구독 이름 · 금액 · 메모 · 파티 멤버 이름 등 백업 JSON) — 계정 기능용, 전송 중 암호화, 사용자가 삭제 요청 가능.
