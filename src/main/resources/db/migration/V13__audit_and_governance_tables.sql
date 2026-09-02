-- 거버넌스 / 감사 / 재현성 (기획 심사 어필용). 대부분 화면에 직접 노출되지 않음

-- 전역 감사 로그. actor_type 에 AI 포함 — 누가/무슨 목적으로 데이터를 다뤘는지 추적
CREATE TABLE audit_logs (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    actor_type  VARCHAR(20) NOT NULL,
    actor_id    BIGINT,
    action      VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id   BIGINT,
    purpose     VARCHAR(200),
    consent_id  BIGINT,
    ip_address  VARCHAR(45),
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_audit_logs_target (target_type, target_id),
    KEY idx_audit_logs_created (created_at),
    CONSTRAINT chk_audit_logs_actor_type CHECK (actor_type IN ('USER', 'COUNSELOR', 'SYSTEM', 'AI'))
) ENGINE = InnoDB;

-- 동의/철회 append-only 이력. core_consents 는 최신 상태 1건만, 변경 이력은 여기에 쌓는다
CREATE TABLE audit_consent_logs (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    business_id       BIGINT      NOT NULL,
    consent_type_code VARCHAR(40) NOT NULL,
    from_status       VARCHAR(20),
    to_status         VARCHAR(20) NOT NULL,
    consent_version   VARCHAR(20) NOT NULL,
    ip_address        VARCHAR(45),
    user_agent        VARCHAR(500),
    changed_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_audit_consent_logs_business_type (business_id, consent_type_code),
    CONSTRAINT chk_audit_consent_logs_to_status CHECK (to_status IN ('GRANTED', 'WITHDRAWN'))
) ENGINE = InnoDB;

-- AI 호출 추적 (RAG 근거·검토 여부 포함)
CREATE TABLE audit_ai_generations (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    feature        VARCHAR(50) NOT NULL,
    model          VARCHAR(50),
    model_version  VARCHAR(20),
    prompt_ref     VARCHAR(100),
    retrieved_docs JSON,
    output_summary TEXT,
    human_reviewed BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_audit_ai_generations_feature (feature)
) ENGINE = InnoDB;

-- 예측 모델 버전 마스터 (재현성)
CREATE TABLE audit_model_versions (
    version     VARCHAR(20)  NOT NULL,
    description VARCHAR(300),
    released_at DATE,
    is_active   BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (version)
) ENGINE = InnoDB;

-- 규칙셋 버전 마스터 (자격판정/예측 규칙). support_programs.ruleset_version 등이 참조하는 논리 버전
CREATE TABLE audit_ruleset_versions (
    version     VARCHAR(20)  NOT NULL,
    domain      VARCHAR(20)  NOT NULL,
    description VARCHAR(300),
    released_at DATE,
    is_active   BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (version),
    CONSTRAINT chk_audit_ruleset_versions_domain CHECK (domain IN ('ELIGIBILITY', 'FORECAST'))
) ENGINE = InnoDB;

-- 공휴일 마스터. 원리금/자동이체 납부일 이동 계산 (Dashboard·안정 상태 "공휴일 납부일 이동")
-- 목데이터가 아니라 실제 한국 공휴일을 시드한다
CREATE TABLE audit_holidays (
    holiday_date  DATE         NOT NULL,
    name          VARCHAR(100) NOT NULL,
    is_substitute BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (holiday_date)
) ENGINE = InnoDB;

-- 알림 발송·클릭·행동완료 (KPI: 회복 행동 완료율)
CREATE TABLE audit_notifications (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    business_id         BIGINT      NOT NULL,
    type                VARCHAR(40) NOT NULL,
    title               VARCHAR(200),
    body                TEXT,
    channel             VARCHAR(20),
    sent_at             DATETIME(6),
    read_at             DATETIME(6),
    action_completed_at DATETIME(6),
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_audit_notifications_business (business_id),
    CONSTRAINT fk_audit_notifications_business FOREIGN KEY (business_id) REFERENCES core_businesses (id)
) ENGINE = InnoDB;
