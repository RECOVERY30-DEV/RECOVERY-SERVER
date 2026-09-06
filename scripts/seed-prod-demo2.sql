-- ─────────────────────────────────────────────────────────────────────────────
-- seed-prod-home.sql / seed-prod-demo.sql 이후에 추가된 API 를 운영에서 바로 검증할 수 있게
-- per-business / per-run 데모 데이터를 채운다.
--
--   대상 화면 : 분리 동의 · 데이터 범위 확인 · 정보 보정 · Dashboard(일자별) · 안정/판단보류(서술)
--               · 셀프 액션 저장 · 사후점검
--
--   전제  : scripts/seed-prod-home.sql + scripts/seed-prod-demo.sql 이 먼저 실행되어
--           business_id (DEMO-HOME-RISK) 와 forecast_run 1건이 있어야 한다.
--           카탈로그(recovery_options=V16, consent_types/programs/counselors=V14)는 이미 적재됨.
--   실행  : mysql -h <host> -u <user> -p recovery30 < scripts/seed-prod-demo2.sql
--   재실행: 모든 INSERT 에 WHERE NOT EXISTS 가드 (idempotent)
--   되돌리기: 맨 아래 블록 참고
--
--   값은 demo 프로파일 시더(DemoSourceSeeder / DemoAdjustmentSeeder / DemoForecastSeeder
--   / DemoRecoveryOptionSeeder / DemoFollowupSeeder)의 QA-RISK 와 동일 (CHECK 제약 통과 검증됨).
-- ─────────────────────────────────────────────────────────────────────────────

SET NAMES utf8mb4;
START TRANSACTION;

SET @biz = (SELECT id FROM core_businesses WHERE biz_reg_no = 'DEMO-HOME-RISK');
SET @run = (SELECT id FROM forecast_runs WHERE business_id = @biz ORDER BY id DESC LIMIT 1);
SET @opt_repay = (SELECT id FROM recovery_options WHERE option_code = 'REPAYMENT_ADJUST');
SET @opt_shift = (SELECT id FROM recovery_options WHERE option_code = 'DUEDATE_SHIFT');


-- 1) 사후 점검 추적 동의 (FOLLOWUP_TRACKING) --------------------------------
--    ANALYSIS 동의는 seed-prod-home.sql 이 이미 넣었고, 사후점검 일정은 추적 동의가 있어야 만들 수 있다.
INSERT INTO core_consents
  (business_id, consent_type_code, consent_version, status, granted_at, ip_address, user_agent)
SELECT @biz, 'FOLLOWUP_TRACKING', 'v1.0', 'GRANTED', '2025-07-14 23:32:00', '127.0.0.1', 'seed-prod-demo2.sql'
WHERE NOT EXISTS (
  SELECT 1 FROM core_consents WHERE business_id = @biz AND consent_type_code = 'FOLLOWUP_TRACKING'
);

SET @consent_track = (
  SELECT id FROM core_consents WHERE business_id = @biz AND consent_type_code = 'FOLLOWUP_TRACKING'
);


-- 2) 데이터 범위 확인 : 연동 소스 4건 (커버리지는 forecast_coverage 와 동일) -----------
INSERT INTO source_data_sources
  (business_id, source_type, institution_name, coverage_rate, period_months, last_synced_at, sync_status)
SELECT @biz, 'BANK_ACCOUNT', 'KB국민은행 · 신한은행', 95.00, 6, '2025-07-14 21:14:00', 'SYNCED'
WHERE NOT EXISTS (SELECT 1 FROM source_data_sources WHERE business_id = @biz AND source_type = 'BANK_ACCOUNT');

INSERT INTO source_data_sources
  (business_id, source_type, institution_name, coverage_rate, period_months, last_synced_at, sync_status)
SELECT @biz, 'CARD_SETTLEMENT', 'BC카드 · KB카드 가맹점 정산', 92.00, 3, '2025-07-13 14:42:00', 'SYNCED'
WHERE NOT EXISTS (SELECT 1 FROM source_data_sources WHERE business_id = @biz AND source_type = 'CARD_SETTLEMENT');

INSERT INTO source_data_sources
  (business_id, source_type, institution_name, coverage_rate, period_months, last_synced_at, sync_status)
SELECT @biz, 'LOAN', 'IBK기업은행 사업자대출 약정', 88.00, 12, '2025-07-14 21:00:00', 'SYNCED'
WHERE NOT EXISTS (SELECT 1 FROM source_data_sources WHERE business_id = @biz AND source_type = 'LOAN');

INSERT INTO source_data_sources
  (business_id, source_type, institution_name, coverage_rate, period_months, last_synced_at, sync_status)
SELECT @biz, 'AUTO_TRANSFER', '공과금 · 구독 · 보험료 등', 61.00, 1, '2025-07-11 00:00:00', 'PARTIAL'
WHERE NOT EXISTS (SELECT 1 FROM source_data_sources WHERE business_id = @biz AND source_type = 'AUTO_TRANSFER');


-- 3) 정보 보정 : 저장된 보정값 2건(SAVED) + 반복 패턴 추정 후보 2건(PROPOSED) ----------
INSERT INTO source_adjustments
  (business_id, adjustment_type, direction, amount, expected_date, certainty, memo, status, applied_run_id, created_at, updated_at)
SELECT @biz, 'CASH_SALES', 'I', 650000, '2025-07-20', 'ESTIMATED', '매주 토요일 현금 매출', 'SAVED', @run,
       '2025-07-14 00:00:00', '2025-07-14 00:00:00'
WHERE NOT EXISTS (
  SELECT 1 FROM source_adjustments WHERE business_id = @biz AND adjustment_type = 'CASH_SALES' AND expected_date = '2025-07-20'
);

INSERT INTO source_adjustments
  (business_id, adjustment_type, direction, amount, expected_date, certainty, memo, status, applied_run_id, created_at, updated_at)
SELECT @biz, 'EXPECTED_EXPENSE', 'O', 1200000, '2025-07-22', 'CONFIRMED', '인테리어 대금', 'SAVED', @run,
       '2025-07-14 00:00:00', '2025-07-14 00:00:00'
WHERE NOT EXISTS (
  SELECT 1 FROM source_adjustments WHERE business_id = @biz AND adjustment_type = 'EXPECTED_EXPENSE' AND expected_date = '2025-07-22'
);

INSERT INTO source_adjustment_suggestions
  (business_id, adjustment_type, suggested_amount, suggested_rule, evidence_text, confidence, status, created_at)
SELECT @biz, 'CASH_SALES', 1200000, '매월 15일', '최근 3개월 동일 패턴', 0.82, 'PROPOSED', '2025-07-14 00:00:00'
WHERE NOT EXISTS (
  SELECT 1 FROM source_adjustment_suggestions WHERE business_id = @biz AND adjustment_type = 'CASH_SALES' AND suggested_rule = '매월 15일'
);

INSERT INTO source_adjustment_suggestions
  (business_id, adjustment_type, suggested_amount, suggested_rule, evidence_text, confidence, status, created_at)
SELECT @biz, 'EXTERNAL_FUND', 850000, '매월 말일', '최근 2개월 유사 패턴', 0.70, 'PROPOSED', '2025-07-14 00:00:00'
WHERE NOT EXISTS (
  SELECT 1 FROM source_adjustment_suggestions WHERE business_id = @biz AND adjustment_type = 'EXTERNAL_FUND' AND suggested_rule = '매월 말일'
);


-- 4) Dashboard / 안정 상태 : 30일 일자별 캘린더 (running balance, 하루 -18만 원 추세) ----
--    이미 한 행이라도 있으면 통째로 건너뛴다.
INSERT INTO forecast_daily
  (forecast_run_id, target_date, d_day, opening_balance,
   confirmed_inflow, confirmed_outflow, expected_inflow_min, expected_inflow_max,
   expected_outflow_min, expected_outflow_max, adjustment_net,
   closing_balance_conservative, closing_balance_expected, closing_balance_optimistic,
   is_shortfall, is_holiday, holiday_shift_note)
WITH RECURSIVE d (i, opening) AS (
  SELECT 0, CAST(2000000 AS SIGNED)
  UNION ALL
  SELECT i + 1,
         opening
           + CASE WHEN i = 5 THEN 680000 ELSE 0 END
           - CASE WHEN i = 7 THEN 950000 WHEN i = 10 THEN 380000 ELSE 0 END
           + CASE WHEN i = 5 THEN 50000 ELSE 0 END
           + 20000 - 200000
  FROM d WHERE i < 29
)
SELECT @run, x.target_date, x.i, x.opening,
       x.ci, x.co, 0, 40000, 0, x.eo, x.an,
       x.expected - 300000, x.expected, x.expected + 200000,
       x.expected < 0, x.is_hol, x.note
FROM (
  SELECT i, opening,
         DATE_ADD('2025-07-15', INTERVAL i DAY) AS target_date,
         CASE WHEN i = 5 THEN 680000 ELSE 0 END AS ci,
         CASE WHEN i = 7 THEN 950000 WHEN i = 10 THEN 380000 ELSE 0 END AS co,
         CASE WHEN i = 5 THEN 120000 ELSE 0 END AS eo,
         CASE WHEN i = 5 THEN 50000 ELSE 0 END AS an,
         (i = 3) AS is_hol,
         CASE WHEN i = 3
              THEN '7월 19일(토) 주말로 원리금 상환 기준일이 7월 18일(금)로 앞당겨졌습니다.'
              ELSE NULL END AS note,
         opening
           + CASE WHEN i = 5 THEN 680000 ELSE 0 END
           - CASE WHEN i = 7 THEN 950000 WHEN i = 10 THEN 380000 ELSE 0 END
           + CASE WHEN i = 5 THEN 50000 ELSE 0 END
           + 20000 - 200000 AS expected
  FROM d
) x
WHERE NOT EXISTS (SELECT 1 FROM forecast_daily WHERE forecast_run_id = @run);

-- 4-b) 07-20(D+5) 근거 라인 4건 ------------------------------------------------
SET @d5 = (SELECT id FROM forecast_daily WHERE forecast_run_id = @run AND target_date = '2025-07-20');

INSERT INTO forecast_daily_items
  (forecast_daily_id, item_kind, label, sub_label, direction, amount_min, amount_max, ref_type, ref_id)
SELECT @d5, v.item_kind, v.label, v.sub_label, v.direction, v.amount_min, v.amount_max, v.ref_type, NULL
FROM (
  SELECT 'CONFIRMED'  AS item_kind, '카드 매출 정산' AS label, '신한카드 · 전일 매출 확정' AS sub_label,
         'I' AS direction, 680000 AS amount_min, 680000 AS amount_max, 'CARD_SETTLEMENT' AS ref_type
  UNION ALL SELECT 'EXPECTED',  '현금 매출 추정', '최근 8주 평균 기반',       'I', 420000, 710000, NULL
  UNION ALL SELECT 'EXPECTED',  '공과금 예정',   '반복 패턴 추정 · 격월 납부', 'O', 120000, 120000, NULL
  UNION ALL SELECT 'ADJUSTMENT','현금매출 추가 입력', '사용자 직접 입력 · 확정',  'I',  50000,  50000, NULL
) v
WHERE @d5 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM forecast_daily_items WHERE forecast_daily_id = @d5);


-- 5) 서술 문구 보강 (STATE_CHANGE_HINT + DISCLAIMER) --------------------------
--    STATUS_LABEL / RISK_NOTE 는 seed-prod-home.sql 이 이미 넣음.
INSERT INTO forecast_run_narratives (forecast_run_id, kind, seq, text)
SELECT @run, 'STATE_CHANGE_HINT', 0, '현금매출·타행자금을 보정하지 않았거나 예정 지출이 갑자기 늘면 상태가 더 나빠질 수 있습니다.'
WHERE NOT EXISTS (SELECT 1 FROM forecast_run_narratives WHERE forecast_run_id = @run AND kind = 'STATE_CHANGE_HINT' AND seq = 0);

INSERT INTO forecast_run_narratives (forecast_run_id, kind, seq, text)
SELECT @run, 'DISCLAIMER', 0, '이 수치는 연동·보정된 데이터 기준 추정이며 실제 결과와 다를 수 있습니다.'
WHERE NOT EXISTS (SELECT 1 FROM forecast_run_narratives WHERE forecast_run_id = @run AND kind = 'DISCLAIMER' AND seq = 0);


-- 6) 셀프 액션 저장 : 자체 실행 계획 1건(ACTIVE) + 준비 항목 3건 -------------------
INSERT INTO recovery_self_action_plans
  (business_id, forecast_run_id, recovery_option_id, expected_effect_text, status, saved_at)
SELECT @biz, @run, @opt_repay, '첫 부족일 +16일 연장, 월 상환액 약 15만 원 감소 예상', 'ACTIVE', '2025-07-15 02:10:00'
WHERE @opt_repay IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM recovery_self_action_plans
    WHERE business_id = @biz AND forecast_run_id = @run AND recovery_option_id = @opt_repay
  );

SET @plan = (
  SELECT id FROM recovery_self_action_plans
  WHERE business_id = @biz AND forecast_run_id = @run AND recovery_option_id = @opt_repay
  ORDER BY id DESC LIMIT 1
);

INSERT INTO recovery_self_action_items (self_action_plan_id, title, target_date, status, memo)
SELECT @plan, '거래 은행에 원리금 납부일 변경 신청', '2025-07-18', 'DONE', '영업점 방문 완료'
WHERE @plan IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_self_action_items WHERE self_action_plan_id = @plan AND title = '거래 은행에 원리금 납부일 변경 신청');

INSERT INTO recovery_self_action_items (self_action_plan_id, title, target_date, status, memo)
SELECT @plan, '임대인에게 7월 임차료 납부일 조정 요청', '2025-07-21', 'PENDING', NULL
WHERE @plan IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_self_action_items WHERE self_action_plan_id = @plan AND title = '임대인에게 7월 임차료 납부일 조정 요청');

INSERT INTO recovery_self_action_items (self_action_plan_id, title, target_date, status, memo)
SELECT @plan, '자동이체 3건 납부일 분산 재설정', '2025-07-22', 'PENDING', NULL
WHERE @plan IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_self_action_items WHERE self_action_plan_id = @plan AND title = '자동이체 3건 납부일 분산 재설정');


-- 7) 사후점검 : D30(완료+결과) / D60 / D90 일정 + 회복안 실행 상태 2건 ------------
INSERT INTO recovery_followup_schedules
  (business_id, packet_id, forecast_run_id, checkpoint, scheduled_date, status, consent_id)
SELECT @biz, NULL, @run, 'D30', '2025-08-14', 'DONE', @consent_track
WHERE @consent_track IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_followup_schedules WHERE business_id = @biz AND checkpoint = 'D30');

INSERT INTO recovery_followup_schedules
  (business_id, packet_id, forecast_run_id, checkpoint, scheduled_date, status, consent_id)
SELECT @biz, NULL, @run, 'D60', '2025-09-13', 'SCHEDULED', @consent_track
WHERE @consent_track IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_followup_schedules WHERE business_id = @biz AND checkpoint = 'D60');

INSERT INTO recovery_followup_schedules
  (business_id, packet_id, forecast_run_id, checkpoint, scheduled_date, status, consent_id)
SELECT @biz, NULL, @run, 'D90', '2025-10-13', 'SCHEDULED', @consent_track
WHERE @consent_track IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_followup_schedules WHERE business_id = @biz AND checkpoint = 'D90');

SET @sch_d30 = (SELECT id FROM recovery_followup_schedules WHERE business_id = @biz AND checkpoint = 'D30' LIMIT 1);

INSERT INTO recovery_followup_results
  (followup_schedule_id, balance_recovered, has_delinquency, baseline_balance, current_balance,
   recovery_amount, latest_forecast_run_id, risk_status, recorded_at)
SELECT @sch_d30, 'PARTIAL', FALSE, 540000, 2180000, 1640000, @run, 'STABLE', '2025-08-14 02:00:00'
WHERE @sch_d30 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_followup_results WHERE followup_schedule_id = @sch_d30);

INSERT INTO recovery_execution_status
  (business_id, recovery_option_id, forecast_run_id, status, blocker_text, updated_at)
SELECT @biz, @opt_repay, @run, 'IN_PROGRESS', NULL, '2025-08-10 05:00:00'
WHERE @opt_repay IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_execution_status WHERE business_id = @biz AND recovery_option_id = @opt_repay);

INSERT INTO recovery_execution_status
  (business_id, recovery_option_id, forecast_run_id, status, blocker_text, updated_at)
SELECT @biz, @opt_shift, @run, 'BLOCKED', '임대인 회신 지연 — 담당자 확인 필요', '2025-08-10 05:00:00'
WHERE @opt_shift IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM recovery_execution_status WHERE business_id = @biz AND recovery_option_id = @opt_shift);


COMMIT;

-- 확인용 출력
SELECT @biz AS business_id, @run AS forecast_run_id,
       (SELECT COUNT(*) FROM source_data_sources        WHERE business_id = @biz) AS data_sources,
       (SELECT COUNT(*) FROM source_adjustments         WHERE business_id = @biz) AS adjustments,
       (SELECT COUNT(*) FROM source_adjustment_suggestions WHERE business_id = @biz) AS suggestions,
       (SELECT COUNT(*) FROM forecast_daily             WHERE forecast_run_id = @run) AS daily_rows,
       (SELECT COUNT(*) FROM forecast_daily_items       WHERE forecast_daily_id = @d5) AS d5_items,
       @plan AS self_action_plan_id,
       (SELECT COUNT(*) FROM recovery_self_action_items WHERE self_action_plan_id = @plan) AS self_action_items,
       (SELECT COUNT(*) FROM recovery_followup_schedules WHERE business_id = @biz) AS followup_schedules,
       @sch_d30 AS followup_d30_schedule_id,
       (SELECT COUNT(*) FROM recovery_execution_status  WHERE business_id = @biz) AS execution_status_rows;


-- ─────────────────────────────────────────────────────────────────────────────
-- 되돌리기 (필요 시 수동 실행)
-- ─────────────────────────────────────────────────────────────────────────────
-- SET @biz = (SELECT id FROM core_businesses WHERE biz_reg_no = 'DEMO-HOME-RISK');
-- SET @run = (SELECT id FROM forecast_runs WHERE business_id = @biz ORDER BY id DESC LIMIT 1);
-- SET @plan = (SELECT id FROM recovery_self_action_plans WHERE business_id = @biz AND forecast_run_id = @run ORDER BY id DESC LIMIT 1);
-- DELETE FROM recovery_followup_results   WHERE followup_schedule_id IN (SELECT id FROM recovery_followup_schedules WHERE business_id = @biz);
-- DELETE FROM recovery_followup_schedules WHERE business_id = @biz;
-- DELETE FROM recovery_execution_status   WHERE business_id = @biz;
-- DELETE FROM recovery_self_action_items  WHERE self_action_plan_id = @plan;
-- DELETE FROM recovery_self_action_plans  WHERE id = @plan;
-- DELETE FROM forecast_run_narratives     WHERE forecast_run_id = @run AND kind IN ('STATE_CHANGE_HINT', 'DISCLAIMER');
-- DELETE FROM forecast_daily_items        WHERE forecast_daily_id IN (SELECT id FROM forecast_daily WHERE forecast_run_id = @run);
-- DELETE FROM forecast_daily              WHERE forecast_run_id = @run;
-- DELETE FROM source_adjustment_suggestions WHERE business_id = @biz;
-- DELETE FROM source_adjustments          WHERE business_id = @biz;
-- DELETE FROM source_data_sources         WHERE business_id = @biz;
-- DELETE FROM core_consents               WHERE business_id = @biz AND consent_type_code = 'FOLLOWUP_TRACKING';
