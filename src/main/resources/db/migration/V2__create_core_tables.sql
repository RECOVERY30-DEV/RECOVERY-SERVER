CREATE TABLE core_users (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(50),
    phone         VARCHAR(255),
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME(6),
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_core_users_email (email),
    CONSTRAINT chk_core_users_status CHECK (status IN ('ACTIVE', 'DORMANT', 'WITHDRAWN'))
) ENGINE = InnoDB;

CREATE TABLE core_businesses (
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    user_id              BIGINT      NOT NULL,
    biz_reg_no           VARCHAR(255),
    biz_name             VARCHAR(100) NOT NULL,
    industry_code        VARCHAR(10),
    opened_at            DATE,
    region_code          VARCHAR(10),
    annual_revenue       BIGINT,
    employee_count       INT,
    safety_buffer_amount BIGINT      NOT NULL DEFAULT 1000000,
    created_at           DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_core_businesses_user (user_id),
    CONSTRAINT fk_core_businesses_user FOREIGN KEY (user_id) REFERENCES core_users (id)
) ENGINE = InnoDB;

CREATE TABLE core_consent_types (
    code            VARCHAR(40)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    is_required     BOOLEAN      NOT NULL DEFAULT FALSE,
    purpose         TEXT,
    data_scope      TEXT,
    withdraw_effect TEXT,
    version         VARCHAR(20)  NOT NULL,
    PRIMARY KEY (code)
) ENGINE = InnoDB;

CREATE TABLE core_consents (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    business_id       BIGINT      NOT NULL,
    consent_type_code VARCHAR(40) NOT NULL,
    consent_version   VARCHAR(20) NOT NULL,
    status            VARCHAR(20) NOT NULL,
    granted_at        DATETIME(6),
    withdrawn_at      DATETIME(6),
    ip_address        VARCHAR(45),
    user_agent        VARCHAR(500),
    PRIMARY KEY (id),
    UNIQUE KEY uk_core_consents_business_type (business_id, consent_type_code),
    CONSTRAINT fk_core_consents_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_core_consents_type FOREIGN KEY (consent_type_code) REFERENCES core_consent_types (code),
    CONSTRAINT chk_core_consents_status CHECK (status IN ('GRANTED', 'WITHDRAWN'))
) ENGINE = InnoDB;
