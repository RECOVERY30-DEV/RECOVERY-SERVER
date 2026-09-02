-- Recovery Packet 전송 상태 + 사후점검(30/60/90일) 화면

-- Packet 상담자 전송 이력. Recovery Packet 화면 "Packet 버전 및 전송 상태", 상담 예약 "정보 전송"
-- 전송 동의(consent_id) 없이는 행 생성 불가
CREATE TABLE recovery_packet_transfers (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    packet_id    BIGINT      NOT NULL,
    counselor_id BIGINT,
    channel      VARCHAR(20),
    scope_json   JSON        NOT NULL,
    consent_id   BIGINT      NOT NULL,
    sent_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_recovery_packet_transfers_packet (packet_id),
    CONSTRAINT fk_recovery_packet_transfers_packet FOREIGN KEY (packet_id) REFERENCES recovery_packets (id) ON DELETE CASCADE,
    CONSTRAINT fk_recovery_packet_transfers_counselor FOREIGN KEY (counselor_id) REFERENCES recovery_counselors (id),
    CONSTRAINT fk_recovery_packet_transfers_consent FOREIGN KEY (consent_id) REFERENCES core_consents (id)
) ENGINE = InnoDB;

-- 30/60/90일 사후 점검 일정. 사후점검 화면 + Packet "사후 점검 일정"
-- 추적 동의(consent_id) 없이는 행 생성 불가
CREATE TABLE recovery_followup_schedules (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    business_id     BIGINT      NOT NULL,
    packet_id       BIGINT,
    forecast_run_id BIGINT,
    checkpoint      VARCHAR(10) NOT NULL,
    scheduled_date  DATE        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    consent_id      BIGINT      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_followup_schedules_biz_packet_cp (business_id, packet_id, checkpoint),
    CONSTRAINT fk_recovery_followup_schedules_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_recovery_followup_schedules_packet FOREIGN KEY (packet_id) REFERENCES recovery_packets (id) ON DELETE SET NULL,
    CONSTRAINT fk_recovery_followup_schedules_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE SET NULL,
    CONSTRAINT fk_recovery_followup_schedules_consent FOREIGN KEY (consent_id) REFERENCES core_consents (id),
    CONSTRAINT chk_recovery_followup_schedules_checkpoint CHECK (checkpoint IN ('D30', 'D60', 'D90')),
    CONSTRAINT chk_recovery_followup_schedules_status CHECK (status IN ('SCHEDULED', 'DONE', 'SKIPPED'))
) ENGINE = InnoDB;

-- 사후 점검 결과. 사후점검 화면 "잔액 회복 현황"
CREATE TABLE recovery_followup_results (
    id                     BIGINT      NOT NULL AUTO_INCREMENT,
    followup_schedule_id   BIGINT      NOT NULL,
    balance_recovered      VARCHAR(10),
    has_delinquency        BOOLEAN     NOT NULL DEFAULT FALSE,
    baseline_balance       BIGINT,
    current_balance        BIGINT,
    recovery_amount        BIGINT,
    latest_forecast_run_id BIGINT,
    risk_status            VARCHAR(20),
    recorded_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_followup_results_schedule (followup_schedule_id),
    CONSTRAINT fk_recovery_followup_results_schedule
        FOREIGN KEY (followup_schedule_id) REFERENCES recovery_followup_schedules (id) ON DELETE CASCADE,
    CONSTRAINT fk_recovery_followup_results_run
        FOREIGN KEY (latest_forecast_run_id) REFERENCES forecast_runs (id) ON DELETE SET NULL,
    CONSTRAINT chk_recovery_followup_results_balance_recovered
        CHECK (balance_recovered IS NULL OR balance_recovered IN ('YES', 'PARTIAL', 'NO')),
    CONSTRAINT chk_recovery_followup_results_risk_status
        CHECK (risk_status IS NULL OR risk_status IN ('RISK', 'STABLE', 'HOLD'))
) ENGINE = InnoDB;

-- 회복안별 실행 상태. 사후점검 화면 "회복안 실행 상태"
CREATE TABLE recovery_execution_status (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    business_id        BIGINT      NOT NULL,
    recovery_option_id BIGINT      NOT NULL,
    forecast_run_id    BIGINT,
    status             VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    blocker_text       VARCHAR(200),
    updated_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_recovery_execution_status_business (business_id),
    CONSTRAINT fk_recovery_execution_status_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_recovery_execution_status_option FOREIGN KEY (recovery_option_id) REFERENCES recovery_options (id),
    CONSTRAINT fk_recovery_execution_status_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id) ON DELETE SET NULL,
    CONSTRAINT chk_recovery_execution_status_status
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'DONE', 'BLOCKED'))
) ENGINE = InnoDB;
