-- 회복안 카탈로그(마스터). 지원제도(V14)와 같은 레퍼런스 데이터로, 모든 환경에 공통 적재한다.
-- per-run 시나리오(recovery_scenarios / recovery_scenario_options)는 @Profile("demo") 시더가 담당.

INSERT INTO recovery_options
  (option_code, category, expected_effect_text, monthly_burden_change_text,
   precondition_text, difficulty, requires_review, disclaimer)
VALUES
  ('REPAYMENT_ADJUST', 'FINANCIAL_CONSULT',
   '부족일 최대 16일 연장 가능', '월 상환액 약 15만 원 감소 예상',
   '원리금 3회 이상 정상 납부 이력', 'MID', TRUE,
   '승인 여부와 조건은 금융기관 심사 결과에 따릅니다.'),

  ('DUEDATE_SHIFT', 'SELF_ACTION',
   '월말 집중 부담 분산', '총액 변화 없음, 시기 조정',
   '임대인·기관과 납부일 협의 가능 여부', 'LOW', FALSE,
   '납부일 변경 가능 여부는 계약 및 기관 방침에 따릅니다.'),

  ('POLICY_FUND_LINK', 'SUPPORT_PROGRAM',
   '부족액 일부 보완 가능', '신규 상환 발생 (한도·금리 미확정)',
   '자격 요건 별도 확인 필요', 'HIGH', TRUE,
   '자격 요건과 한도는 공식 출처 및 상담자 확인이 필요합니다.'),

  ('RATE_CUT_REFINANCE', 'FINANCIAL_CONSULT',
   '이자 부담 경감 가능', '금리·한도 미확정, 상담 필요',
   '신용 상태·거래 이력 기반 검토', 'MID', TRUE,
   '금리 인하 및 대환 승인은 금융기관 심사 결과에 따릅니다.'),

  ('BIZ_DIAGNOSIS', 'SUPPORT_PROGRAM',
   '중장기 매출 회복 가능성', '단기 현금흐름 직접 효과 낮음',
   '지역·업종별 지원 기관 확인 필요', 'LOW', FALSE,
   '단기 부족 해소보다 중장기 회복을 목표로 하는 옵션입니다.');
