# RECOVERY-SERVER

## 프로젝트 개요
- 백엔드 서버 (Spring Boot, 모듈러 모놀리스 아키텍처)
- 언어: Java 25
- 빌드 도구: Gradle
- 아키텍처 스타일: 모듈 = Bounded Context, 모듈 내부 = Vertical Slice(기능 단위)

## 아키텍처 원칙 (반드시 지킬 것)

### 1. 모듈은 서로 직접 참조하지 않는다
- 모듈 간 참조는 오직 두 경로로만 허용:
    - **동기 필요 시** → 상대 모듈의 `api/` 패키지에 있는 인터페이스만 호출 (구현체·internal 패키지는 절대 참조 금지)
    - **비동기 가능 시** → `shared/event/`의 이벤트를 발행하고 다른 모듈이 구독
- `internal/` 패키지에 있는 클래스는 그 모듈 밖에서 import하면 안 됨
- 새 기능 추가할 때 다른 모듈의 `internal`이나 `domain`을 직접 import하려는 코드가 보이면 즉시 지적하고, `api` 인터페이스나 이벤트로 우회할 것

### 2. 모듈 내부는 레이어가 아니라 슬라이스(기능) 단위로 구성한다
- 전통적인 Controller/Service/Repository 계층 분리 금지
- 기능 하나 = 폴더 하나 (예: `createorder/`, `getorder/`)
- 슬라이스 폴더 안에는 그 기능에 필요한 것만: `Command/Query`(입력), `Handler`(컨트롤러+서비스+레포 로직 전체), `Response/View`(출력)
- 기능을 수정할 때 여러 패키지를 오가지 않고 슬라이스 폴더 하나만 보면 되도록 유지

### 3. 경계는 코드 리뷰가 아니라 도구로 강제한다
- ArchUnit으로 다음 규칙을 테스트에 포함:
    - 모듈 패키지가 다른 모듈의 `internal` 또는 `domain`을 참조하면 빌드 실패
    - `api` 패키지 외부로 구현 클래스가 노출되지 않는지 검증
- 새 모듈이나 슬라이스를 추가할 때 ArchUnit 규칙도 같이 업데이트할 것

## 폴더 구조
```
src/main/java/com/example/app
├── order/
│   ├── api/                 ← 외부에 노출하는 인터페이스만
│   ├── domain/               ← 애그리거트 루트, 값 객체
│   ├── createorder/          ← 슬라이스: Command/Handler/Response
│   ├── getorder/              ← 슬라이스: Query/Handler/View
│   └── internal/              ← 모듈 내부 전용, 외부 참조 금지
├── product/                   (구조 동일)
├── member/                    (구조 동일)
└── shared/                    ← 공유 커널
    ├── event/                 ← 모듈 간 비동기 통신
    └── vo/                    ← 여러 모듈 공통 값 객체
```

## 새 기능 추가 시 작업 순서
1. 어느 모듈(bounded context)에 속하는지 먼저 판단
2. 기존 슬라이스에 끼워 넣을지, 새 슬라이스 폴더를 만들지 결정 (다른 기능과 로직을 공유하려는 유혹이 들면 대부분 새 슬라이스가 맞음)
3. 다른 모듈 데이터가 필요하면 해당 모듈의 `api` 인터페이스 확인 → 없으면 그 모듈에 인터페이스를 먼저 추가
4. Command/Query → Handler → Response 순으로 작성
5. `./gradlew test` (ArchUnit 규칙 포함) 통과 확인 후 커밋

## 빌드 / 실행 명령
```bash
./gradlew build            # 빌드
./gradlew test             # 테스트 (ArchUnit 아키텍처 규칙 포함)
./gradlew bootRun          # 로컬 서버 실행
./gradlew test --tests "com.example.app.order.createorder.*"   # 특정 슬라이스만 테스트
```

## 커밋 / PR 규칙
- 커밋 메시지: `[타입] 설명` (예: `[feat] 주문 생성 슬라이스 추가`)
- 타입: feat, fix, refactor, domain, arch(아키텍처 규칙 변경), docs, test, chore
- 브랜치: `main` 직접 작업 금지, `feature/기능명` 브랜치 사용
- 모듈 경계를 넘는 변경(공유 커널 수정, api 인터페이스 변경)은 PR 설명에 영향받는 모듈 명시

## Git 훅 설정 (최초 1회, 클론 직후)
```bash
./scripts/setup-git-hooks.sh    # core.hooksPath를 .githooks 로 지정
```
- `.githooks/commit-msg`: 커밋 메시지가 `[타입] 설명` 형식이 아니면 커밋 자체를 거부
- `.githooks/pre-push`: `main`/`master`로의 직접 push, 강제(non-fast-forward) push를 거부
- 새 기능 브랜치는 `./scripts/new-feature-branch.sh <기능명>` 으로 생성 (`main`에서 최신화 후 `feature/<기능명>` 생성)
- 훅 스크립트를 수정하면 팀원 전체가 다시 pull 받아야 적용됨 (강제 배포 수단은 아님, 최초 설정을 각자 1회 실행해야 함)

## 절대 하지 말 것
- 다른 모듈의 `internal/`, `domain/` 패키지 직접 import
- 레이어드 구조로 되돌리기 (Controller/Service/Repository 패키지 분리)
- `.env`, `application-secret.yml` 등 민감 정보 파일 읽기/커밋
- `main` 브랜치 강제 push
- ArchUnit 규칙 비활성화하거나 우회

## 참고 사항
- 이 파일은 매 세션 시작 시 자동으로 로드됩니다.
- 모듈이 추가되거나 아키텍처 규칙이 바뀌면 이 파일도 같이 업데이트하세요.