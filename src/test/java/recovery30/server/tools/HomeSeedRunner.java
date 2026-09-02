package recovery30.server.tools;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * 운영/클라우드 DB(=local 프로파일의 {@code application-local.yml} 접속정보)에 사업자 홈 화면 API 검증용 더미 데이터를 1건 주입한다.
 *
 * <p>일반 테스트가 아니다. CI/{@code ./gradlew test}에서는 {@code SEED_HOME} 환경변수가 없으면 건너뛴다. 실행:
 *
 * <pre>
 *   SEED_HOME=true ./gradlew test --tests "recovery30.server.tools.HomeSeedRunner"
 * </pre>
 *
 * <p>스키마는 건드리지 않는다({@code ddl-auto=validate}, {@code flyway.enabled=false}). 값은 {@code
 * scripts/seed-prod-home.sql} / {@code DemoForecastSeeder}의 QA-RISK와 동일하며 모든 INSERT는 존재 여부를 먼저
 * 확인한다(재실행 안전). 되돌리기는 {@code scripts/seed-prod-home.sql} 하단 블록 참고.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("local")
@TestPropertySource(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
      "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
      "demo.seed.enabled=false"
    })
@EnabledIfEnvironmentVariable(named = "SEED_HOME", matches = "true")
class HomeSeedRunner {

  private static final Logger log = LoggerFactory.getLogger(HomeSeedRunner.class);
  private static final Timestamp TS = Timestamp.valueOf("2025-07-14 23:32:00");
  private static final String EMAIL = "demo-home@recovery30.local";
  private static final String BIZ_REG_NO = "DEMO-HOME-RISK";
  private static final Date BASE_DATE = Date.valueOf("2025-07-15");

  @Autowired private JdbcTemplate jdbc;

  @Test
  void RISK_페르소나_1건을_주입한다() {
    long userId = ensureUser();
    long businessId = ensureBusiness(userId);
    long consentId = ensureConsent(businessId);
    long runId = ensureRun(businessId, consentId);
    ensureRiskDrivers(runId);
    ensureCoverage(runId);
    ensureNarratives(runId);

    log.info("[seed] 완료 - business_id={}, forecast_run_id={}", businessId, runId);
    log.info(
        "[seed] 검증: curl -s https://recovery-30.shop/api/businesses/{}/forecasts/latest",
        businessId);
    log.info(
        "[seed] 검증: curl -s https://recovery-30.shop/api/forecasts/{}/risk-drivers?limit=3", runId);
  }

  private long ensureUser() {
    Long id = firstLong("SELECT id FROM core_users WHERE email = ?", EMAIL);
    if (id != null) {
      return id;
    }
    jdbc.update(
        "INSERT INTO core_users (email, password_hash, name, status, created_at) VALUES (?, ?, ?, ?, ?)",
        EMAIL,
        "{noop}demo",
        "데모 상점 대표",
        "ACTIVE",
        TS);
    return firstLong("SELECT id FROM core_users WHERE email = ?", EMAIL);
  }

  private long ensureBusiness(long userId) {
    Long id = firstLong("SELECT id FROM core_businesses WHERE biz_reg_no = ?", BIZ_REG_NO);
    if (id != null) {
      return id;
    }
    jdbc.update(
        "INSERT INTO core_businesses (user_id, biz_reg_no, biz_name, industry_code, opened_at,"
            + " region_code, annual_revenue, employee_count, safety_buffer_amount, created_at)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        userId,
        BIZ_REG_NO,
        "데모 상점 (RISK)",
        "I56111",
        Date.valueOf("2023-01-15"),
        "11110",
        180_000_000L,
        2,
        1_000_000L,
        TS);
    return firstLong("SELECT id FROM core_businesses WHERE biz_reg_no = ?", BIZ_REG_NO);
  }

  private long ensureConsent(long businessId) {
    Long id =
        firstLong(
            "SELECT id FROM core_consents WHERE business_id = ? AND consent_type_code = 'ANALYSIS'",
            businessId);
    if (id != null) {
      return id;
    }
    jdbc.update(
        "INSERT INTO core_consents (business_id, consent_type_code, consent_version, status,"
            + " granted_at, ip_address, user_agent) VALUES (?, 'ANALYSIS', 'v1.0', 'GRANTED', ?, ?, ?)",
        businessId,
        TS,
        "127.0.0.1",
        "HomeSeedRunner");
    return firstLong(
        "SELECT id FROM core_consents WHERE business_id = ? AND consent_type_code = 'ANALYSIS'",
        businessId);
  }

  private long ensureRun(long businessId, long consentId) {
    Long id =
        firstLong(
            "SELECT id FROM forecast_runs WHERE business_id = ? AND base_date = ? ORDER BY id DESC"
                + " LIMIT 1",
            businessId,
            BASE_DATE);
    if (id != null) {
      return id;
    }
    jdbc.update(
        "INSERT INTO forecast_runs (business_id, consent_id, base_date, horizon_days, status,"
            + " confidence_level, coverage_overall, first_shortfall_date, days_to_shortfall,"
            + " min_balance_conservative, min_balance_expected, min_balance_optimistic,"
            + " shortfall_amount_min, shortfall_amount_max, is_buffer_met, model_version,"
            + " ruleset_version, triggered_by, created_at) VALUES (?, ?, ?, 30, 'RISK', 'MEDIUM',"
            + " 84.00, ?, 11, -1280000, 540000, 830000, 760000, 1240000, 0, 'fc-model-v0.3',"
            + " 'rule-2025-06', 'SCHEDULED', ?)",
        businessId,
        consentId,
        BASE_DATE,
        Date.valueOf("2025-07-26"),
        TS);
    return firstLong(
        "SELECT id FROM forecast_runs WHERE business_id = ? AND base_date = ? ORDER BY id DESC LIMIT 1",
        businessId,
        BASE_DATE);
  }

  private void ensureRiskDrivers(long runId) {
    if (count("forecast_risk_drivers", runId) > 0) {
      return;
    }
    jdbc.update(
        "INSERT INTO forecast_risk_drivers (forecast_run_id, rank_no, driver_code, title,"
            + " contribution_amount, is_estimating, occurrence_date, impact_period_text, metric_text,"
            + " description, assumption_text) VALUES"
            + " (?, 1, 'RENT_LOAN_CONCENTRATION', '월말 원리금 임차료 집중', -1850000, 0, ?, NULL, NULL,"
            + " '7월 말 임차료 150만 원과 대출 원리금 170만 원이 같은 주에 출금 예정입니다.', '최근 3개월 출금 이력 기반 반영'),"
            + " (?, 2, 'SALES_DECLINE_4W', '최근 4주 매출 감소 추세', NULL, 1, NULL, '6월 15일~7월 12일 영향', '-18%',"
            + " '최근 4주 평균 대비 카드 정산 수입이 감소 추세입니다.', '직전 4주 평균 입금 패턴 반영'),"
            + " (?, 3, 'AUTODEBIT_OVERLAP', '자동이체 3건 납부일 겹침', NULL, 0, ?, NULL, NULL, NULL, NULL)",
        runId, Date.valueOf("2025-07-31"), runId, runId, Date.valueOf("2025-07-28"));
  }

  private void ensureCoverage(long runId) {
    if (count("forecast_coverage", runId) > 0) {
      return;
    }
    insertCoverage(runId, "BANK_ACCOUNT", "95.00", 0);
    insertCoverage(runId, "CARD_SETTLEMENT", "92.00", 0);
    insertCoverage(runId, "LOAN", "88.00", 0);
    insertCoverage(runId, "AUTO_TRANSFER", "61.00", 1);
  }

  private void insertCoverage(long runId, String sourceType, String rate, int below) {
    jdbc.update(
        "INSERT INTO forecast_coverage (forecast_run_id, source_type, coverage_rate, last_synced_at,"
            + " is_below_threshold) VALUES (?, ?, ?, ?, ?)",
        runId,
        sourceType,
        new BigDecimal(rate),
        TS,
        below);
  }

  private void ensureNarratives(long runId) {
    if (count("forecast_run_narratives", runId) > 0) {
      return;
    }
    jdbc.update(
        "INSERT INTO forecast_run_narratives (forecast_run_id, kind, seq, text) VALUES"
            + " (?, 'STATUS_LABEL', 0, '주의 필요'), (?, 'RISK_NOTE', 0, '부족일까지 11일 남았습니다.')",
        runId,
        runId);
  }

  private int count(String table, long runId) {
    Integer n =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + table + " WHERE forecast_run_id = ?", Integer.class, runId);
    return n == null ? 0 : n;
  }

  private Long firstLong(String sql, Object... args) {
    return jdbc.query(sql, rs -> rs.next() ? rs.getLong(1) : null, args);
  }
}
