CREATE TABLE recovery_options (
    id                         BIGINT      NOT NULL AUTO_INCREMENT,
    option_code                VARCHAR(50) NOT NULL,
    category                   VARCHAR(30) NOT NULL,
    expected_effect_text       TEXT,
    monthly_burden_change_text TEXT,
    precondition_text          TEXT,
    difficulty                 VARCHAR(10),
    requires_review            BOOLEAN     NOT NULL DEFAULT FALSE,
    disclaimer                 TEXT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_options_code (option_code),
    CONSTRAINT chk_recovery_options_category
        CHECK (category IN ('FINANCIAL_CONSULT', 'SELF_ACTION', 'SUPPORT_PROGRAM')),
    CONSTRAINT chk_recovery_options_difficulty CHECK (difficulty IN ('LOW', 'MID', 'HIGH'))
) ENGINE = InnoDB;

CREATE TABLE recovery_scenarios (
    id                     BIGINT      NOT NULL AUTO_INCREMENT,
    forecast_run_id        BIGINT      NOT NULL,
    scenario_type          VARCHAR(20) NOT NULL,
    first_shortfall_date   DATE,
    min_balance            BIGINT,
    delta_days             INT,
    delta_min_balance      BIGINT,
    monthly_payment_delta  BIGINT,
    note                   TEXT,
    PRIMARY KEY (id),
    KEY idx_recovery_scenarios_run (forecast_run_id),
    CONSTRAINT fk_recovery_scenarios_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE CASCADE,
    CONSTRAINT chk_recovery_scenarios_type CHECK (scenario_type IN ('BASELINE', 'SIMULATED'))
) ENGINE = InnoDB;

CREATE TABLE recovery_scenario_options (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    scenario_id        BIGINT NOT NULL,
    recovery_option_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_scenario_options (scenario_id, recovery_option_id),
    CONSTRAINT fk_recovery_scenario_options_scenario FOREIGN KEY (scenario_id) REFERENCES recovery_scenarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_recovery_scenario_options_option FOREIGN KEY (recovery_option_id) REFERENCES recovery_options (id)
) ENGINE = InnoDB;

CREATE TABLE recovery_user_option_selections (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    forecast_run_id    BIGINT      NOT NULL,
    recovery_option_id BIGINT      NOT NULL,
    selected_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    -- 최대 2개 제한은 문서상 애플리케이션 레벨 책임으로 명시됨 (DB 강제 아님)
    KEY idx_recovery_user_option_selections_run (forecast_run_id),
    CONSTRAINT fk_recovery_user_option_selections_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE CASCADE,
    CONSTRAINT fk_recovery_user_option_selections_option FOREIGN KEY (recovery_option_id) REFERENCES recovery_options (id)
) ENGINE = InnoDB;
