-- 레퍼런스(카탈로그) 데이터. 모든 환경(로컬·QA·운영)에 공통 적재된다.
-- 데모 페르소나(사업자별 거래/예측 데이터)는 여기 넣지 않는다 → @Profile("demo") 시더 담당.

-- ── 버전 마스터 ───────────────────────────────────────────────
INSERT INTO audit_model_versions (version, description, released_at, is_active) VALUES
  ('fc-model-v0.3', 'MVP 규칙기반 현금흐름 예측 (엔진 대체)', '2025-07-01', TRUE);

INSERT INTO audit_ruleset_versions (version, domain, description, released_at, is_active) VALUES
  ('rule-2025-06', 'ELIGIBILITY', '2025년 상반기 소상공인 지원제도 자격판정 규칙', '2025-06-01', TRUE),
  ('fcrule-2025-06', 'FORECAST', '30일 현금흐름 예측 파라미터셋', '2025-06-01', TRUE);

-- ── 동의 항목 마스터 ─────────────────────────────────────────
INSERT INTO core_consent_types (code, name, is_required, purpose, data_scope, withdraw_effect, version)
VALUES
  ('ANALYSIS', '서비스 분석 동의', TRUE,
   '30일 현금흐름 예측 및 부족 원인 분석에 사업자 거래 데이터를 활용합니다.',
   '사업자 거래 내역, 보정값, 예측 결과',
   '철회 시 30일 현금흐름 분석을 포함한 모든 서비스 이용이 중단됩니다.', 'v1.0'),
  ('PACKET_TRANSFER', '상담원 전송 동의', FALSE,
   '상담 예약 시 Recovery Packet을 상담원에게 사전 전송합니다.',
   'Recovery Packet (위험 Snapshot, 원인, 선택안, 질문, 준비서류)',
   '철회해도 상담 예약은 유지되나 Packet이 전송되지 않습니다.', 'v1.0'),
  ('FOLLOWUP_TRACKING', '30·60·90일 사후 점검 동의', FALSE,
   '실행 결과와 잔액 회복 여부를 확인해 추천을 개선합니다.',
   '실행 결과, 잔액 회복 여부, 연체 발생 여부',
   '미동의 시 추적 알림을 받지 않으며 분석 이용에는 영향을 주지 않습니다.', 'v1.0');

-- ── 상담사 ───────────────────────────────────────────────────
INSERT INTO recovery_counselors (name, institution, branch, role) VALUES
  ('김상담', '소상공인시장진흥공단', '서울중부센터', '경영지도사'),
  ('이회복', 'IBK기업은행', '을지로점', '여신상담역');

-- ── 지원제도 3건 ─────────────────────────────────────────────
INSERT INTO recovery_support_programs
  (program_code, name, agency, support_content, limit_amount, interest_rate_text, term_text,
   apply_deadline, apply_url, official_source_url, ruleset_version, status)
VALUES
  ('SBIZ_STABLE_FUND', '소상공인 경영안정자금', '소상공인시장진흥공단',
   '운전자금 최대 2,000만 원 융자', 20000000, '연 3.4% (고정, 상담자 확인 필요)', '3년 거치 5년 분할상환',
   '2025-07-31', 'https://www.sbiz.or.kr', 'https://www.sbiz.or.kr/notice/2025-1',
   'rule-2025-06', 'ACTIVE'),
  ('SBIZ_119PLUS', '소상공인 119Plus', '소상공인시장진흥공단',
   '경영애로 소상공인 저금리 대환 및 상환유예', 30000000, '연 4.5% 내외 (심사 결과에 따름)', '최대 5년 분할상환',
   '2025-08-29', 'https://www.sbiz.or.kr', 'https://www.sbiz.or.kr/notice/119plus',
   'rule-2025-06', 'ACTIVE'),
  ('SUNSHINE_119', '햇살론 119', '서민금융진흥원',
   '연체 우려 차주 대상 긴급 생계·사업자금', 20000000, '연 6.9% 고정', '최대 5년',
   '2025-12-31', 'https://www.kinfa.or.kr', 'https://www.kinfa.or.kr/sunshine119',
   'rule-2025-06', 'ACTIVE');

-- ── 자격요건 규칙 (소상공인 경영안정자금) ─────────────────────
-- rule_expression은 규칙엔진 입력용 JSON. MVP에서는 목 형태만 채운다.
INSERT INTO recovery_program_eligibility_rules (program_id, rule_code, label, rule_expression, evaluation_type)
SELECT id, 'BIZ_AGE_1Y', '사업자등록 1년 이상',
       JSON_OBJECT('field', 'openedAt', 'op', 'olderThan', 'value', '1Y'), 'AUTO'
FROM recovery_support_programs WHERE program_code = 'SBIZ_STABLE_FUND';

INSERT INTO recovery_program_eligibility_rules (program_id, rule_code, label, rule_expression, evaluation_type)
SELECT id, 'REVENUE_1B', '연매출 10억 원 이하',
       JSON_OBJECT('field', 'annualRevenue', 'op', '<=', 'value', 1000000000), 'AUTO'
FROM recovery_support_programs WHERE program_code = 'SBIZ_STABLE_FUND';

INSERT INTO recovery_program_eligibility_rules (program_id, rule_code, label, rule_expression, evaluation_type)
SELECT id, 'NO_DELINQUENCY', '금융기관 연체 없음',
       JSON_OBJECT('field', 'delinquency', 'op', '=', 'value', FALSE), 'COUNSELOR_ONLY'
FROM recovery_support_programs WHERE program_code = 'SBIZ_STABLE_FUND';

INSERT INTO recovery_program_eligibility_rules (program_id, rule_code, label, rule_expression, evaluation_type)
SELECT id, 'INDUSTRY_ALLOWED', '제한 업종 미해당',
       JSON_OBJECT('field', 'industryCode', 'op', 'notIn', 'value', JSON_ARRAY('P91', 'I562')), 'AUTO'
FROM recovery_support_programs WHERE program_code = 'SBIZ_STABLE_FUND';

-- ── 필요서류 (소상공인 경영안정자금) ─────────────────────────
INSERT INTO recovery_program_documents (program_id, name, description, is_required)
SELECT id, '사업자등록증', '최근 발급본', TRUE
FROM recovery_support_programs WHERE program_code = 'SBIZ_STABLE_FUND';

INSERT INTO recovery_program_documents (program_id, name, description, is_required)
SELECT id, '최근 3개월 거래내역', '주거래 사업용 계좌 기준', TRUE
FROM recovery_support_programs WHERE program_code = 'SBIZ_STABLE_FUND';

INSERT INTO recovery_program_documents (program_id, name, description, is_required)
SELECT id, '임대차계약서', '사업장 임차 시', FALSE
FROM recovery_support_programs WHERE program_code = 'SBIZ_STABLE_FUND';

-- ── 공휴일 (2025) : 원리금·자동이체 납부일 이동 계산용 ────────
INSERT INTO audit_holidays (holiday_date, name, is_substitute) VALUES
  ('2025-01-01', '신정', FALSE),
  ('2025-03-01', '삼일절', FALSE),
  ('2025-03-03', '삼일절 대체공휴일', TRUE),
  ('2025-05-05', '어린이날·부처님오신날', FALSE),
  ('2025-05-06', '대체공휴일', TRUE),
  ('2025-06-06', '현충일', FALSE),
  ('2025-08-15', '광복절', FALSE),
  ('2025-10-03', '개천절', FALSE),
  ('2025-10-06', '추석 연휴', FALSE),
  ('2025-10-07', '추석', FALSE),
  ('2025-10-08', '추석 연휴', FALSE),
  ('2025-10-09', '한글날', FALSE),
  ('2025-12-25', '성탄절', FALSE);
