# API 설계 — 사업자 홈 화면 (Forecast)

> 대상 화면: Figma `사업자 홈 화면` (node `313:5046`)
> 방식: 리소스(박스) 단위 분리. 홈 = 엔드포인트 6개 + 클라이언트 상수
> 모듈: `forecast` · 상태: 설계만 (미구현)

---

## 0. 값 표현 규칙 (전 엔드포인트 공통)

프레젠테이션 문자열은 저장/전송하지 않는다. **원자값만 주고 포맷은 클라이언트가 한다.**

| 종류 | API가 주는 형태 | 클라가 만드는 것 |
|---|---|---|
| 금액 | 원 단위 정수 (부호 포함) `-1280000` | "−128만 원", 콤마 |
| 날짜 | ISO `2025-07-26` (`LocalDate`) | "7월 26일 예상" |
| 시각 | ISO-8601 UTC (`Instant`) `2025-07-15T23:32:00Z` | KST 변환 후 "오전 8:32" |
| D-day | 정수 `11` | "D-11" |
| 게이지/진행률 | `dDay` + `horizonDays` 만 | 비율 계산·바 렌더 |
| 상태 | enum (`RISK`/`STABLE`/`HOLD`) + 편의용 `statusLabel` 문자열 | enum으로 자체 매핑도 가능 |

- 응답 래퍼: `ApiResponse<T>` (`success` / `data` / `error`). 실패는 `BusinessException(ErrorCode)` → `GlobalExceptionHandler`.
- `forecastRunId`: A가 준 값을 B~F 경로에 그대로 사용하고, B~F 응답 본문에도 **에코**한다 → 콜 사이에 새 run이 생겨도 클라가 불일치를 감지.

---

## 1. 홈 화면 = 엔드포인트 6개 + 클라 상수

| Figma 박스 | 엔드포인트 |
|---|---|
| 헤더 (기준일·갱신시각) + 상태 kicker | **A** `GET /api/businesses/{businessId}/forecasts/latest` |
| "예상 최저잔액" 박스 | **B** `GET /api/forecasts/{runId}/min-balance` |
| "첫 부족 예상일" 박스 | **C** `GET /api/forecasts/{runId}/shortfall` |
| "안전 잔액" 박스 | **D** `GET /api/forecasts/{runId}/safety-buffer` |
| "주요 위험 신호" 박스 | **E** `GET /api/forecasts/{runId}/risk-drivers?limit=3` |
| "분석 데이터 범위" 박스 | **F** `GET /api/forecasts/{runId}/coverage` |

**엔드포인트 없이 클라이언트 상수로 처리** (유저·run과 무관한 고정 카피):

| Figma 요소 | 처리 |
|---|---|
| "Recovery30" 로고 | 브랜드 에셋 |
| "30일 현금흐름 현황" (화면 타이틀) | 클라 상수 |
| "예상 최저잔액" / "보수적" / "낙관적" 라벨 | 클라 상수 |
| "시나리오 기준 범위이며 확정 결과가 아닙니다." (밴드 하단) | 클라 상수 |
| "오늘" / "향후 30일" / "첫 부족 예상일" 라벨 | 클라 상수 |
| "안전 잔액" / "현재 기준" 라벨 | 클라 상수 |
| "주요 위험 신호" 헤딩 / 빨간 사각 bullet | 클라 상수 |
| "위험분석 바로가기" CTA + "연체 전 회복 지원용 분석으로 신용평가·대출 심사와 무관합니다." | 클라 상수 (딥링크 타깃은 Dashboard, `forecastRunId`만 있으면 클라가 구성) |
| "분석 데이터 범위" 헤딩 / "현금매출·타행자금 등 일부 정보는 포함되지 않을 수 있습니다." | 클라 상수 |
| 하단 탭바 (홈/현금흐름/회복안/관리) | 클라 네비게이션 |

**호출 그래프**: `A` → 응답의 `forecastRunId` → `B·C·D·E·F` 병렬. depth 2, 총 6 콜.

---

## 2. A — `GET /api/businesses/{businessId}/forecasts/latest`

최신 `forecast_runs` 1건의 식별자 + 메타. 모든 예측 화면의 진입점.

**쿼리**
```
SELECT * FROM forecast_runs
 WHERE business_id = :businessId
 ORDER BY base_date DESC, created_at DESC
 LIMIT 1;
-- + forecast_run_narratives WHERE forecast_run_id = :id AND kind = 'STATUS_LABEL' ORDER BY seq LIMIT 1
```

**응답 `LatestForecastView`**

| 필드 | 타입 | Figma 위치 | 원천 컬럼 | Mock |
|---|---|---|---|---|
| `forecastRunId` | long | (미표시 — B~F 호출용) | `forecast_runs.id` | — |
| `baseDate` | date | 헤더 "2025년 7월 15일 기준"의 날짜부 | `base_date` | ✅ 고정 `2025-07-15` |
| `updatedAt` | instant | 헤더 "· 최근 갱신 오전 8:32" | `created_at` | ✅ 고정 시각 |
| `horizonDays` | int | (미표시 — C 게이지에서 사용) | `horizon_days` | 고정 `30` |
| `status` | enum | 박스4 "주의 필요" kicker, 박스3 shield 색 결정 | `status` | ✅ 데모상 `RISK` |
| `statusLabel` | string | 박스4 상단 "주의 필요" | `forecast_run_narratives(STATUS_LABEL)`, 없으면 status 파생 | ✅ 파생/목 |
| `confidenceLevel` | enum | (미표시 — 판단보류 화면에서 사용) | `confidence_level` | 고정 `MEDIUM` |

**빈 경우**: run 0건 → **`404 FORECAST_404_1`** ("예측 이력이 없습니다"). 클라는 온보딩/빈 홈 렌더.

> ⚠️ **Figma 불일치**: 화면은 `D-11`(부족일 존재)인데 박스3 shield가 "안전상태", 박스4 kicker는 "주의 필요"로 혼재. 컴포넌트 기본 상태 미override로 보임. **API는 `status` 기준으로 일관 처리** — 부족일이 있으면 `RISK`, shield/label도 거기에 맞춘다.

---

## 3. B — `GET /api/forecasts/{forecastRunId}/min-balance`

**박스**: "예상 최저잔액" (슬라이더)
**재사용**: 홈 · Dashboard · 원인상세 · 판단보류 · 안정상태 · 회복안비교 · Packet

**쿼리**: A와 같은 행에서 `min_balance_*` 3컬럼.

**응답 `MinBalanceView`**

| 필드 | 타입 | Figma 위치 | 원천 컬럼 | Mock |
|---|---|---|---|---|
| `forecastRunId` | long | echo | — | — |
| `available` | bool | `false`면 "범위 산출 불가"(판단보류) | `min_balance_expected IS NOT NULL` | — |
| `conservative` | long(원) | 슬라이더 왼쪽 값 "−128만" + 헤드라인 좌측 "−128만 원" | `min_balance_conservative` | ✅ `-1280000` |
| `expected` | long(원) | 슬라이더 ▼▲ 마커 위치 + 헤드라인 우측 "+54만 원" | `min_balance_expected` | ✅ `540000` |
| `optimistic` | long(원) | 슬라이더 오른쪽 값 "83만" | `min_balance_optimistic` | ✅ `830000` |

**클라 렌더 규칙**
- 헤드라인 = `conservative` ~ `expected` ("최악 ~ 예상")
- 트랙 = `conservative` → `optimistic` 선형, 마커 = `(expected − conservative) / (optimistic − conservative)`
- `HOLD` → `200`, `available=false`, 3값 `null`

> ⚠️ **Figma 목값 불일치**: 헤드라인은 `-128 ~ +54`, 슬라이더 끝은 `-128 ~ 83`. 시드 시 `conservative ≤ expected ≤ optimistic`로 맞출 것 (DB `chk_band_order`가 강제).

---

## 4. C — `GET /api/forecasts/{forecastRunId}/shortfall`

**박스**: "첫 부족 예상일"
**재사용**: 홈 · Dashboard · 원인상세("부족 예상 시점 요약") · 회복안비교 · Packet · 안정상태(없음 표시)

**응답 `ShortfallView`**

| 필드 | 타입 | Figma 위치 | 원천 컬럼 | Mock |
|---|---|---|---|---|
| `forecastRunId` | long | echo | — | — |
| `hasShortfall` | bool | `false`면 "30일 이내 없음"(안정상태) | `first_shortfall_date IS NOT NULL` | — |
| `dDay` | int \| null | "D-11"의 **11** | `days_to_shortfall` | ✅ `11` |
| `expectedDate` | date \| null | "7월 26일 예상" | `first_shortfall_date` | ✅ `2025-07-26` |
| `horizonDays` | int | 게이지 오른쪽 "향후 30일" | `horizon_days` | 고정 `30` |
| `shortfallAmountMin` | long \| null | (홈 미표시 — Dashboard/원인상세 "예상 부족액") | `shortfall_amount_min` | ✅ 목 |
| `shortfallAmountMax` | long \| null | 〃 | `shortfall_amount_max` | ✅ 목 |

**클라 렌더 규칙**
- 게이지 채움 = `dDay / horizonDays`
- "예상" 접미사, "D-" 접두사는 클라
- `STABLE`/부족 없음 → `hasShortfall=false`, `dDay`·`expectedDate`·`amount*` `null`

---

## 5. D — `GET /api/forecasts/{forecastRunId}/safety-buffer`

**박스**: "안전 잔액"
**재사용**: 홈 · 안정상태("Safety Buffer 충족")

**응답 `SafetyBufferView`**

| 필드 | 타입 | Figma 위치 | 원천 컬럼 | Mock |
|---|---|---|---|---|
| `forecastRunId` | long | echo | — | — |
| `amount` | long(원) | "약 83만 원" | (결정 필요, 6-①) | ✅ `830000` |
| `bufferAmount` | long(원) | (미표시 — 판정 기준선) | `core_businesses.safety_buffer_amount` → `forecast_runs` 스냅샷 권장 | 고정 `1000000` |
| `bufferMet` | bool | shield 아이콘 + "안전상태" 텍스트/색 | `is_buffer_met` | ✅ 목 |
| `label` | string | shield 옆 "안전상태" | `bufferMet` + `status` 파생 | 파생 |

**클라 렌더 규칙**: "약" 접두사, "만 원" 단위는 클라.

> **크로스모듈**: `bufferAmount`는 `business` 모듈 소유. forecast 배치가 run 생성 시 `safety_buffer_amount`(+ `safety_balance_amount`)를 `forecast_runs`에 **스냅샷 복사**하면 D도 forecast 안에서 끝난다(크로스모듈 0). 미채택 시 D만 `BusinessApi` 호출.
> **정의 확인**: "약 83만 원" = 슬라이더 `optimistic` 83만과 동일 → 안전 잔액 = 낙관 케이스 최저잔액일 가능성. 정의 확정 필요.

---

## 6. E — `GET /api/forecasts/{forecastRunId}/risk-drivers?limit=3`

**박스**: "주요 위험 신호"
**재사용**: 홈(`?limit=3`) · Dashboard · 원인상세(`?include=evidence`) · 회복안비교 · Packet

**쿼리**
```
SELECT * FROM forecast_risk_drivers
 WHERE forecast_run_id = :runId
 ORDER BY rank_no
 LIMIT :limit;   -- limit 없으면 전체
```

**응답 `List<RiskDriverView>`**

| 필드 | 타입 | Figma 위치 | 원천 컬럼 | Mock |
|---|---|---|---|---|
| `rank` | int | 행 순서 | `rank_no` | ✅ 목 |
| `driverCode` | string | (미표시 — 아이콘/딥링크 매핑) | `driver_code` | ✅ 목 |
| `title` | string | 행 왼쪽 "월말 원리금 임차료 집중" / "최근 4주 매출 감소 추세" / "자동이체 3건 납부일 겹침" | `title` | ✅ 목 |
| `displayValue` | string | 행 오른쪽 "7월31일" / "-18%" / "7월28일" | 파생 (아래) | ✅ 목 |
| `occurrenceDate` | date \| null | `displayValue` 소스 | `occurrence_date` | ✅ 목 |
| `occurrenceText` | string \| null | 〃 (복수일 "20일·25일 발생") | `occurrence_text` | ✅ 목 |
| `metricText` | string \| null | 〃 ("-18%") | `metric_text` | ✅ 목 |
| `contributionAmount` | long \| null | (홈 미표시 — 상세 "-185만 원", `null`이면 "확인 필요") | `contribution_amount` | ✅ 목 |
| `estimating` | bool | (홈 미표시 — 상세 "근거 데이터 부족") | `is_estimating` | ✅ 목 |
| `evidence` | array \| null | `?include=evidence`일 때만 (원인상세 "근거 거래") | `forecast_risk_driver_evidence` | ✅ 목 |

**`displayValue` 파생 규칙**: `metricText` → (없으면) `occurrenceText` → (없으면) `occurrenceDate`.
날짜 포맷("7월31일")은 서버가 편의로 계산해 `displayValue`에 넣되, **원자값(`occurrenceDate` 등)도 항상 동봉**하여 클라가 다르게 렌더할 여지를 남긴다. 위험 신호 지표는 종류가 제각각("-18%", "3건", "약 32% 감소")이라 `metricText`는 표준화하지 않고 문자열로 둔다.

**빈 경우**: run은 있고 원인 0건 → `200 []`. runId 없음 → `404 FORECAST_404_1`.

---

## 7. F — `GET /api/forecasts/{forecastRunId}/coverage`

**박스**: "분석 데이터 범위"
**재사용**: 홈 · Dashboard · 판단보류 · 데이터범위확인

**쿼리**: `forecast_coverage WHERE forecast_run_id = :runId` (소스타입별 최대 4행)

**응답 `List<CoverageView>`**

| 필드 | 타입 | Figma 위치 | 원천 컬럼 | Mock |
|---|---|---|---|---|
| `sourceType` | enum | 행 라벨 (클라 매핑: "사업자 계좌"/"카드 정산"/"대출"/"자동이체") | `source_type` | ✅ 목 |
| `status` | enum `COMPLETE`/`PARTIAL`/`MISSING` | 행 오른쪽 "갱신 완료" / "부분 반영" | `is_below_threshold` 파생 | ✅ 목 |
| `coverageRate` | decimal \| null | (홈 미표시 — 판단보류 "72%" 등) | `coverage_rate` | ✅ `BANK 95 / CARD 92 / LOAN 88 / AUTO 61` |
| `lastSyncedAt` | instant \| null | (홈 미표시 — Dashboard "오늘 09:14 반영") | `last_synced_at` | ✅ 고정 시각 |
| `belowThreshold` | bool | (판정 근거) | `is_below_threshold` | ✅ 목 |

**클라 렌더 규칙**: 홈은 3줄이므로 클라가 `LOAN` + `AUTO_TRANSFER`를 "자동이체/대출" 1행으로 병합하고 `status`는 둘 중 나쁜 쪽(`PARTIAL`)을 취한다. API는 정규 4종을 그대로 반환.

**빈 경우**: 행 0건 → `200 []` (클라가 4종을 `MISSING`으로 처리).

---

## 8. 구현 시 필요한 것

- **ErrorCode 추가**: `FORECAST_404_1(HttpStatus.NOT_FOUND, "FORECAST_404_1", "예측 이력이 없습니다")`
- **슬라이스 6개** (`forecast/`): `getlatestforecast/`, `getminbalance/`, `getshortfall/`, `getsafetybuffer/`, `getriskdrivers/`, `getforecastcoverage/`
- **`forecast/internal/` 리포지토리**: `ForecastRunRepository`, `ForecastRunNarrativeRepository`, `RiskDriverRepository`, `ForecastCoverageRepository` (+ evidence용 `RiskDriverEvidenceRepository`)
- **(D 스냅샷안 채택 시) `V14`**: `forecast_runs`에 `safety_buffer_amount`, `safety_balance_amount` 컬럼 추가 + `ForecastRun` 필드 2개
- **Swagger**: 6개 Handler에 `@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")`, record 필드에 `@Schema`
- **테스트** (`@SpringBootTest @AutoConfigureMockMvc @Transactional` + MockMvc): A(이력 없음 404 / base_date 최신 우선 / HOLD면 minBalance available=false), C(STABLE이면 hasShortfall=false), E(limit 3 / 0건 빈 배열 / include=evidence), F(임계 미만이면 status=PARTIAL)

---

## 9. 결정해야 할 것

1. **안전 잔액 `amount` 정의** — `min_balance_optimistic` 재사용 vs `forecast_runs.safety_balance_amount` 스냅샷 컬럼 추가
2. **`safety_buffer` 크로스모듈** — `forecast_runs`에 `safety_buffer_amount` 스냅샷(권장) vs `BusinessApi` 호출
3. **`displayValue`** — 서버 계산(권장, 원자값 동봉) vs 클라 계산
4. **이력 없을 때** — `404 FORECAST_404_1`(권장) vs `200` 빈 상태 바디

---

## 10. MVP Mock data 고정 목록

예측 엔진·데이터 연동이 없으므로 아래는 전부 시드(고정값):

**`forecast_runs` 1행** (사업자 홈 데모용)
| 컬럼 | 값 |
|---|---|
| `status` | `RISK` |
| `base_date` | `2025-07-15` |
| `created_at` | 고정 시각 (KST 08:32) |
| `horizon_days` | `30` |
| `confidence_level` | `MEDIUM` |
| `coverage_overall` | `84.00` |
| `min_balance_conservative / expected / optimistic` | `-1280000 / 540000 / 830000` |
| `days_to_shortfall` | `11` |
| `first_shortfall_date` | `2025-07-26` |
| `shortfall_amount_min / max` | 목값 |
| `is_buffer_met` | 목 (정의 확정 후) |
| `model_version / ruleset_version` | `"fc-model-v0.3"` / `"rule-2025-06"` |
| `triggered_by` | `"SCHEDULED"` |

**`forecast_coverage` 4행**: `BANK_ACCOUNT 95% COMPLETE` · `CARD_SETTLEMENT 92% COMPLETE` · `LOAN 88% COMPLETE` · `AUTO_TRANSFER 61% PARTIAL`, `last_synced_at` 전부 고정 시각.

**`forecast_risk_drivers` 3행**
| rank | title | metric_text | occurrence_date | contribution_amount |
|---|---|---|---|---|
| 1 | 월말 원리금 임차료 집중 | — | `2025-07-31` | 목 |
| 2 | 최근 4주 매출 감소 추세 | `-18%` | — | 목 |
| 3 | 자동이체 3건 납부일 겹침 | — | `2025-07-28` | 목 |

**`core_businesses.safety_buffer_amount`** = `1000000` (기본값)
**`forecast_run_narratives`** (선택): `STATUS_LABEL` = `"주의 필요"`

**시드 정합성 규칙**: `conservative ≤ expected ≤ optimistic` / `status='RISK'`면 `first_shortfall_date` 존재 / `coverage_rate < 70`이면 `is_below_threshold = true`.
