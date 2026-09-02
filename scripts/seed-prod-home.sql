-- ─────────────────────────────────────────────────────────────────────────────
-- 사업자 홈 화면 API 검증용 더미 데이터 (RISK 페르소나 1건)
--
-- 대상        : 운영 DB (recovery30)  ※ 반드시 V1~V15 마이그레이션 적용 후 실행
-- 실행        : mysql -h <host> -u <user> -p recovery30 < scripts/seed-prod-home.sql
-- 재실행 안전 : 모든 INSERT 에 WHERE NOT EXISTS 가드. 이미 있으면 건너뜀
-- 롤백        : 맨 아래 "-- 되돌리기" 블록 참고
--
-- 값은 forecast 모듈 DemoForecastSeeder 의 QA-RISK 와 동일 (CHECK 제약 통과 검증됨).
-- 로그인은 필요 없음 (홈 API 는 businessId path 파라미터만 받고 인증 없음).
-- ─────────────────────────────────────────────────────────────────────────────

SET NAMES utf8mb4;
START TRANSACTION;

-- 1) 계정 -----------------------------------------------------------------------
INSERT INTO core_users (email, password_hash, name, status, created_at)
SELECT 'demo-home@recovery30.local', '{noop}demo', '데모 상점 대표', 'ACTIVE', '2025-07-14 23:32:00'
WHERE NOT EXISTS (SELECT 1 FROM core_users WHERE email = 'demo-home@recovery30.local');

SET @user_id = (SELECT id FROM core_users WHERE email = 'demo-home@recovery30.local');

-- 2) 사업자 -------------------------------------------------------------------
INSERT INTO core_businesses
  (user_id, biz_reg_no, biz_name, industry_code, opened_at, region_code,
   annual_revenue, employee_count, safety_buffer_amount, created_at)
SELECT @user_id, 'DEMO-HOME-RISK', '데모 상점 (RISK)', 'I56111', '2023-01-15', '11110',
       180000000, 2, 1000000, '2025-07-14 23:32:00'
WHERE NOT EXISTS (SELECT 1 FROM core_businesses WHERE biz_reg_no = 'DEMO-HOME-RISK');

SET @business_id = (SELECT id FROM core_businesses WHERE biz_reg_no = 'DEMO-HOME-RISK');

-- 3) 분석 동의 (forecast_runs.consent_id NOT NULL FK) --------------------------
INSERT INTO core_consents
  (business_id, consent_type_code, consent_version, status, granted_at, ip_address, user_agent)
SELECT @business_id, 'ANALYSIS', 'v1.0', 'GRANTED', '2025-07-14 23:32:00', '127.0.0.1', 'seed-prod-home.sql'
WHERE NOT EXISTS (
  SELECT 1 FROM core_consents WHERE business_id = @business_id AND consent_type_code = 'ANALYSIS'
);

SET @consent_id = (
  SELECT id FROM core_consents WHERE business_id = @business_id AND consent_type_code = 'ANALYSIS'
);

-- 4) 예측 실행 (RISK) ---------------------------------------------------------
INSERT INTO forecast_runs
  (business_id, consent_id, base_date, horizon_days, status, confidence_level, coverage_overall,
   first_shortfall_date, days_to_shortfall,
   min_balance_conservative, min_balance_expected, min_balance_optimistic,
   shortfall_amount_min, shortfall_amount_max, is_buffer_met,
   model_version, ruleset_version, triggered_by, created_at)
SELECT @business_id, @consent_id, '2025-07-15', 30, 'RISK', 'MEDIUM', 84.00,
       '2025-07-26', 11,
       -1280000, 540000, 830000,
       760000, 1240000, FALSE,
       'fc-model-v0.3', 'rule-2025-06', 'SCHEDULED', '2025-07-14 23:32:00'
WHERE NOT EXISTS (
  SELECT 1 FROM forecast_runs WHERE business_id = @business_id AND base_date = '2025-07-15'
);

SET @run_id = (
  SELECT id FROM forecast_runs
  WHERE business_id = @business_id AND base_date = '2025-07-15'
  ORDER BY id DESC LIMIT 1
);

-- 5) 부족 원인 Top 3 --------------------------------------------------------
INSERT INTO forecast_risk_drivers
  (forecast_run_id, rank_no, driver_code, title, contribution_amount, is_estimating,
   occurrence_date, occurrence_text, impact_period_text, metric_text, description, assumption_text)
SELECT @run_id, 1, 'RENT_LOAN_CONCENTRATION', '월말 원리금 임차료 집중', -1850000, FALSE,
       '2025-07-31', NULL, NULL, NULL,
       '7월 말 임차료 150만 원과 대출 원리금 170만 원이 같은 주에 출금 예정입니다.',
       '최근 3개월 출금 이력 기반 반영'
WHERE NOT EXISTS (SELECT 1 FROM forecast_risk_drivers WHERE forecast_run_id = @run_id AND rank_no = 1);

INSERT INTO forecast_risk_drivers
  (forecast_run_id, rank_no, driver_code, title, contribution_amount, is_estimating,
   occurrence_date, occurrence_text, impact_period_text, metric_text, description, assumption_text)
SELECT @run_id, 2, 'SALES_DECLINE_4W', '최근 4주 매출 감소 추세', NULL, TRUE,
       NULL, NULL, '6월 15일~7월 12일 영향', '-18%',
       '최근 4주 평균 대비 카드 정산 수입이 감소 추세입니다.',
       '직전 4주 평균 입금 패턴 반영'
WHERE NOT EXISTS (SELECT 1 FROM forecast_risk_drivers WHERE forecast_run_id = @run_id AND rank_no = 2);

INSERT INTO forecast_risk_drivers
  (forecast_run_id, rank_no, driver_code, title, contribution_amount, is_estimating,
   occurrence_date, occurrence_text, impact_period_text, metric_text, description, assumption_text)
SELECT @run_id, 3, 'AUTODEBIT_OVERLAP', '자동이체 3건 납부일 겹침', NULL, FALSE,
       '2025-07-28', NULL, NULL, NULL, NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM forecast_risk_drivers WHERE forecast_run_id = @run_id AND rank_no = 3);

-- 6) 소스별 커버리지 스냅샷 ------------------------------------------------
INSERT INTO forecast_coverage (forecast_run_id, source_type, coverage_rate, last_synced_at, is_below_threshold)
SELECT @run_id, 'BANK_ACCOUNT', 95.00, '2025-07-14 23:32:00', FALSE
WHERE NOT EXISTS (SELECT 1 FROM forecast_coverage WHERE forecast_run_id = @run_id AND source_type = 'BANK_ACCOUNT');

INSERT INTO forecast_coverage (forecast_run_id, source_type, coverage_rate, last_synced_at, is_below_threshold)
SELECT @run_id, 'CARD_SETTLEMENT', 92.00, '2025-07-14 23:32:00', FALSE
WHERE NOT EXISTS (SELECT 1 FROM forecast_coverage WHERE forecast_run_id = @run_id AND source_type = 'CARD_SETTLEMENT');

INSERT INTO forecast_coverage (forecast_run_id, source_type, coverage_rate, last_synced_at, is_below_threshold)
SELECT @run_id, 'LOAN', 88.00, '2025-07-14 23:32:00', FALSE
WHERE NOT EXISTS (SELECT 1 FROM forecast_coverage WHERE forecast_run_id = @run_id AND source_type = 'LOAN');

INSERT INTO forecast_coverage (forecast_run_id, source_type, coverage_rate, last_synced_at, is_below_threshold)
SELECT @run_id, 'AUTO_TRANSFER', 61.00, '2025-07-14 23:32:00', TRUE
WHERE NOT EXISTS (SELECT 1 FROM forecast_coverage WHERE forecast_run_id = @run_id AND source_type = 'AUTO_TRANSFER');

-- 7) 서술 문구 -----------------------------------------------------------
INSERT INTO forecast_run_narratives (forecast_run_id, kind, seq, text)
SELECT @run_id, 'STATUS_LABEL', 0, '주의 필요'
WHERE NOT EXISTS (SELECT 1 FROM forecast_run_narratives WHERE forecast_run_id = @run_id AND kind = 'STATUS_LABEL' AND seq = 0);

INSERT INTO forecast_run_narratives (forecast_run_id, kind, seq, text)
SELECT @run_id, 'RISK_NOTE', 0, '부족일까지 11일 남았습니다.'
WHERE NOT EXISTS (SELECT 1 FROM forecast_run_narratives WHERE forecast_run_id = @run_id AND kind = 'RISK_NOTE' AND seq = 0);

COMMIT;

-- 결과 확인 : 아래 두 값을 curl 에 사용
SELECT @business_id AS business_id, @run_id AS forecast_run_id;


-- ─────────────────────────────────────────────────────────────────────────────
-- 되돌리기 (필요 시 수동 실행)
-- ─────────────────────────────────────────────────────────────────────────────
-- SET @business_id = (SELECT id FROM core_businesses WHERE biz_reg_no = 'DEMO-HOME-RISK');
-- SET @run_id = (SELECT id FROM forecast_runs WHERE business_id = @business_id AND base_date = '2025-07-15' ORDER BY id DESC LIMIT 1);
-- DELETE FROM forecast_run_narratives   WHERE forecast_run_id = @run_id;
-- DELETE FROM forecast_coverage         WHERE forecast_run_id = @run_id;
-- DELETE FROM forecast_risk_drivers     WHERE forecast_run_id = @run_id;
-- DELETE FROM forecast_runs             WHERE id = @run_id;
-- DELETE FROM core_consents             WHERE business_id = @business_id;
-- DELETE FROM core_businesses           WHERE id = @business_id;
-- DELETE FROM core_users                WHERE email = 'demo-home@recovery30.local';
