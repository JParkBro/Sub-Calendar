# Git 컨벤션 (범용)

> 브랜치, 커밋, PR, 리뷰, 머지, 버전 · 릴리스, 저장소 설정까지 Git 사용 전반의 규칙입니다.
> PR마다 실행하는 테스트 구성은 [TESTING.md 6장](TESTING.md#6-ci-구성)을 따릅니다.

## 목차

0. 한눈에 보기
1. 브랜치
2. 커밋
3. PR
4. 리뷰 코멘트
5. 머지
6. 버전 · 태그 · 릴리스
7. 이슈 · 라벨
8. 저장소 설정 (브랜치 보호 · CI)
9. 로컬 Git Hook
10. 비밀 정보 · 커밋하면 안 되는 파일
11. 상황별 대응
12. 체크리스트

---

## 0. 한눈에 보기

| 항목 | 규칙 |
|---|---|
| 브랜치 전략 | GitHub Flow: `main` + 짧게 사는 작업 브랜치 |
| 브랜치 이름 | `{type}/{이슈번호}-{요약}` → `feat/12-home-next-plan` |
| 커밋 · PR 제목 | `[Type] scope: 요약` → `[Feat] home: 다음 일정 카드 추가` |
| 머지 방식 | Squash merge만 허용 (PR 1개 = `main` 커밋 1개) |
| `main` 보호 | PR 필수, CI 통과 필수, 직접 push · force push 금지 |
| 버전 | SemVer `MAJOR.MINOR.PATCH`, 출시 커밋에 태그 `v1.2.0` |
| 릴리스 노트 | PR 라벨 기준 자동 생성 |

---

## 1. 브랜치

### 1-1. 전략: GitHub Flow

```
main ──●──────────●──────────●──────●── (항상 빌드 · 테스트 통과 상태)
        \        /            \    /
         feat/12-...           fix/31-...
```

- **`main`은 항상 배포 가능한 상태**를 유지합니다. 직접 커밋하지 않습니다.
- 모든 작업은 `main`에서 브랜치를 만들어 PR로 합칩니다.
- 작업 브랜치는 **짧게** 유지합니다 (권장 3일 이내). 길어지면 기능을 쪼개거나 기능 플래그(Remote Config 등)로 감춘 채 먼저 병합합니다.
- 별도 `develop` 브랜치는 두지 않습니다. 1인 · 소규모 팀에서는 관리 비용만 늘어납니다.

### 1-2. 브랜치 이름

```
{type}/{이슈번호}-{영문-요약}
```

| type | 용도 | 예 |
|---|---|---|
| `feat` | 기능 추가 | `feat/12-home-next-plan` |
| `fix` | 버그 수정 | `fix/31-widget-date-off-by-one` |
| `refactor` | 구조 개선 | `refactor/40-split-item-repository` |
| `design` | UI · 스타일만 변경 | `design/44-theme-sage-dark` |
| `test` | 테스트 추가 · 수정 | `test/52-migration-v3` |
| `docs` | 문서 | `docs/60-git-conventions` |
| `build` | Gradle · 의존성 · 빌드 설정 | `build/63-update-koin` |
| `ci` | CI 설정 | `ci/70-roborazzi-verify` |
| `chore` | 그 외 잡무 | `chore/75-cleanup-resources` |
| `hotfix` | 출시 버전 긴급 수정 (11-2 참고) | `hotfix/1.2.1-crash-on-launch` |
| `release` | 출시 준비 (필요할 때만) | `release/1.3.0` |

- 소문자, 단어 구분은 하이픈(`-`), 영문만 사용합니다.
- 이슈가 없는 작은 작업은 번호를 생략할 수 있습니다: `chore/update-gitignore`.

---

## 2. 커밋

### 2-1. 형식

```
[Type] scope: 요약

본문 (선택) — 무엇을 왜 바꿨는지

꼬리말 (선택) — Closes #12, BREAKING CHANGE: …
```

예:

```
[Feat] home: 다음 일정 카드 추가

홈 상단에 가장 빠른 미완료 일정을 보여준다.
일정이 없으면 "일정 추가" 버튼을 노출한다.

Closes #12
```

### 2-2. Type

| Type | 의미 | 릴리스 노트 |
|---|---|---|
| `[Feat]` | 사용자에게 보이는 기능 추가 | 새 기능 |
| `[Fix]` | 버그 수정 | 버그 수정 |
| `[Design]` | UI · 스타일만 변경 (동작 변화 없음) | 개선 |
| `[Refactor]` | 동작 변화 없는 코드 구조 변경 | 제외 |
| `[Perf]` | 성능 개선 | 개선 |
| `[Test]` | 테스트 추가 · 수정 | 제외 |
| `[Docs]` | 문서 | 제외 |
| `[Build]` | Gradle, 의존성, 빌드 설정 | 제외 |
| `[CI]` | CI 설정 | 제외 |
| `[Chore]` | 그 외 (리소스 정리, 설정 파일 등) | 제외 |
| `[Revert]` | 이전 커밋 되돌리기 | 상황에 따라 |

- Type 첫 글자는 대문자, 대괄호로 감쌉니다.
- 하나의 커밋에 여러 성격이 섞이면 커밋을 나눕니다. 나눌 수 없으면 가장 중요한 Type을 씁니다.

### 2-3. scope

- **변경한 모듈 · 영역**을 적습니다. 모듈 이름에서 `feature:` · `core:` 접두사를 뺀 이름을 씁니다.

| 변경 위치 | scope |
|---|---|
| `feature:home:*` | `home` |
| `core:data` | `data` |
| `core:designsystem` | `designsystem` |
| `app` | `app` |
| 여러 모듈 전반 | 생략 가능 (`[Build] Koin 4.x로 업데이트`) |
| 문서 | 문서 이름 또는 생략 |

### 2-4. 요약 (제목 줄)

- **[필수]** 50자 안팎, 마침표 없음.
- **[필수]** 한국어로 쓰고 **"~ 추가 / 수정 / 제거 / 변경 / 개선"** 같은 명사형으로 끝냅니다.
  - ✅ `[Fix] widget: 자정 이후 D-day가 갱신되지 않는 문제 수정`
  - ❌ `[Fix] 버그 고침.`, `[Fix] widget: 수정했습니다`
- **[필수]** 무엇이 바뀌는지 드러나게. `수정`, `작업`, `업데이트`만 쓰지 않습니다.

### 2-5. 본문 · 꼬리말

- 본문은 **왜 바꿨는지**를 적습니다. 무엇을 바꿨는지는 diff가 보여줍니다.
- 한 줄 72자 안팎에서 줄바꿈합니다.
- 꼬리말:

| 꼬리말 | 의미 |
|---|---|
| `Closes #12` | PR 병합 시 이슈 자동 종료 |
| `Refs #12` | 관련 이슈 (종료하지 않음) |
| `BREAKING CHANGE: 설명` | 하위 호환이 깨지는 변경 (DB 스키마, 저장 형식, API 계약 등) |

### 2-6. 커밋 단위

- **[필수]** 커밋 하나는 **한 가지 논리적 변경**. 빌드가 깨지는 중간 상태를 가능한 한 남기지 않습니다.
- 작업 브랜치에서는 작게 자주 커밋해도 괜찮습니다. **Squash merge로 `main`에는 PR 제목 하나만 남기 때문**입니다.
- **[금지]** 포맷 변경(ktlint 자동 수정)과 로직 변경을 한 커밋에 섞기. 리뷰가 어려워집니다.

> **Conventional Commits와의 관계:** `feat(home): …` 형식(소문자, 괄호 scope)을 쓰면 semantic-release 같은 자동 버전 도구를 바로 쓸 수 있습니다. 이 문서는 가독성을 위해 `[Type]` 형식을 쓰고, 릴리스 노트 자동화는 **PR 라벨**로 처리합니다(6-4). 자동 버전 도구가 필요해지면 형식만 바꾸면 되도록 Type 이름을 Conventional Commits와 1:1로 맞춰 두었습니다.

---

## 3. PR

### 3-1. 제목

- **커밋과 같은 형식**: `[Type] scope: 요약`
- Squash merge 시 PR 제목이 `main`의 커밋 메시지가 되므로 **[필수]**로 형식을 지킵니다. CI에서 검사합니다(8-3).

### 3-2. 크기

| 변경 줄 수 (테스트 · 생성 파일 제외) | 판단 |
|---|---|
| ~ 400줄 | 적당 |
| 400 ~ 800줄 | 나눌 수 있는지 검토 |
| 800줄 초과 | 원칙적으로 분할 |

- 기능이 크면 순서대로 쪼갭니다: ① 모델 · 도메인 → ② 데이터 계층 → ③ 화면.
- 리팩터링과 기능 추가는 **별도 PR**로.

### 3-3. Draft PR

- 작업 중 공유 · CI 확인이 필요하면 Draft로 엽니다. 리뷰 요청은 Ready for review로 바꿀 때 합니다.

### 3-4. 본문 템플릿

`.github/pull_request_template.md`

```markdown
## 개요
<!-- 무엇을, 왜 바꿨는지 한두 문장 -->

## 관련 이슈
Closes #

## 변경 사항
- 

## 스크린샷 (UI 변경 시 필수)
| 라이트 | 다크 |
|---|---|
|  |  |

## 테스트
- [ ] 단위 테스트 추가 · 수정
- [ ] 스크린샷 기준 이미지 갱신 (의도한 UI 변경인 경우)
- [ ] 직접 확인한 시나리오:

## 체크리스트
- [ ] PR 제목이 `[Type] scope: 요약` 형식이다
- [ ] CODING_CONVENTIONS 리뷰 체크리스트를 확인했다
- [ ] DB 스키마 변경 시 Migration과 테스트가 있다
- [ ] 새 문자열은 리소스로 추가했다
- [ ] 비밀 정보(키, 토큰)가 포함되지 않았다

## 리뷰어에게
<!-- 특히 봐줬으면 하는 부분, 고민한 대안 -->
```

### 3-5. 셀프 리뷰 (1인 개발 포함)

- **[필수]** 리뷰 요청 전, GitHub의 "Files changed" 화면에서 **본인이 diff 전체를 한 번 읽습니다.** 에디터에서 보는 것과 달리 놓친 디버그 코드 · 주석 · 불필요한 변경이 잘 보입니다.
- 의도가 코드만으로 드러나지 않는 곳에는 본인이 먼저 PR 코멘트로 설명을 남깁니다.

---

## 4. 리뷰 코멘트

### 4-1. 접두사

코멘트 맨 앞에 **반영이 필요한 정도**를 붙입니다.

| 접두사 | 의미 | 작성자 대응 |
|---|---|---|
| `[필수]` | 버그, 규칙 위반, 보안 문제. 반영 전 병합 불가 | 반드시 수정 |
| `[제안]` | 더 나은 방법 제안. 작성자가 판단 | 반영하거나 이유를 답변 |
| `[질문]` | 이해를 위한 질문 | 답변 (필요하면 코드 · 주석 보완) |
| `[사소]` | 오타, 이름, 취향 수준 | 반영 자유 |
| `[좋아요]` | 잘한 점 공유 | – |

예:

```
[필수] 저장 실패 시 isSaving이 true로 남습니다. onFailure에서도 false로 돌려야 합니다.
[제안] 이 계산은 core:domain으로 옮기면 위젯에서도 재사용할 수 있을 것 같아요.
[질문] 여기서 Flow 대신 suspend로 한 이유가 있을까요?
```

### 4-2. 리뷰어 규칙

- 사람이 아니라 **코드**에 대해 씁니다. "왜 이렇게 했어요?" 대신 "이 경우 ~가 될 수 있어요".
- 문제만 지적하지 말고 **근거와 대안**을 함께 씁니다.
- 도구(ktlint, detekt)가 잡는 포맷 문제는 코멘트하지 않습니다.
- 리뷰 요청 후 **1영업일 이내** 첫 응답을 목표로 합니다.

### 4-3. 작성자 규칙

- 모든 코멘트에 답하거나 반영합니다. 반영했으면 해당 커밋을 언급하고, 반영하지 않으면 이유를 적습니다.
- **대화 해결(Resolve)은 코멘트를 단 사람이** 합니다. 1인 개발이면 반영 후 본인이 해결합니다.
- 리뷰 반영 커밋은 `[Fix] home: 리뷰 반영 - 저장 실패 시 상태 복구`처럼 별도 커밋으로 올립니다 (squash되므로 이력은 남지 않음).

### 4-4. 승인 기준

| 팀 규모 | 승인 |
|---|---|
| 1인 | 승인 0명. 셀프 리뷰 + CI 통과 |
| 2인 이상 | 1명 이상 승인 + `[필수]` 코멘트 전부 해결 |

---

## 5. 머지

### 5-1. 방식

- **[필수]** **Squash merge만 허용합니다.** 저장소 설정에서 Merge commit · Rebase merge를 끕니다.
- Squash 커밋 메시지는 **PR 제목 + 본문의 개요 · 관련 이슈**로 정리합니다 (GitHub 설정: "Default to pull request title and description").
- **[필수]** 병합 후 작업 브랜치를 삭제합니다 (저장소 설정: Automatically delete head branches).

### 5-2. `main` 최신화

작업 중 `main`이 바뀌면 작업 브랜치를 최신 `main` 위로 올립니다.

```bash
git fetch origin
git rebase origin/main          # 충돌 해결 후
git push --force-with-lease     # 내 작업 브랜치에만
```

- **[필수]** 강제 푸시는 `--force-with-lease`만, **본인 작업 브랜치에만** 사용합니다.
- **[금지]** `main`에 force push, `main`의 이력 수정.

### 5-3. 병합 조건

- CI 필수 검사 전부 통과
- `[필수]` 리뷰 코멘트 전부 해결
- 브랜치가 최신 `main` 기준 (저장소 설정: Require branches to be up to date)

---

## 6. 버전 · 태그 · 릴리스

### 6-1. 버전 규칙 (SemVer)

`MAJOR.MINOR.PATCH`

| 올리는 자리 | 경우 |
|---|---|
| MAJOR | 대규모 개편, 데이터 호환이 깨지는 변경 |
| MINOR | 기능 추가 (`[Feat]` 포함 릴리스) |
| PATCH | 버그 수정만 있는 릴리스, 핫픽스 |

### 6-2. Android 버전 코드

```
versionName = "1.3.2"
versionCode = MAJOR * 10000 + MINOR * 100 + PATCH   // 10302
```

- 버전 값은 한 곳(`gradle.properties` 또는 Version Catalog)에서만 관리합니다.
- 같은 버전 안에서 재빌드가 필요하면 PATCH를 올립니다. 스토어에 올린 versionCode는 재사용할 수 없습니다.

### 6-3. 태그

- **[필수]** 스토어에 올린 빌드의 커밋에 태그를 답니다: `v1.3.2` (주석 있는 태그).

```bash
git tag -a v1.3.2 -m "v1.3.2"
git push origin v1.3.2
```

- 태그 push를 CI 릴리스 워크플로(서명 빌드 → 스토어 업로드)의 트리거로 사용할 수 있습니다.

### 6-4. 릴리스 노트 자동 생성

PR 라벨로 GitHub Release 노트를 자동 분류합니다.

```yaml
# .github/release.yml
changelog:
  exclude:
    labels: [skip-changelog]
  categories:
    - title: 새 기능
      labels: [type:feat]
    - title: 개선
      labels: [type:design, type:perf]
    - title: 버그 수정
      labels: [type:fix]
    - title: 기타
      labels: ["*"]
```

- PR 제목의 Type에 맞춰 라벨을 자동으로 붙이는 워크플로를 두면(8-3) 수동 작업이 없습니다.
- 스토어 출시 노트는 이 자동 노트를 바탕으로 **사용자 언어로 다시 씁니다.**

---

## 7. 이슈 · 라벨

### 7-1. 이슈 템플릿

`.github/ISSUE_TEMPLATE/`에 두 가지를 둡니다.

**bug_report.md**
```markdown
## 현상
## 재현 방법
1.
## 기대 동작
## 환경
- 앱 버전:
- 기기 / OS:
## 스크린샷 · 로그
```

**feature_request.md**
```markdown
## 배경 (왜 필요한가)
## 하고 싶은 것
## 완료 조건
- [ ]
## 참고 (디자인, 문서 링크)
```

### 7-2. 라벨

| 그룹 | 라벨 |
|---|---|
| 종류 | `type:feat`, `type:fix`, `type:design`, `type:refactor`, `type:perf`, `type:test`, `type:docs`, `type:build`, `type:ci`, `type:chore` |
| 우선순위 | `priority:high`, `priority:medium`, `priority:low` |
| 상태 | `status:blocked`, `status:needs-design` |
| 기타 | `skip-changelog`, `breaking-change` |

### 7-3. 연결

- PR 본문 또는 커밋 꼬리말에 `Closes #번호`를 적어 병합 시 이슈를 자동으로 닫습니다.
- 큰 기능은 상위 이슈 하나 아래에 작업 이슈를 체크리스트로 나눕니다.

---

## 8. 저장소 설정 (브랜치 보호 · CI)

### 8-1. `main` 보호 규칙 (GitHub Rulesets)

| 설정 | 값 |
|---|---|
| Require a pull request before merging | ✅ (승인 수: 1인 0명 / 팀 1명 이상) |
| Require status checks to pass | ✅ 아래 8-2의 필수 검사 |
| Require branches to be up to date | ✅ |
| Require conversation resolution | ✅ |
| Require linear history | ✅ (Squash merge와 함께) |
| Block force pushes | ✅ |
| Restrict deletions | ✅ |

### 8-2. PR 필수 검사

| 검사 | 내용 | 참고 |
|---|---|---|
| `pr-title` | PR 제목 형식 검사 | 8-3 |
| `lint` | ktlint, detekt, Android Lint | CODING_CONVENTIONS 1장 |
| `unit-test` | 전체 JVM 테스트, Koin verify | TESTING 6장 |
| `screenshot` | Roborazzi 비교 | TESTING 5-10 |

기기 테스트 · E2E는 PR 필수 검사에 넣지 않고 `main` 병합 후 또는 예약 실행합니다.

### 8-3. PR 제목 검사 · 라벨 자동 부여

```yaml
# .github/workflows/pr-title.yml
name: pr-title
on:
  pull_request:
    types: [opened, edited, synchronize, reopened]

permissions:
  pull-requests: write

jobs:
  pr-title:
    runs-on: ubuntu-latest
    steps:
      - name: Check title format
        env:
          TITLE: ${{ github.event.pull_request.title }}
        run: |
          pattern='^\[(Feat|Fix|Design|Refactor|Perf|Test|Docs|Build|CI|Chore|Revert)\]( [a-z0-9-]+:)? .+'
          if [[ ! "$TITLE" =~ $pattern ]]; then
            echo "PR 제목 형식이 올바르지 않습니다: $TITLE"
            echo "형식: [Type] scope: 요약  (예: [Feat] home: 다음 일정 카드 추가)"
            exit 1
          fi

      - name: Add type label
        env:
          GH_TOKEN: ${{ github.token }}
          TITLE: ${{ github.event.pull_request.title }}
          PR: ${{ github.event.pull_request.number }}
          REPO: ${{ github.repository }}
        run: |
          type=$(echo "$TITLE" | sed -E 's/^\[([A-Za-z]+)\].*/\1/' | tr '[:upper:]' '[:lower:]')
          gh pr edit "$PR" --repo "$REPO" --add-label "type:$type"
```

- 라벨은 저장소에 미리 만들어 둡니다 (7-2).
- 액션 버전과 러너는 적용 시점의 권장 버전을 사용합니다.

---

## 9. 로컬 Git Hook

CI에서 떨어지기 전에 로컬에서 먼저 잡습니다. 훅 파일은 저장소에 커밋하고 경로만 지정합니다.

```bash
# 최초 1회 (README에 안내)
git config core.hooksPath .githooks
```

### 9-1. commit-msg: 커밋 메시지 형식 검사

```bash
#!/usr/bin/env bash
# .githooks/commit-msg
msg=$(head -n1 "$1")
pattern='^\[(Feat|Fix|Design|Refactor|Perf|Test|Docs|Build|CI|Chore|Revert)\]( [a-z0-9-]+:)? .+'
if [[ "$msg" =~ ^(Merge|fixup!|squash!) ]]; then exit 0; fi
if [[ ! "$msg" =~ $pattern ]]; then
  echo "커밋 메시지 형식: [Type] scope: 요약"
  echo "예) [Feat] home: 다음 일정 카드 추가"
  exit 1
fi
```

### 9-2. pre-commit: 포맷 검사 (빠른 것만)

```bash
#!/usr/bin/env bash
# .githooks/pre-commit
./gradlew ktlintCheck --daemon -q || {
  echo "ktlint 실패. ./gradlew ktlintFormat 실행 후 다시 커밋하세요."
  exit 1
}
```

### 9-3. pre-push: 단위 테스트 (선택)

```bash
#!/usr/bin/env bash
# .githooks/pre-push
./gradlew testDevDebugUnitTest --daemon -q
```

- 훅 파일에 실행 권한을 줍니다: `chmod +x .githooks/*`.
- **[필수]** 훅을 건너뛰는 `--no-verify`는 쓰지 않습니다. 훅이 실패하면 원인을 고친 뒤 다시 커밋 · 푸시합니다.

---

## 10. 비밀 정보 · 커밋하면 안 되는 파일

### 10-1. 절대 커밋 금지

| 파일 · 값 | 대신 |
|---|---|
| 서명 키스토어 (`*.jks`, `*.keystore`) | CI 비밀 변수(Base64) + 로컬 보관 |
| 키스토어 비밀번호 | `keystore.properties`, CI 비밀 변수 |
| 서비스 계정 키(JSON), API 비밀 키 | `local.properties`, CI 비밀 변수, Worker Secret |
| `.env`, `local.properties` | 예시 파일 `local.properties.example`만 커밋 |
| 개인 IDE 설정 (`.idea/workspace.xml` 등) | `.gitignore` |

> `google-services.json`은 비밀 키가 아니지만(보안은 보안 규칙 · App Check가 담당), 공개 저장소라면 커밋하지 않고 CI에서 주입하는 것을 권장합니다.

### 10-2. 반드시 커밋

| 파일 | 이유 |
|---|---|
| Room 스키마 JSON (`schemas/`) | 마이그레이션 테스트 기준 |
| 스크린샷 기준 이미지 | 스크린샷 비교 기준 (용량이 커지면 Git LFS) |
| `gradle/wrapper/` | 같은 Gradle 버전으로 빌드 |
| `.editorconfig`, detekt 설정, `.githooks/` | 규칙을 모두가 공유 |

### 10-3. 비밀 정보를 커밋했을 때

1. **즉시 해당 키를 폐기 · 재발급**합니다. 커밋을 지워도 이미 노출된 것으로 간주합니다.
2. 그다음 이력에서 제거합니다 (`git filter-repo` 등). 공개 저장소라면 GitHub 지원에 캐시 삭제를 요청합니다.
3. GitHub의 Secret scanning · Push protection을 켜 둡니다.

### 10-4. .gitignore 기본

```gitignore
# Build
build/
.gradle/
.kotlin/
*.apk
*.aab

# Local
local.properties
keystore.properties
.env
*.jks
*.keystore
service-account*.json

# IDE
.idea/*
!.idea/codeStyles/
*.iml

# OS
.DS_Store
Thumbs.db

# Worker (Node)
node_modules/
.wrangler/
.dev.vars
```

---

## 11. 상황별 대응

### 11-1. 일반 기능 개발

```bash
git switch main && git pull
git switch -c feat/12-home-next-plan
# 작업 · 커밋
git push -u origin feat/12-home-next-plan
# PR 생성 (제목: [Feat] home: 다음 일정 카드 추가)
# CI 통과 · 리뷰 반영 → Squash merge → 브랜치 자동 삭제
```

### 11-2. 출시 버전 긴급 수정 (핫픽스)

`main`에 아직 출시하지 않은 기능이 들어가 있을 때:

```bash
git switch -c hotfix/1.2.1-crash-on-launch v1.2.0   # 출시 태그에서 분기
# 수정 · 커밋 ([Fix] app: 시작 시 크래시 수정)
# versionName 1.2.1로 올리고 빌드 · 출시
git tag -a v1.2.1 -m "v1.2.1" && git push origin v1.2.1
# 같은 수정을 main에도 반영: hotfix 브랜치로 main 대상 PR 생성 (또는 cherry-pick)
```

`main`에 미출시 변경이 없으면 일반 `fix/` 브랜치로 처리하고 `main`에서 바로 출시합니다.

### 11-3. 잘못 병합한 PR 되돌리기

- GitHub의 **Revert** 버튼으로 되돌리기 PR을 만듭니다. 제목은 `[Revert] {원래 PR 제목}`.
- **[금지]** `main`에서 `git reset` 후 force push.

### 11-4. 큰 기능을 나눠서 병합하기

- 미완성 기능은 Remote Config 등의 **기능 플래그**로 숨긴 채 작은 PR로 계속 병합합니다.
- 오래 사는 기능 브랜치(몇 주 단위)를 만들지 않습니다. 충돌과 리뷰 부담이 커집니다.

---

## 12. 체크리스트

**저장소 처음 만들 때**
- [ ] `main` 보호 규칙 설정 (8-1)
- [ ] Squash merge만 허용, 병합 후 브랜치 자동 삭제
- [ ] 라벨 생성 (7-2), `.github/release.yml`
- [ ] PR · 이슈 템플릿
- [ ] PR 제목 검사 · CI 워크플로
- [ ] `.githooks/` 커밋, README에 `git config core.hooksPath .githooks` 안내
- [ ] `.gitignore`, Secret scanning · Push protection 활성화

**PR 올릴 때**
- [ ] 브랜치 이름 `{type}/{이슈번호}-{요약}`
- [ ] PR 제목 `[Type] scope: 요약`
- [ ] 템플릿 채움 (UI 변경 시 라이트 · 다크 스크린샷)
- [ ] 셀프 리뷰 완료
- [ ] 400줄을 넘으면 분할 검토

**출시할 때**
- [ ] versionName · versionCode 올림
- [ ] 출시 커밋에 `v{버전}` 태그
- [ ] GitHub Release 노트 생성 → 스토어 노트 작성
