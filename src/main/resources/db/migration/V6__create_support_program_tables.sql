CREATE TABLE recovery_support_programs (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    program_code         VARCHAR(50)  NOT NULL,
    name                 VARCHAR(200) NOT NULL,
    agency               VARCHAR(100),
    support_content      TEXT,
    limit_amount         BIGINT,
    interest_rate_text   VARCHAR(100),
    term_text            VARCHAR(200),
    apply_deadline       DATE,
    apply_url            VARCHAR(500),
    official_source_url  VARCHAR(500),
    ruleset_version      VARCHAR(20),
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_support_programs_code (program_code),
    CONSTRAINT chk_recovery_support_programs_status CHECK (status IN ('ACTIVE', 'CLOSED'))
) ENGINE = InnoDB;

CREATE TABLE recovery_program_eligibility_rules (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    program_id      BIGINT      NOT NULL,
    rule_code       VARCHAR(50) NOT NULL,
    label           VARCHAR(200) NOT NULL,
    rule_expression JSON,
    evaluation_type VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_recovery_program_eligibility_rules_program (program_id),
    CONSTRAINT fk_recovery_program_eligibility_rules_program
        FOREIGN KEY (program_id) REFERENCES recovery_support_programs (id) ON DELETE CASCADE,
    CONSTRAINT chk_recovery_program_eligibility_rules_eval_type
        CHECK (evaluation_type IN ('AUTO', 'COUNSELOR_ONLY'))
) ENGINE = InnoDB;

CREATE TABLE recovery_program_eligibility_checks (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    business_id      BIGINT      NOT NULL,
    program_id       BIGINT      NOT NULL,
    forecast_run_id  BIGINT,
    result           VARCHAR(20) NOT NULL,
    reason_text      TEXT,
    is_advisory      BOOLEAN     NOT NULL DEFAULT TRUE,
    ruleset_version  VARCHAR(20),
    checked_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_recovery_program_eligibility_checks_business (business_id),
    KEY idx_recovery_program_eligibility_checks_program (program_id),
    CONSTRAINT fk_recovery_program_eligibility_checks_business
        FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_recovery_program_eligibility_checks_program
        FOREIGN KEY (program_id) REFERENCES recovery_support_programs (id),
    CONSTRAINT fk_recovery_program_eligibility_checks_run
        FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE SET NULL,
    CONSTRAINT chk_recovery_program_eligibility_checks_result
        CHECK (result IN ('LIKELY_PASS', 'NEEDS_REVIEW', 'LIKELY_FAIL', 'UNKNOWN')),
    -- 자동 자격판정 아님: 항상 advisory(참고용)여야 함
    CONSTRAINT chk_recovery_program_eligibility_checks_advisory CHECK (is_advisory = TRUE)
) ENGINE = InnoDB;
