-- 셀프 액션 저장 / 지원사업 상세(필요서류) / 지원사업 목록(추천) / 상담 예약(대상 회복안·가용 슬롯)

-- 자체 실행 계획. 셀프 액션 저장 화면
CREATE TABLE recovery_self_action_plans (
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    business_id          BIGINT      NOT NULL,
    forecast_run_id      BIGINT      NOT NULL,
    recovery_option_id   BIGINT      NOT NULL,
    expected_effect_text TEXT,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    saved_at             DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_recovery_self_action_plans_run (forecast_run_id),
    CONSTRAINT fk_recovery_self_action_plans_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_recovery_self_action_plans_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE CASCADE,
    CONSTRAINT fk_recovery_self_action_plans_option FOREIGN KEY (recovery_option_id) REFERENCES recovery_options (id),
    CONSTRAINT chk_recovery_self_action_plans_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
) ENGINE = InnoDB;

-- 자체 실행 준비 항목 (예정일 입력). 셀프 액션 저장 화면 "자체 실행 준비 항목"
CREATE TABLE recovery_self_action_items (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    self_action_plan_id  BIGINT       NOT NULL,
    title                VARCHAR(200) NOT NULL,
    target_date          DATE,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    memo                 VARCHAR(200),
    PRIMARY KEY (id),
    KEY idx_recovery_self_action_items_plan (self_action_plan_id),
    CONSTRAINT fk_recovery_self_action_items_plan
        FOREIGN KEY (self_action_plan_id) REFERENCES recovery_self_action_plans (id) ON DELETE CASCADE,
    CONSTRAINT chk_recovery_self_action_items_status CHECK (status IN ('PENDING', 'DONE'))
) ENGINE = InnoDB;

-- 지원제도 필요서류. 지원사업 상세 화면 "필요서류"
CREATE TABLE recovery_program_documents (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    program_id  BIGINT       NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(300),
    is_required BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    KEY idx_recovery_program_documents_program (program_id),
    CONSTRAINT fk_recovery_program_documents_program
        FOREIGN KEY (program_id) REFERENCES recovery_support_programs (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- 예측 실행별 지원제도 추천 목록. 지원사업 목록(추천 정렬) / 회복안 비교
CREATE TABLE recovery_program_recommendations (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    forecast_run_id BIGINT      NOT NULL,
    program_id      BIGINT      NOT NULL,
    rank_no         INT         NOT NULL,
    match_reason    TEXT,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_program_recommendations_run_program (forecast_run_id, program_id),
    CONSTRAINT fk_recovery_program_recommendations_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE CASCADE,
    CONSTRAINT fk_recovery_program_recommendations_program FOREIGN KEY (program_id) REFERENCES recovery_support_programs (id)
) ENGINE = InnoDB;

-- 상담에서 다룰 회복안 (N:M). 상담 예약 화면 "선택한 회복안"
CREATE TABLE recovery_consultation_options (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    consultation_id    BIGINT NOT NULL,
    recovery_option_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_consultation_options (consultation_id, recovery_option_id),
    CONSTRAINT fk_recovery_consultation_options_consultation
        FOREIGN KEY (consultation_id) REFERENCES recovery_consultations (id) ON DELETE CASCADE,
    CONSTRAINT fk_recovery_consultation_options_option
        FOREIGN KEY (recovery_option_id) REFERENCES recovery_options (id)
) ENGINE = InnoDB;

-- 상담자 예약 가능 슬롯. 상담 예약 화면 "예약 가능 일시 선택" (MVP는 시드 목데이터)
CREATE TABLE recovery_counselor_slots (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    counselor_id BIGINT      NOT NULL,
    start_at     DATETIME(6) NOT NULL,
    end_at       DATETIME(6) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    PRIMARY KEY (id),
    KEY idx_recovery_counselor_slots_counselor_start (counselor_id, start_at),
    CONSTRAINT fk_recovery_counselor_slots_counselor
        FOREIGN KEY (counselor_id) REFERENCES recovery_counselors (id) ON DELETE CASCADE,
    CONSTRAINT chk_recovery_counselor_slots_status CHECK (status IN ('OPEN', 'BOOKED', 'BLOCKED')),
    CONSTRAINT chk_recovery_counselor_slots_range CHECK (start_at < end_at)
) ENGINE = InnoDB;
