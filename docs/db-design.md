# Recovery30 데이터베이스 설계서 (MySQL)

> 기준: Figma `UI` 섹션 25개 화면 + 기획 회의록
> DBMS: **MySQL 8.0** (InnoDB, `utf8mb4`)
> 마이그레이션: Flyway (`src/main/resources/db/migration`), `spring.jpa.hibernate.ddl-auto=validate`
> 버전: v2.0 — 피그마 전수 대조 반영 (`V9`~`V13` 추가)

---

## 0. 설계 원칙 (기획 안전장치의 DB 반영)

| 기획 원칙 | DB 반영 |
|---|---|
| 명시적 동의 있을 때만 분석 | `core_consents` + 분석 실행(`forecast_runs`)에 `consent_id` **NOT NULL** FK |
| 예측점수를 대출한도·금리·추심에 사용 금지 | 예측 결과를 `forecast_*` 접두사로 논리 격리. 신용평가계(CSS/CB)는 향후 **별도 DB + 별도 계정**으로 두고 그 계정 GRANT에서 `forecast_*` 제외 |
| 자격 판정은 규칙기반, 최종 판단은 사람 | `recovery_program_eligibility_checks.is_advisory = TRUE` CHECK 고정, `recovery_consultations`에 `CHECK (final_decision IS NULL OR decided_by IS NOT NULL)` |
| 근거를 고객에게 공개 | `forecast_daily_items`, `forecast_risk_driver_evidence`로 모든 수치의 원천 거래(`ref_type`/`ref_id`) 추적 |
| 오탐 고객 불이익 없음 | 예측 상태는 `forecast_runs` 이력만 남기고 `core_businesses`에 등급 컬럼 없음 |
| Packet 수정 시 새 버전 | `recovery_packets.version` + `supersedes_packet_id` 체인, 덮어쓰기 금지 |

### PostgreSQL 설계서 → MySQL 변환 규칙

| PostgreSQL | MySQL 8.0 |
|---|---|
| `CREATE SCHEMA` + `schema.table` | 단일 DB `recovery30` + 접두사 `core_ / source_ / forecast_ / recovery_ / audit_` |
| `BIGSERIAL PRIMARY KEY` | `BIGINT NOT NULL AUTO_INCREMENT` + `PRIMARY KEY (id)` |
| `TIMESTAMPTZ` | `DATETIME(6)` — UTC 저장, 앱은 `java.time.Instant` |
| `now()` | `CURRENT_TIMESTAMP(6)` |
| `NUMERIC(p,s)` | `DECIMAL(p,s)` |
| `JSONB` | `JSON` (엔티티는 `@JdbcTypeCode(SqlTypes.JSON) String`) |
| `CREATE INDEX ... WHERE ...` (부분 인덱스) | **미지원** → 복합 인덱스 + 쿼리 필터 (예: `idx_source_adjustments_active (business_id, status, expected_date)`) |
| `CHECK (...)` | 그대로 (8.0.16+ 강제) |
| 인덱스 `col DATE DESC` | 그대로 (8.0 내림차순 인덱스) |
| 암호화 컬럼 | 앱 레벨 (JPA `AttributeConverter` + AES-GCM). `AES_ENCRYPT()` 사용 금지 |
| enum | `VARCHAR + CHECK IN(...)` (네이티브 `ENUM` 회피) |

---

## 1. 모듈(bounded context) ↔ 테이블 접두사

| 모듈 패키지 | 테이블 접두사 | 비고 |
|---|---|---|
| `member` | `member` | 기존 회원 모듈 (인증 예시). `core_users`와 이원화 상태 — 6.8 참고 |
| `business` | `core_` | users / businesses / consents / consent_types |
| `source` | `source_` | 원천 데이터 + 사용자 보정값 |
| `forecast` | `forecast_` | 예측 결과 (격리 대상) |
| `recoveryoption` | `recovery_` | 회복안 / 시나리오 / 셀프 액션 |
| `supportprogram` | `recovery_` | 지원제도 / 자격판정 / 추천 |
| `packet` | `recovery_` | Recovery Packet + 전송 |
| `consultation` | `recovery_` | 상담 예약 / 상담자 / 슬롯 |
| `followup` | `recovery_` | **신규** — 사후점검 / 실행 상태 |
| `audit` | `audit_` | **신규** — 감사 / 거버넌스 / 재현성 / 공휴일 / 알림 |

> `recovery_` 접두사는 여러 모듈이 공유하지만, 각 테이블은 소유 모듈의 `domain/` 패키지에 엔티티가 존재하며 ArchUnit이 교차 참조를 막는다.

---

## 2. 마이그레이션 이력

| 파일 | 내용 |
|---|---|
| `V1__create_member_table.sql` | `member` |
| `V2__create_core_tables.sql` | `core_users`, `core_businesses`, `core_consent_types`, `core_consents` |
| `V3__create_source_tables.sql` | `source_bank_accounts`, `source_transactions`, `source_card_settlements`, `source_loans`, `source_loan_schedules`, `source_recurring_expenses` |
| `V4__create_forecast_tables.sql` | `forecast_runs`, `forecast_daily`, `forecast_risk_drivers` |
| `V5__create_recovery_option_tables.sql` | `recovery_options`, `recovery_scenarios`, `recovery_scenario_options`, `recovery_user_option_selections` |
| `V6__create_support_program_tables.sql` | `recovery_support_programs`, `recovery_program_eligibility_rules`, `recovery_program_eligibility_checks` |
| `V7__create_packet_tables.sql` | `recovery_packets` |
| `V8__create_consultation_tables.sql` | `recovery_counselors`, `recovery_consultations` |
| **`V9__forecast_home_and_coverage_tables.sql`** | `source_data_sources`, `forecast_coverage`, `forecast_daily_items`, `forecast_run_narratives` + `forecast_risk_drivers` 컬럼 3개 추가 |
| **`V10__adjustment_and_evidence_tables.sql`** | `source_adjustments`, `source_adjustment_suggestions`, `forecast_risk_driver_evidence` |
| **`V11__recovery_extension_tables.sql`** | `recovery_self_action_plans/items`, `recovery_program_documents`, `recovery_program_recommendations`, `recovery_consultation_options`, `recovery_counselor_slots` |
| **`V12__packet_transfer_and_followup_tables.sql`** | `recovery_packet_transfers`, `recovery_followup_schedules/results`, `recovery_execution_status` |
| **`V13__audit_and_governance_tables.sql`** | `audit_logs`, `audit_consent_logs`, `audit_ai_generations`, `audit_model_versions`, `audit_ruleset_versions`, `audit_holidays`, `audit_notifications` |
| `V14__seed_reference_data.sql` | 레퍼런스 시드: 버전 마스터, `core_consent_types` 3건, 상담사 2명, 지원제도 3건 + 규칙/서류, 2025 공휴일 |
| **`V15__slot_capacity_and_eligibility_check_items.sql`** | `recovery_counselor_slots` 정원 컬럼(`capacity`, `booked_count`) 추가 + `recovery_program_eligibility_check_items` 신규 (피그마 갭 ①②) |

---

## 3. 화면 ↔ 테이블 대조 결과

`✅` = 구현됨 · `🆕` = V9~V13에서 추가 · `➕col` = 기존 테이블 컬럼 추가

| 화면 | 사용 테이블 |
|---|---|
| 로그인 · 스플래시 | `core_users` ✅ |
| 분리 동의 · 동의 관리 | `core_consent_types` ✅, `core_consents` ✅, `audit_consent_logs` 🆕 |
| **사업자 홈** | `forecast_runs` ✅, `forecast_risk_drivers` ✅ ➕col, `forecast_coverage` 🆕, `forecast_run_narratives` 🆕 (상태 라벨) |
| **Dashboard (30일 분석)** | `forecast_runs` ✅, `forecast_daily` ✅, `forecast_daily_items` 🆕, `forecast_risk_drivers` ✅ ➕col, `forecast_coverage` 🆕, `source_data_sources` 🆕, `audit_holidays` 🆕 |
| 원인 상세 | `forecast_runs` ✅, `forecast_risk_drivers` ✅ ➕col, `forecast_risk_driver_evidence` 🆕 |
| 판단보류 안내 | `forecast_runs`(status=`HOLD`) ✅, `forecast_coverage` 🆕 |
| 정보 보정 | `source_adjustments` 🆕, `source_adjustment_suggestions` 🆕, `forecast_runs`(`coverage_overall`) ✅ |
| 안정 상태 안내 | `forecast_runs`(status=`STABLE`) ✅, `forecast_daily(_items)` 🆕, `forecast_run_narratives` 🆕, `audit_holidays` 🆕 |
| 현금매출 / 타행·외부자금 / 예정수입 / 예정지출 입력 (4) | `source_adjustments` 🆕 (`adjustment_type`으로 구분) |
| 데이터 범위 확인 | `source_data_sources` 🆕 |
| 회복안 비교 (3종) | `forecast_runs` ✅, `forecast_risk_drivers` ✅, `recovery_options` ✅, `recovery_scenarios` ✅, `recovery_scenario_options` ✅, `recovery_user_option_selections` ✅ |
| 셀프 액션 저장 | `recovery_self_action_plans` 🆕, `recovery_self_action_items` 🆕, `recovery_scenarios` ✅ |
| 지원사업 목록 | `recovery_support_programs` ✅, `recovery_program_recommendations` 🆕 |
| 지원사업 상세 | `recovery_support_programs` ✅, `recovery_program_eligibility_rules` ✅, `recovery_program_eligibility_checks` ✅, `recovery_program_eligibility_check_items` 🆕(V15), `recovery_program_documents` 🆕 |
| Recovery Packet | `recovery_packets` ✅, `recovery_packet_transfers` 🆕, `recovery_followup_schedules` 🆕 |
| 상담 예약 | `recovery_consultations` ✅, `recovery_consultation_options` 🆕, `recovery_counselors` ✅, `recovery_counselor_slots` 🆕 ➕col(V15 `capacity`/`booked_count`) |
| 사후점검 | `recovery_followup_schedules` 🆕, `recovery_followup_results` 🆕, `recovery_execution_status` 🆕, `forecast_runs` ✅ |
| 상담자 대시보드 (뷰) | `recovery_consultations` + `forecast_runs` + `recovery_packets` — `counselor_queue` VIEW (미구현, 6.6) |

### 3.A `forecast_risk_drivers` 컬럼 추가 (V9)

피그마 부족 원인 카드가 단일 `occurrence_date`/`contribution_amount`로 표현 안 되는 케이스:

| 컬럼 | 이유 (화면 예시) |
|---|---|
| `occurrence_text VARCHAR(100)` | 복수 발생일 — "11월 20일·25일 발생". 정렬은 `occurrence_date` 유지 |
| `impact_period_text VARCHAR(100)` | 영향 기간 — "6월 15일~28일 영향" |
| `metric_text VARCHAR(50)` | 금액 아닌 지표 — "-18%", "약 32% 감소" |

### 3.B 신규 테이블 요지

- **`source_data_sources`** — 연동 커넥션의 **현재(live)** 상태. `source_type` ∈ {`BANK_ACCOUNT`,`CARD_SETTLEMENT`,`LOAN`,`AUTO_TRANSFER`}, `institution_name`, `coverage_rate`, `period_months`, `last_synced_at`, `sync_status` ∈ {`SYNCED`,`PARTIAL`,`FAILED`}. `UNIQUE(business_id, source_type)`.
- **`forecast_coverage`** — 예측 1회 **시점의 스냅샷**. `is_below_threshold`가 하나라도 TRUE면 `forecast_runs.status = HOLD` 근거. `source_data_sources`와 중복처럼 보이지만 **목적이 다름**(live vs run-snapshot) — 둘 다 유지.
- **`forecast_daily_items`** — 하루치 근거 라인. `item_kind` ∈ {`CONFIRMED`,`EXPECTED`,`ADJUSTMENT`}, `amount_min`/`amount_max`(범위), `ref_type`/`ref_id`(원천 역추적). `CHECK (amount_min <= amount_max)`.
- **`forecast_run_narratives`** — RISK 전용인 `forecast_risk_drivers`가 담을 수 없는 서술문. `kind` ∈ {`STATUS_LABEL`,`STABLE_REASON`,`RISK_NOTE`,`STATE_CHANGE_HINT`,`DISCLAIMER`}.
- **`source_adjustments`** — 4개 입력 화면 통합. `adjustment_type` ∈ {`CASH_SALES`,`EXTERNAL_FUND`,`EXPECTED_INCOME`,`EXPECTED_EXPENSE`}, `certainty` ∈ {`CONFIRMED`,`ESTIMATED`}, `status` ∈ {`DRAFT`,`SAVED`,`DISCARDED`}, `applied_run_id`.
  - **시나리오 반영 규칙**: `CONFIRMED` → 예상·낙관 반영 / `ESTIMATED` → 낙관만 반영, 보수적 제외.
- **`source_adjustment_suggestions`** — 반복 패턴 추정 후보. `status` ∈ {`PROPOSED`,`ACCEPTED`,`REJECTED`}, `accepted_adjustment_id`.
- **`forecast_risk_driver_evidence`** — 원인별 근거 거래. `label`, `period_text`, `ref_type`/`ref_id`. `ON DELETE CASCADE`.
- **`recovery_self_action_plans` / `_items`** — 자체 실행 계획 + 준비 항목(`target_date` 입력, `status` ∈ {`PENDING`,`DONE`}).
- **`recovery_program_documents`** — 지원제도 필요서류.
- **`recovery_program_recommendations`** — 예측 실행별 추천, `rank_no`, `match_reason`. `UNIQUE(forecast_run_id, program_id)`.
- **`recovery_consultation_options`** — 상담 ↔ 회복안 N:M.
- **`recovery_counselor_slots`** — 예약 가능 슬롯. `status` ∈ {`OPEN`,`BOOKED`,`BLOCKED`}, `CHECK (start_at < end_at)`. **V15**: `capacity`/`booked_count` 추가 — 화면의 "잔여 N석" = `capacity - booked_count`, 예약 가능 = `status <> 'BLOCKED' AND booked_count < capacity`. `CHECK (capacity >= 1)`, `CHECK (0 <= booked_count <= capacity)`.
- **`recovery_program_eligibility_check_items`** (V15) — 자격 판정의 **규칙 단위** 결과. `recovery_program_eligibility_checks`는 (business, program) 1행 롤업이라 지원사업 상세 화면의 "규칙별 체크박스 + 개별 문구"를 못 그림. `check_id`+`rule_id`(`UNIQUE`), `result` ∈ {`LIKELY_PASS`,`NEEDS_REVIEW`,`LIKELY_FAIL`,`UNKNOWN`}, `note_text`, `is_advisory = TRUE` CHECK 고정. `ON DELETE CASCADE`(부모 check / rule 양쪽).
- **`recovery_packet_transfers`** — Packet 전송 이력. `scope_json`(전송 범위 4항목), `consent_id` **NOT NULL**.
- **`recovery_followup_schedules`** — `checkpoint` ∈ {`D30`,`D60`,`D90`}, `status` ∈ {`SCHEDULED`,`DONE`,`SKIPPED`}, `consent_id` **NOT NULL**.
- **`recovery_followup_results`** — schedule당 1건(`UNIQUE`). `balance_recovered` ∈ {`YES`,`PARTIAL`,`NO`}, `has_delinquency`, `latest_forecast_run_id`.
- **`recovery_execution_status`** — 회복안별 `status` ∈ {`NOT_STARTED`,`IN_PROGRESS`,`DONE`,`BLOCKED`}, `blocker_text`.
- **`audit_*`** — 3.C 참고.

### 3.C 거버넌스 (`audit_*`, V13)

| 테이블 | 용도 |
|---|---|
| `audit_logs` | `actor_type` ∈ {`USER`,`COUNSELOR`,`SYSTEM`,`AI`}, action/target/purpose/consent_id/ip |
| `audit_consent_logs` | 동의/철회 append-only. `core_consents`는 최신 1건만 |
| `audit_ai_generations` | AI 호출 추적: feature, model, `retrieved_docs`(RAG 근거 JSON), `human_reviewed` |
| `audit_model_versions` | 예측 모델 버전 마스터 (PK = `version`) |
| `audit_ruleset_versions` | 규칙셋 버전 마스터. `domain` ∈ {`ELIGIBILITY`,`FORECAST`} |
| `audit_holidays` | 공휴일 (PK = `holiday_date`). 납부일 이동 계산. **실제 데이터 시드** (목 아님) |
| `audit_notifications` | 알림 발송·읽음·행동완료. KPI "회복 행동 완료율" |

### 3.D 피그마 자체 오류 (DB 무관)

- **정보 보정 화면**: 4개 보정 항목마다 "보증 한도 최대 1억 원 / 보증료 연 0.9%" 문구가 붙어 있으나 이는 보증부 대출/지원제도 문구로 보정 항목과 무관 — Figma 복붙 실수. DB 반영 안 함.

---

## 4. 상태 전이 규칙

### `forecast_runs.status`
```
coverage_overall < 70%  또는  forecast_coverage.is_below_threshold 존재  →  HOLD (판단보류)
그 외 + first_shortfall_date 존재                                        →  RISK
그 외 + 30일 내 부족 없음 + is_buffer_met                                →  STABLE
```
`CHECK (status = 'HOLD' OR min_balance_expected IS NOT NULL)` — 판단보류가 아니면 최저잔액 밴드 필수.
`CHECK (min_balance_conservative <= min_balance_expected <= min_balance_optimistic)`.

### 보정값 → 재계산
```
source_adjustments.status = DRAFT      (입력만, 예측 미반영)
      ↓ 저장
source_adjustments.status = SAVED      → forecast_runs 신규 생성 (triggered_by = ADJUSTMENT_SAVED)
      ↓
source_adjustments.applied_run_id 기록
```

### Packet 버전
```
v1 생성(DRAFT) → 고객 확인(CONFIRMED) → 전송(SENT)
회복안 변경 시 → v1 유지 + v2 신규 (supersedes_packet_id = v1.id)
```

### 상담 최종 판단
```
final_decision 은 decided_by(counselor_id) 가 있을 때만 기록 가능  ← CHECK 제약
transfer_consent_granted = FALSE 여도 예약은 성립
```

---

## 5. 리소스별 API 매핑 (엔드포인트 = 리소스 1개, 화면이 조합)

> 화면 단위가 아니라 리소스 단위로 엔드포인트를 나눈다. 클라이언트가 여러 리소스를 조합해 화면을 구성한다.

### forecast
| 엔드포인트 | 리소스 | 재사용 화면 |
|---|---|---|
| `GET /api/businesses/{bizId}/forecasts/latest` | 최신 예측 run | 홈, Dashboard, 원인상세, 판단보류, 안정상태, 회복안비교, 사후점검, Packet |
| `GET /api/forecasts/{runId}` | 특정 run | Packet 스냅샷 |
| `GET /api/forecasts/{runId}/daily` | 30일 캘린더 행 | Dashboard 차트/일자별, 안정상태 차트 |
| `GET /api/forecasts/{runId}/daily/{date}` | 하루 + `forecast_daily_items[]` | Dashboard 일자상세, 안정상태 바텀시트 |
| `GET /api/forecasts/{runId}/risk-drivers?limit=&include=evidence` | 부족 원인 (+근거) | 홈(limit 3), Dashboard, 원인상세, 회복안비교, Packet |
| `GET /api/forecasts/{runId}/coverage` | source_type별 커버리지 스냅샷 | 홈, Dashboard, 판단보류 |
| `POST /api/businesses/{bizId}/forecasts:recalculate` | 재계산 트리거(동의 확인 → 신규 run) | 보정 저장 후, 수동 재계산 |

### source
| `GET /api/businesses/{bizId}/data-sources` | 연동 커넥션 현재 상태 | 데이터 범위 확인, 홈, Dashboard |
| `GET/POST/PATCH/DELETE /api/businesses/{bizId}/adjustments?type=&status=` | 보정값 CRUD (4화면 공용) | 입력 4화면, 정보 보정 |
| `GET /api/businesses/{bizId}/adjustment-suggestions` · `POST /{id}:accept` | 반복 패턴 추정 후보 | 정보 보정 |

### consent (business 모듈)
| `GET /api/consent-types` | 동의 마스터(목적·범위·철회영향) | 분리 동의, 동의 관리 |
| `GET /api/businesses/{bizId}/consents` · `PUT /{typeCode}` | 현재 상태 + grant/withdraw(ip·ua·로그) | 분리 동의, 동의 관리 |

### recoveryoption
| `GET /api/forecasts/{runId}/recovery-options` | 회복안 카드 | 회복안 비교 |
| `GET /api/forecasts/{runId}/scenarios` | baseline + 옵션조합 시뮬 | 회복안 비교 시나리오표 |
| `PUT /api/forecasts/{runId}/option-selections` | 최대 2개 선택 (앱 레벨 제한) | 회복안 비교 |
| `POST /api/forecasts/{runId}/self-action-plans` · `PATCH …/items/{id}` | 셀프 실행 계획·준비항목 | 셀프 액션 저장, 사후점검 |

### supportprogram
| `GET /api/support-programs?applicableOnly=&sort=deadline` | 목록 | 지원사업 목록 |
| `GET /api/support-programs/{code}` · `/documents` | 상세·필요서류 | 지원사업 상세 |
| `GET /api/businesses/{bizId}/support-programs/{code}/eligibility` | 자격 판정 (advisory) | 지원사업 상세 |
| `GET /api/forecasts/{runId}/program-recommendations` | 추천 + match_reason | 지원사업 목록, 회복안 비교 |

### packet
| `GET /api/businesses/{bizId}/packets/latest` · `GET /api/packets/{id}` | Packet (snapshot 펼침 + 버전체인) | Recovery Packet |
| `POST /api/forecasts/{runId}/packets` | 신규 버전 생성 (덮어쓰기 X) | 회복안/셀프액션 저장 후 |
| `GET/POST /api/packets/{id}/transfers` | 전송 이력 (scope·consent 확인) | Packet 전송상태, 상담 예약 |

### consultation
| `GET /api/counselors/{id}/slots?from=&to=` | 예약 가능 슬롯 | 상담 예약 |
| `POST /api/businesses/{bizId}/consultations` · `GET /api/consultations/{id}` | 예약 생성·확인 | 상담 예약 |
| `GET /api/counselors/queue` | 우선순위 뷰 (부족일 ASC…) | 상담자 대시보드 |

### followup
| `GET /api/businesses/{bizId}/followups` · `GET /api/followups/{id}/result` | D30/60/90 현황·결과 | 사후점검, Packet 일정 |
| `GET /api/businesses/{bizId}/recovery-execution-status` | 회복안별 실행 상태 | 사후점검 |

### 클라 조합 예시
- 홈 = `forecasts/latest` → (runId로) `risk-drivers?limit=3` + `coverage`
- Dashboard = `forecasts/latest` + `daily` + `risk-drivers` + `data-sources`
- 원인 상세 = `forecasts/{runId}` + `risk-drivers?include=evidence`
- 회복안 비교 = `forecasts/latest` + `risk-drivers` + `recovery-options` + `scenarios` + `option-selections`

**정합성**: `risk-drivers`/`coverage`는 `latest`가 준 `forecastRunId`로 명시 호출하고, 모든 응답에 `forecastRunId`+`baseDate`를 에코해 클라가 불일치를 감지한다.

---

## 6. Mock data 사용 영역

실제 연동/엔진/크롤링이 없는 MVP 구간은 시드 목데이터로 채우고, API는 그 위에서 정상 동작한다.

| 영역 | 처리 |
|---|---|
| 원천 데이터 `source_bank_accounts` `source_transactions` `source_card_settlements` `source_loans` `source_loan_schedules` `source_recurring_expenses` | **시드**. 회의록 규모: 사업자 100명 × 12개월, 거래 ~14만 행, `forecast_daily` 100 × 30 = 3,000행 |
| 연동 상태 `source_data_sources` / `forecast_coverage` | **목값 고정**. "마지막 갱신 09:14" 등. 판단보류 데모용으로 자동이체 61% 등 일부 낮게 |
| 예측 엔진 출력 `forecast_runs` `forecast_daily` `forecast_daily_items` `forecast_risk_drivers` `forecast_risk_driver_evidence` `forecast_run_narratives` | ML 전 → **규칙기반 or 사전계산 목**. `shap_value`·`description`·`assumption_text`·서술문은 템플릿 목 |
| 반복패턴 추정 `source_adjustment_suggestions` | **목 후보** ("매월 15일 현금매출 120만" 고정) |
| 정보 보정 "재계산 예상 영향" (D+12→D+18) | **목 계산값** or 고정 (별도 저장소 없음, 조회 시 계산) |
| 회복안·시나리오 `recovery_options` `recovery_scenarios` `recovery_scenario_options` | **목 마스터 + 목 시뮬 결과** |
| 지원사업 `recovery_support_programs` `recovery_program_eligibility_rules/checks` `recovery_program_documents` | **목 카탈로그 3건**, 규칙버전 `v2025-06` 고정, 판정 결과도 목 |
| RAG 추천 `recovery_program_recommendations` | **목 추천 + match_reason 문구** |
| 상담사·슬롯 `recovery_counselors` `recovery_counselor_slots` | **목 2~3명 + 고정 가용 슬롯** |
| 사후점검 결과 `recovery_followup_results` `recovery_execution_status` | **시연용 목 결과** (회복 완료 / 진행중 등) |
| 거버넌스 `audit_ai_generations` `audit_model_versions` `audit_ruleset_versions` `audit_notifications` | **버전 문자열·로그 목** ("model v0.3", "ruleset v2025-06") |

### 목 아님 — 실제 로직 필요
`core_users` 인증 · `core_consents` 동의 상태전이 (법적 증빙) · `audit_consent_logs` append · `source_adjustments` 사용자 입력 CRUD · `recovery_user_option_selections` 선택 · `recovery_packets` 버전 체인 · `recovery_consultations` 예약 생성 · `audit_holidays` (한국 공휴일 **실제 시드**)

---

## 6.5 공모전 MVP 구현 우선순위

| 순위 | 테이블 | 사유 |
|---|---|---|
| **P0** | `core_businesses`, `core_consents`, `source_transactions`, `source_card_settlements`, `source_loan_schedules`, `source_recurring_expenses`, `forecast_runs`, `forecast_daily`, `forecast_risk_drivers` | 30일 예측 + 원인 TOP3 시연 최소 집합 (구현 완료) |
| **P0** | `forecast_coverage`, `forecast_daily_items`, `source_data_sources`, `forecast_run_narratives` | 홈/Dashboard/판단보류/안정상태 렌더 (V9, 구현 완료) |
| **P0** | `recovery_options`, `recovery_scenarios`, `recovery_user_option_selections` | 회복안 비교 화면 (구현 완료) |
| **P0** | `recovery_support_programs`, `recovery_program_eligibility_*`, `recovery_program_recommendations` | 지원제도 매칭 (V11) |
| **P0** | `recovery_packets`, `recovery_consultations`, `recovery_counselor_slots` | Packet 생성 → 상담 연결 시연 종점 |
| **P1** | `source_adjustments`, `source_adjustment_suggestions` | 판단보류/보정 플로우 (V10) |
| **P1** | `forecast_risk_driver_evidence`, `recovery_self_action_*`, `recovery_program_documents` | 근거 바텀시트 / 셀프 액션 (V10~V11) |
| **P2** | `recovery_packet_transfers`, `recovery_followup_*`, `recovery_execution_status` | 사후점검 (시연 3분 밖, V12) |
| **P2** | `audit_*` | 심사 어필용 거버넌스 (V13) |

---

## 6.6 미구현 (후속)

- **`counselor_queue` VIEW** — 상담자 대시보드. 정렬 키: `days_to_shortfall ASC`, `shortfall_amount_max DESC`, `recovery_packets.status = 'SENT'`. 뷰 마이그레이션 별도 추가 예정.
- 각 모듈의 `api/` 인터페이스·슬라이스 핸들러 — 본 문서는 데이터 계층까지만.

---

## 7. 확인이 필요한 미결 사항

1. **다중 사업자** — 한 `core_users`가 여러 `core_businesses`를 가질 수 있는지 (현재 1:N 설계).
2. **상담자 소속** — 은행 직원 vs 정책기관 상담사에 따라 `recovery_counselors` 권한 모델이 달라짐.
3. **Safety Buffer 기준** — `core_businesses.safety_buffer_amount` 고정값(기본 100만) vs 업종·규모 산식.
4. **회복안 최대 2개 제한** — DB 제약 vs 앱 레벨 (현재 앱 레벨, `V5` 주석에 명시됨).
5. **예측 결과 보관 기간** — 개인정보 최소보관 원칙상 `forecast_runs` 파기 정책 / 파티셔닝 (`PARTITION BY RANGE (TO_DAYS(base_date))`).
6. **부분 인덱스 대체** — `source_adjustments` 활성행 조회를 복합 인덱스 필터로 확정 (현재) vs 생성컬럼 인덱스.
7. **물리 격리 수준** — 단일 DB + 접두사 유지(현재) vs `forecast_*`만 별도 DB.
8. **`member` vs `core_users` 이원화** — 로그인 계정이 두 테이블에 존재. `member`=인증 전용 / `core_users`=프로필로 역할 분리할지, 통합할지.
9. **"안전 잔액" 숫자 정의** — 홈 화면 "약 83만 원"이 `min_balance_expected` 재사용인지, `forecast_runs`에 별도 스냅샷 컬럼(`safety_balance_amount`)을 둘지.
