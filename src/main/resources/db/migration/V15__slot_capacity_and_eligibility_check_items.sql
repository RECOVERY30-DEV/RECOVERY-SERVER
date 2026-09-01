-- 피그마 전수 대조 후속 (docs/db-design.md 3장)
--  갭 ① 상담 예약 "잔여 N석"      → recovery_counselor_slots 를 다인 정원 모델로 확장
--  갭 ② 지원사업 상세 자격요건 "항목별" 판정 → 규칙 단위 결과 자식 테이블 추가

-- ── 갭 ① recovery_counselor_slots : 정원(capacity) / 예약수(booked_count) ──
--  잔여석 = capacity - booked_count
--  예약 가능 조건 = status <> 'BLOCKED' AND booked_count < capacity
--  기존 status('OPEN'/'BOOKED'/'BLOCKED')는 그대로 유지한다.
--   - 'BLOCKED' : 운영자가 수동으로 막은 슬롯
--   - 'BOOKED'  : 정원과 무관하게 강제 마감하고 싶을 때 사용 (없어도 booked_count 로 판단 가능)
ALTER TABLE recovery_counselor_slots
    ADD COLUMN capacity     INT NOT NULL DEFAULT 1 AFTER end_at,
    ADD COLUMN booked_count INT NOT NULL DEFAULT 0 AFTER capacity,
    ADD CONSTRAINT chk_recovery_counselor_slots_capacity
        CHECK (capacity >= 1),
    ADD CONSTRAINT chk_recovery_counselor_slots_booked
        CHECK (booked_count >= 0 AND booked_count <= capacity);

-- ── 갭 ② recovery_program_eligibility_check_items : 규칙별 판정 결과 ──
--  recovery_program_eligibility_checks 는 (business, program) 1회 평가 = 1행 (전체 롤업).
--  지원사업 상세 화면은 규칙 4개를 각각 체크박스 + 개별 문구
--  ("등록일 기준 충족 가능성 높음", "확인 필요 - 상담자가 최종 판단합니다")로 표시하므로
--  규칙 단위 결과가 별도로 필요하다.
CREATE TABLE recovery_program_eligibility_check_items (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    check_id    BIGINT      NOT NULL,
    rule_id     BIGINT      NOT NULL,
    result      VARCHAR(20) NOT NULL,
    note_text   TEXT,
    is_advisory BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_program_eligibility_check_items_check_rule (check_id, rule_id),
    KEY idx_program_eligibility_check_items_check (check_id),
    CONSTRAINT fk_program_eligibility_check_items_check
        FOREIGN KEY (check_id) REFERENCES recovery_program_eligibility_checks (id) ON DELETE CASCADE,
    CONSTRAINT fk_program_eligibility_check_items_rule
        FOREIGN KEY (rule_id) REFERENCES recovery_program_eligibility_rules (id) ON DELETE CASCADE,
    CONSTRAINT chk_program_eligibility_check_items_result
        CHECK (result IN ('LIKELY_PASS', 'NEEDS_REVIEW', 'LIKELY_FAIL', 'UNKNOWN')),
    -- 자동 자격판정 아님: 규칙별 결과도 항상 참고용(advisory)이어야 한다
    CONSTRAINT chk_program_eligibility_check_items_advisory CHECK (is_advisory = TRUE)
) ENGINE = InnoDB;
