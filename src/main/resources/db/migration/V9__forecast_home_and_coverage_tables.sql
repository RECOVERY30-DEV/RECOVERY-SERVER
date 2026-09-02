-- 사업자 홈 / Dashboard / 판단보류 / 안정 상태 / 데이터 범위 확인 화면 지원
-- 피그마 대조 결과 추가된 테이블과 컬럼 (docs/db-design.md 3.B, 3.C 참고)

-- 원천 연동 커넥션의 현재 상태 (live). "데이터 범위 확인" 화면 + 홈/Dashboard "분석 데이터 범위"
CREATE TABLE source_data_sources (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    business_id      BIGINT      NOT NULL,
    source_type      VARCHAR(30) NOT NULL,
    institution_name VARCHAR(100),
    coverage_rate    DECIMAL(5, 2),
    period_months    INT         NOT NULL DEFAULT 6,
    last_synced_at   DATETIME(6),
    sync_status      VARCHAR(20) NOT NULL DEFAULT 'SYNCED',
    PRIMARY KEY (id),
    UNIQUE KEY uk_source_data_sources_business_type (business_id, source_type),
    CONSTRAINT fk_source_data_sources_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT chk_source_data_sources_type
        CHECK (source_type IN ('BANK_ACCOUNT', 'CARD_SETTLEMENT', 'LOAN', 'AUTO_TRANSFER')),
    CONSTRAINT chk_source_data_sources_sync_status
        CHECK (sync_status IN ('SYNCED', 'PARTIAL', 'FAILED'))
) ENGINE = InnoDB;

-- 예측 1회 시점의 소스별 Coverage 스냅샷. 임계 미달 시 forecast_runs.status = HOLD 근거
CREATE TABLE forecast_coverage (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    forecast_run_id    BIGINT      NOT NULL,
    source_type        VARCHAR(30) NOT NULL,
    coverage_rate      DECIMAL(5, 2),
    last_synced_at     DATETIME(6),
    is_below_threshold BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_forecast_coverage_run_type (forecast_run_id, source_type),
    CONSTRAINT fk_forecast_coverage_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE CASCADE,
    CONSTRAINT chk_forecast_coverage_type
        CHECK (source_type IN ('BANK_ACCOUNT', 'CARD_SETTLEMENT', 'LOAN', 'AUTO_TRANSFER'))
) ENGINE = InnoDB;

-- 일자별 근거 라인. Dashboard "일자별 현금흐름" + 안정 상태 "일자별 근거 바텀시트"
-- 모든 금액이 원천 레코드(ref_type/ref_id)로 역추적 가능해야 한다 (근거 공개 원칙)
CREATE TABLE forecast_daily_items (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    forecast_daily_id BIGINT       NOT NULL,
    item_kind         VARCHAR(20)  NOT NULL,
    label             VARCHAR(100) NOT NULL,
    sub_label         VARCHAR(200),
    direction         CHAR(1)      NOT NULL,
    amount_min        BIGINT       NOT NULL,
    amount_max        BIGINT       NOT NULL,
    ref_type          VARCHAR(30),
    ref_id            BIGINT,
    PRIMARY KEY (id),
    KEY idx_forecast_daily_items_daily (forecast_daily_id),
    CONSTRAINT fk_forecast_daily_items_daily FOREIGN KEY (forecast_daily_id) REFERENCES forecast_daily (id) ON DELETE CASCADE,
    CONSTRAINT chk_forecast_daily_items_kind CHECK (item_kind IN ('CONFIRMED', 'EXPECTED', 'ADJUSTMENT')),
    CONSTRAINT chk_forecast_daily_items_direction CHECK (direction IN ('I', 'O')),
    CONSTRAINT chk_forecast_daily_items_amount_range CHECK (amount_min <= amount_max)
) ENGINE = InnoDB;

-- 예측 실행별 서술 문구. 안정 상태 "판단 근거"/"상태가 바뀔 수 있는 경우", 홈 상태 라벨, 공통 고지문
CREATE TABLE forecast_run_narratives (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    forecast_run_id BIGINT      NOT NULL,
    kind            VARCHAR(30) NOT NULL,
    seq             INT         NOT NULL DEFAULT 0,
    text            TEXT        NOT NULL,
    PRIMARY KEY (id),
    KEY idx_forecast_run_narratives_run (forecast_run_id),
    CONSTRAINT fk_forecast_run_narratives_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE CASCADE,
    CONSTRAINT chk_forecast_run_narratives_kind
        CHECK (kind IN ('STATUS_LABEL', 'STABLE_REASON', 'RISK_NOTE', 'STATE_CHANGE_HINT', 'DISCLAIMER'))
) ENGINE = InnoDB;

-- 부족 원인 카드 표시 보강 (원인 상세 / Dashboard / 홈 대조)
--  occurrence_text  : "11월 20일·25일 발생" 처럼 복수 날짜 문자열 (정렬용 occurrence_date는 유지)
--  impact_period_text: "6월 15일~28일 영향"
--  metric_text      : "-18%", "약 32% 감소" 처럼 금액이 아닌 지표
ALTER TABLE forecast_risk_drivers
    ADD COLUMN occurrence_text   VARCHAR(100) NULL AFTER occurrence_date,
    ADD COLUMN impact_period_text VARCHAR(100) NULL AFTER occurrence_text,
    ADD COLUMN metric_text       VARCHAR(50)  NULL AFTER contribution_amount;
