CREATE TABLE recovery_counselors (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    institution VARCHAR(100),
    branch      VARCHAR(100),
    role        VARCHAR(50),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE recovery_consultations (
    id                        BIGINT      NOT NULL AUTO_INCREMENT,
    business_id               BIGINT      NOT NULL,
    packet_id                 BIGINT,
    counselor_id              BIGINT,
    channel                   VARCHAR(20) NOT NULL,
    scheduled_at              DATETIME(6) NOT NULL,
    purpose_text              TEXT,
    pre_question              TEXT,
    transfer_consent_granted  BOOLEAN     NOT NULL DEFAULT FALSE,
    status                    VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    final_decision            VARCHAR(30),
    decided_by                BIGINT,
    decided_at                DATETIME(6),
    result_note               TEXT,
    PRIMARY KEY (id),
    KEY idx_recovery_consultations_business (business_id),
    CONSTRAINT fk_recovery_consultations_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_recovery_consultations_packet FOREIGN KEY (packet_id) REFERENCES recovery_packets (id),
    CONSTRAINT fk_recovery_consultations_counselor FOREIGN KEY (counselor_id) REFERENCES recovery_counselors (id),
    CONSTRAINT fk_recovery_consultations_decided_by FOREIGN KEY (decided_by) REFERENCES recovery_counselors (id),
    CONSTRAINT chk_recovery_consultations_channel CHECK (channel IN ('PHONE', 'VISIT', 'VIDEO', 'CHAT')),
    CONSTRAINT chk_recovery_consultations_status
        CHECK (status IN ('REQUESTED', 'CONFIRMED', 'COMPLETED', 'CANCELED')),
    -- AI/시스템 단독 결정 차단: 최종 판단은 상담자가 있을 때만 기록 가능
    CONSTRAINT chk_human_decision CHECK (final_decision IS NULL OR decided_by IS NOT NULL)
) ENGINE = InnoDB;
