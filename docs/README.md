# docs

```
docs/
├─ common/                          # 범용: 데이즈(Days)와 같은 문서를 그대로 복사해서 사용
│  ├─ README.md
│  ├─ ANDROID_ARCHITECTURE.md       # 모듈 · 계층 · 의존 방향 · Navigation 3 · Koin · Ktor
│  ├─ CODING_CONVENTIONS.md         # 이름 · MVI · Compose · 코루틴 · 에러 처리 · Git
│  ├─ TESTING.md                    # 테스트 종류 · 템플릿 · CI
│  └─ GIT_CONVENTIONS.md            # 브랜치 · 커밋 · PR · 리뷰 · 릴리스
│
└─ subcal/                            # 구독캘린더 전용
   ├─ README.md
   ├─ product-spec.md
   ├─ module-map.md
   ├─ data-model-erd.md
   ├─ theme-guide.md
   ├─ ads-strategy.md
   ├─ infra.md
   ├─ design/                       # Stitch 시안 (화면별 폴더)
   └─ assets/
      └─ theme-tokens.json
```

- **common**은 "어떻게 만드는가", **subcal**은 "무엇을 만드는가"를 다룹니다.
- 두 문서가 충돌하면 subcal 문서의 "공통 문서와 다르게 가는 부분"에 기록된 예외만 subcal을 따르고, 나머지는 common을 따릅니다.
- common 문서를 고치면 데이즈 저장소에도 반영할지 판단합니다 (두 프로젝트가 같은 규칙을 공유).
