-- 현금매출/타행·외부자금/예정수입/예정지출 입력 (4화면) + 정보 보정 + 원인 상세 "근거 거래"

-- 사용자 보정값. 4개 입력 화면을 adjustment_type 하나로 통합한다.
--  certainty = CONFIRMED : 예상·낙관 시나리오 모두 반영
--  certainty = ESTIMATED : 낙관 시나리오에만 반영, 보수적 시나리오 제외
CREATE TABLE source_adjustments (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    business_id      BIGINT      NOT NULL,
    adjustment_type  VARCHAR(30) NOT NULL,
    direction        CHAR(1)     NOT NULL,
    amount           BIGINT      NOT NULL,
    expected_date    DATE        NOT NULL,
    certainty        VARCHAR(20) NOT NULL,
    recurrence_rule  VARCHAR(100),
    expense_category VARCHAR(30),
    fund_source      VARCHAR(50),
    memo             VARCHAR(200),
    status           VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    applied_run_id   BIGINT,
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    -- PostgreSQL 부분 인덱스( WHERE status='SAVED' ) 대체: 복합 인덱스 + 쿼리에서 status 필터
    KEY idx_source_adjustments_active (business_id, status, expected_date),
    CONSTRAINT fk_source_adjustments_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_source_adjustments_run FOREIGN KEY (applied_run_id) REFERENCES forecast_runs (id) ON DELETE SET NULL,
    CONSTRAINT chk_source_adjustments_type
        CHECK (adjustment_type IN ('CASH_SALES', 'EXTERNAL_FUND', 'EXPECTED_INCOME', 'EXPECTED_EXPENSE')),
    CONSTRAINT chk_source_adjustments_direction CHECK (direction IN ('I', 'O')),
    CONSTRAINT chk_source_adjustments_amount CHECK (amount > 0),
    CONSTRAINT chk_source_adjustments_certainty CHECK (certainty IN ('CONFIRMED', 'ESTIMATED')),
    CONSTRAINT chk_source_adjustments_status CHECK (status IN ('DRAFT', 'SAVED', 'DISCARDED'))
) ENGINE = InnoDB;

-- 반복 패턴 추정 후보. 정보 보정 화면 "반복 패턴 추정 후보"
CREATE TABLE source_adjustment_suggestions (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    business_id           BIGINT      NOT NULL,
    adjustment_type       VARCHAR(30) NOT NULL,
    suggested_amount      BIGINT,
    suggested_rule        VARCHAR(100),
    evidence_text         VARCHAR(200),
    confidence            DECIMAL(5, 2),
    status                VARCHAR(20) NOT NULL DEFAULT 'PROPOSED',
    accepted_adjustment_id BIGINT,
    created_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_source_adjustment_suggestions_business (business_id),
    CONSTRAINT fk_source_adjustment_suggestions_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_source_adjustment_suggestions_accepted
        FOREIGN KEY (accepted_adjustment_id) REFERENCES source_adjustments (id) ON DELETE SET NULL,
    CONSTRAINT chk_source_adjustment_suggestions_type
        CHECK (adjustment_type IN ('CASH_SALES', 'EXTERNAL_FUND', 'EXPECTED_INCOME', 'EXPECTED_EXPENSE')),
    CONSTRAINT chk_source_adjustment_suggestions_status
        CHECK (status IN ('PROPOSED', 'ACCEPTED', 'REJECTED'))
) ENGINE = InnoDB;

-- 부족 원인별 근거 거래. 원인 상세 화면 "근거 거래" ("신한카드 정산 5건 · 6월 2일~11일")
CREATE TABLE forecast_risk_driver_evidence (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    risk_driver_id  BIGINT       NOT NULL,
    ref_type        VARCHAR(30),
    ref_id          BIGINT,
    label           VARCHAR(200) NOT NULL,
    period_text     VARCHAR(100),
    PRIMARY KEY (id),
    KEY idx_forecast_risk_driver_evidence_driver (risk_driver_id),
    CONSTRAINT fk_forecast_risk_driver_evidence_driver
        FOREIGN KEY (risk_driver_id) REFERENCES forecast_risk_drivers (id) ON DELETE CASCADE
) ENGINE = InnoDB;
