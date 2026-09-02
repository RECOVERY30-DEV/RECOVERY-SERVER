CREATE TABLE recovery_packets (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    business_id           BIGINT      NOT NULL,
    forecast_run_id       BIGINT      NOT NULL,
    version               INT         NOT NULL,
    supersedes_packet_id  BIGINT,
    snapshot_json         JSON        NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    generated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    customer_confirmed_at DATETIME(6),
    sent_at               DATETIME(6),
    pdf_url               TEXT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_packets_business_run_version (business_id, forecast_run_id, version),
    CONSTRAINT fk_recovery_packets_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_recovery_packets_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs (id),
    CONSTRAINT fk_recovery_packets_supersedes FOREIGN KEY (supersedes_packet_id) REFERENCES recovery_packets (id),
    CONSTRAINT chk_recovery_packets_status CHECK (status IN ('DRAFT', 'CONFIRMED', 'SENT'))
) ENGINE = InnoDB;
