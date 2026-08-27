CREATE TABLE forecast_runs (
    id                        BIGINT      NOT NULL AUTO_INCREMENT,
    business_id               BIGINT      NOT NULL,
    consent_id                BIGINT      NOT NULL,
    base_date                 DATE        NOT NULL,
    horizon_days              INT         NOT NULL DEFAULT 30,
    status                    VARCHAR(20) NOT NULL,
    confidence_level          VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    coverage_overall          DECIMAL(5, 2),
    first_shortfall_date      DATE,
    days_to_shortfall         INT,
    min_balance_conservative  BIGINT,
    min_balance_expected      BIGINT,
    min_balance_optimistic    BIGINT,
    shortfall_amount_min      BIGINT,
    shortfall_amount_max      BIGINT,
    is_buffer_met             BOOLEAN     NOT NULL DEFAULT FALSE,
    model_version             VARCHAR(20) NOT NULL,
    ruleset_version           VARCHAR(20) NOT NULL,
    triggered_by              VARCHAR(20) NOT NULL,
    created_at                DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_forecast_runs_business_date (business_id, base_date DESC),
    CONSTRAINT fk_forecast_runs_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_forecast_runs_consent FOREIGN KEY (consent_id) REFERENCES core_consents (id),
    CONSTRAINT chk_forecast_runs_status CHECK (status IN ('RISK', 'STABLE', 'HOLD')),
    -- 판단보류가 아니면 최저잔액 범위는 반드시 존재
    CONSTRAINT chk_hold_nullable CHECK (
        status = 'HOLD' OR min_balance_expected IS NOT NULL
        ),
    -- 보수적 <= 예상 <= 낙관
    CONSTRAINT chk_band_order CHECK (
        min_balance_conservative IS NULL
            OR (min_balance_conservative <= min_balance_expected
            AND min_balance_expected <= min_balance_optimistic)
        )
) ENGINE = InnoDB;

CREATE TABLE forecast_daily (
    id                           BIGINT      NOT NULL AUTO_INCREMENT,
    forecast_run_id              BIGINT      NOT NULL,
    target_date                  DATE        NOT NULL,
    d_day                        INT         NOT NULL,
    opening_balance              BIGINT      NOT NULL,
    confirmed_inflow             BIGINT      NOT NULL DEFAULT 0,
    confirmed_outflow            BIGINT      NOT NULL DEFAULT 0,
    expected_inflow_min          BIGINT      NOT NULL DEFAULT 0,
    expected_inflow_max          BIGINT      NOT NULL DEFAULT 0,
    expected_outflow_min         BIGINT      NOT NULL DEFAULT 0,
    expected_outflow_max         BIGINT      NOT NULL DEFAULT 0,
    adjustment_net               BIGINT      NOT NULL DEFAULT 0,
    closing_balance_conservative BIGINT      NOT NULL,
    closing_balance_expected     BIGINT      NOT NULL,
    closing_balance_optimistic   BIGINT      NOT NULL,
    is_shortfall                 BOOLEAN     NOT NULL DEFAULT FALSE,
    is_holiday                   BOOLEAN     NOT NULL DEFAULT FALSE,
    holiday_shift_note           VARCHAR(200),
    PRIMARY KEY (id),
    UNIQUE KEY uk_forecast_daily_run_date (forecast_run_id, target_date),
    CONSTRAINT fk_forecast_daily_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE forecast_risk_drivers (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    forecast_run_id     BIGINT      NOT NULL,
    rank_no             INT         NOT NULL,
    driver_code         VARCHAR(50) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    contribution_amount BIGINT,
    is_estimating       BOOLEAN     NOT NULL DEFAULT FALSE,
    occurrence_date     DATE,
    description         TEXT,
    assumption_text     TEXT,
    shap_value          DECIMAL(10, 4),
    PRIMARY KEY (id),
    KEY idx_forecast_risk_drivers_run (forecast_run_id),
    CONSTRAINT fk_forecast_risk_drivers_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE CASCADE
) ENGINE = InnoDB;
