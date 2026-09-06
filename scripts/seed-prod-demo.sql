-- ─────────────────────────────────────────────────────────────────────────────
-- 오늘 만든 API(원인상세·회복안비교·지원사업·Packet·상담예약)를 운영에서 바로 쓸 수 있게
-- per-run / per-business 데모 데이터를 채운다.
--
-- 전제  : scripts/seed-prod-home.sql 이 먼저 실행되어 business_id=1, forecast_run_id=1 이 있어야 한다.
--         카탈로그(recovery_options=V16, support_programs/rules/documents/counselors=V14)는 이미 적재됨.
-- 실행  : mysql -h <host> -u <user> -p recovery30 < scripts/seed-prod-demo.sql
-- 재실행: 모든 INSERT 에 WHERE NOT EXISTS 가드 (idempotent)
-- ─────────────────────────────────────────────────────────────────────────────

SET NAMES utf8mb4;
START TRANSACTION;

SET @biz = (SELECT id FROM core_businesses WHERE biz_reg_no = 'DEMO-HOME-RISK');
SET @run = (SELECT id FROM forecast_runs WHERE business_id = @biz ORDER BY id DESC LIMIT 1);
SET @opt_repay  = (SELECT id FROM recovery_options WHERE option_code = 'REPAYMENT_ADJUST');
SET @opt_shift  = (SELECT id FROM recovery_options WHERE option_code = 'DUEDATE_SHIFT');
SET @prog_sbiz  = (SELECT id FROM recovery_support_programs WHERE program_code = 'SBIZ_STABLE_FUND');
SET @prog_119   = (SELECT id FROM recovery_support_programs WHERE program_code = 'SBIZ_119PLUS');
SET @prog_sun   = (SELECT id FROM recovery_support_programs WHERE program_code = 'SUNSHINE_119');


-- 1) 회복안 비교 : BASELINE + SIMULATED 2 ------------------------------------
INSERT INTO recovery_scenarios
  (forecast_run_id, scenario_type, first_shortfall_date, min_balance, delta_days, delta_min_balance, monthly_payment_delta, note)
SELECT @run, 'BASELINE', '2025-07-26', -1240000, NULL, NULL, NULL, '현재 데이터 기반 기준 시나리오입니다.'
WHERE NOT EXISTS (SELECT 1 FROM recovery_scenarios WHERE forecast_run_id = @run AND scenario_type = 'BASELINE');

INSERT INTO recovery_scenarios
  (forecast_run_id, scenario_type, first_shortfall_date, min_balance, delta_days, delta_min_balance, monthly_payment_delta, note)
SELECT @run, 'SIMULATED', '2025-08-11', -630000, 16, 610000, -150000, '상담 및 심사 결과에 따라 실제 효과는 달라질 수 있습니다.'
WHERE NOT EXISTS (
  SELECT 1 FROM recovery_scenarios WHERE forecast_run_id = @run AND scenario_type = 'SIMULATED' AND delta_days = 16
);

INSERT INTO recovery_scenarios
  (forecast_run_id, scenario_type, first_shortfall_date, min_balance, delta_days, delta_min_balance, monthly_payment_delta, note)
SELECT @run, 'SIMULATED', '2025-08-04', -860000, 9, 380000, 0, '납부일 변경은 기관 협의 후 실제 반영됩니다.'
WHERE NOT EXISTS (
  SELECT 1 FROM recovery_scenarios WHERE forecast_run_id = @run AND scenario_type = 'SIMULATED' AND delta_days = 9
);

SET @sc_repay = (SELECT id FROM recovery_scenarios WHERE forecast_run_id = @run AND scenario_type = 'SIMULATED' AND delta_days = 16 LIMIT 1);
SET @sc_shift = (SELECT id FROM recovery_scenarios WHERE forecast_run_id = @run AND scenario_type = 'SIMULATED' AND delta_days = 9 LIMIT 1);

INSERT INTO recovery_scenario_options (scenario_id, recovery_option_id)
SELECT @sc_repay, @opt_repay
WHERE NOT EXISTS (SELECT 1 FROM recovery_scenario_options WHERE scenario_id = @sc_repay AND recovery_option_id = @opt_repay);

INSERT INTO recovery_scenario_options (scenario_id, recovery_option_id)
SELECT @sc_shift, @opt_shift
WHERE NOT EXISTS (SELECT 1 FROM recovery_scenario_options WHERE scenario_id = @sc_shift AND recovery_option_id = @opt_shift);


-- 2) 지원제도 추천 (run 1) -------------------------------------------------
INSERT INTO recovery_program_recommendations (forecast_run_id, program_id, rank_no, match_reason, created_at)
SELECT @run, @prog_sbiz, 1, '최근 6개월 매출 감소·사업자 2년 이상·신용등급 조건 확인 필요', '2025-07-15 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM recovery_program_recommendations WHERE forecast_run_id = @run AND program_id = @prog_sbiz);

INSERT INTO recovery_program_recommendations (forecast_run_id, program_id, rank_no, match_reason, created_at)
SELECT @run, @prog_119, 2, '경영애로 소상공인 저금리 대환 대상 가능 · 현금흐름 데이터 반영', '2025-07-15 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM recovery_program_recommendations WHERE forecast_run_id = @run AND program_id = @prog_119);

INSERT INTO recovery_program_recommendations (forecast_run_id, program_id, rank_no, match_reason, created_at)
SELECT @run, @prog_sun, 3, '연체 우려 차주 대상 · 지역·소득 요건 별도 확인 필요', '2025-07-15 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM recovery_program_recommendations WHERE forecast_run_id = @run AND program_id = @prog_sun);


-- 3) 지원제도 자격 판정 (business 1 × SBIZ_STABLE_FUND) --------------------
INSERT INTO recovery_program_eligibility_checks
  (business_id, program_id, forecast_run_id, result, reason_text, is_advisory, ruleset_version, checked_at)
SELECT @biz, @prog_sbiz, @run, 'NEEDS_REVIEW',
       '최근 8주 매출 감소 패턴이 지원 대상 조건과 유사하나, 금융기관 연체 여부는 상담자 확인이 필요합니다.',
       TRUE, 'rule-2025-06', '2025-07-15 00:00:00'
WHERE NOT EXISTS (
  SELECT 1 FROM recovery_program_eligibility_checks WHERE business_id = @biz AND program_id = @prog_sbiz
);

SET @chk = (SELECT id FROM recovery_program_eligibility_checks WHERE business_id = @biz AND program_id = @prog_sbiz LIMIT 1);

INSERT INTO recovery_program_eligibility_check_items (check_id, rule_id, result, note_text, is_advisory)
SELECT @chk, r.id,
       CASE r.rule_code WHEN 'NO_DELINQUENCY' THEN 'NEEDS_REVIEW' ELSE 'LIKELY_PASS' END,
       CASE r.rule_code
         WHEN 'BIZ_AGE_1Y'       THEN '등록일 기준 충족 가능성 높음'
         WHEN 'REVENUE_1B'       THEN '최근 매출 데이터 기준 해당 가능'
         WHEN 'NO_DELINQUENCY'   THEN '확인 필요 - 상담자가 최종 판단합니다'
         WHEN 'INDUSTRY_ALLOWED' THEN '현재 업종 코드 기준 해당 없음'
       END,
       TRUE
FROM recovery_program_eligibility_rules r
WHERE r.program_id = @prog_sbiz
  AND NOT EXISTS (
    SELECT 1 FROM recovery_program_eligibility_check_items i WHERE i.check_id = @chk AND i.rule_id = r.id
  );


-- 4) Recovery Packet v1 (DRAFT) ----------------------------------------------
INSERT INTO recovery_packets
  (business_id, forecast_run_id, version, supersedes_packet_id, snapshot_json, status, generated_at)
SELECT @biz, @run, 1, NULL,
  JSON_OBJECT(
    'riskSnapshot', JSON_OBJECT('firstShortfallDate','2025-07-26','minBalanceRange','-180만 원 ~ -240만 원','status','위험 — 부족 가능성 높음'),
    'adjustments', JSON_ARRAY(
      JSON_OBJECT('label','현금매출 추가 입력','amountText','+65만 원 / 7월 20일'),
      JSON_OBJECT('label','예정 지출 (인테리어 대금)','amountText','-120만 원 / 7월 22일')),
    'causes', JSON_ARRAY(
      JSON_OBJECT('rank',1,'title','월말 임차료·원리금 집중','contributionText','-210만 원 기여'),
      JSON_OBJECT('rank',2,'title','최근 8주 매출 감소','contributionText','-95만 원 기여'),
      JSON_OBJECT('rank',3,'title','계절적 매출 회복 지연','contributionText','확인 필요')),
    'selectedOptions', JSON_ARRAY(
      JSON_OBJECT('title','상환조건 조정 상담','expectedEffect','월 부담 -40만 원 추정','preparation','사업자등록증, 최근 3개월 거래내역','nextAction','상담 예약 완료 — 7월 16일 14:00'),
      JSON_OBJECT('title','고정비 납부일 재배치','expectedEffect','부족일 +7일 개선 추정','preparation','임차계약서 확인, 임대인 협의','nextAction','직접 실행 저장됨')),
    'analysisBasis', '사업자계좌·카드정산·자동이체 포함. 현금거래·타행자금은 보정값 반영분만 포함됩니다.'
  ),
  'DRAFT', '2025-07-14 00:32:00'
WHERE NOT EXISTS (SELECT 1 FROM recovery_packets WHERE business_id = @biz AND forecast_run_id = @run);


-- 5) 상담 슬롯 (V14 상담자 2명 × 3슬롯, 잔여 2 / 3 / 1) ----------------------
SET @c1 = (SELECT id FROM recovery_counselors ORDER BY id LIMIT 1);
SET @c2 = (SELECT id FROM recovery_counselors ORDER BY id LIMIT 1 OFFSET 1);

INSERT INTO recovery_counselor_slots (counselor_id, start_at, end_at, status, capacity, booked_count)
SELECT c.id, s.start_at, s.end_at, 'OPEN', 3, s.booked
FROM (SELECT @c1 AS id UNION ALL SELECT @c2) c
JOIN (
  SELECT '2025-07-14 01:00:00' AS start_at, '2025-07-14 01:30:00' AS end_at, 1 AS booked
  UNION ALL SELECT '2025-07-14 05:00:00', '2025-07-14 05:30:00', 0
  UNION ALL SELECT '2025-07-15 02:00:00', '2025-07-15 02:30:00', 2
) s
WHERE c.id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM recovery_counselor_slots x WHERE x.counselor_id = c.id AND x.start_at = s.start_at
  );

-- 6) 지원제도 마감일을 미래로 (V14 시드가 2025년이라 applicableOnly 필터에 다 걸러짐) --
UPDATE recovery_support_programs
SET apply_deadline = DATE_ADD(CURRENT_DATE, INTERVAL 60 DAY)
WHERE program_code = 'SBIZ_STABLE_FUND' AND apply_deadline < CURRENT_DATE;
UPDATE recovery_support_programs
SET apply_deadline = DATE_ADD(CURRENT_DATE, INTERVAL 90 DAY)
WHERE program_code = 'SBIZ_119PLUS' AND apply_deadline < CURRENT_DATE;
UPDATE recovery_support_programs
SET apply_deadline = DATE_ADD(CURRENT_DATE, INTERVAL 120 DAY)
WHERE program_code = 'SUNSHINE_119' AND apply_deadline < CURRENT_DATE;

COMMIT;

-- 확인용 출력
SELECT @biz AS business_id, @run AS forecast_run_id,
       (SELECT id FROM recovery_packets WHERE business_id = @biz AND forecast_run_id = @run LIMIT 1) AS packet_id,
       @c1 AS counselor_id_1, @c2 AS counselor_id_2;
