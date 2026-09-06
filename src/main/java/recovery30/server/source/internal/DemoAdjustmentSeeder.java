package recovery30.server.source.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.business.api.BusinessApi;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.source.domain.Adjustment;
import recovery30.server.source.domain.AdjustmentSuggestion;

/**
 * demo 프로파일에서 정보 보정 화면용 데이터를 주입한다: QA-RISK 의 보정값 2건(SAVED, Packet snapshot 과 일치)과 반복 패턴 추정 후보
 * 2건(PROPOSED). db-design 6절대로 후보는 목데이터.
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(8)
public class DemoAdjustmentSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoAdjustmentSeeder.class);
  private static final Instant TS = Instant.parse("2025-07-14T00:00:00Z");

  private final BusinessApi businessApi;
  private final ForecastApi forecastApi;
  private final AdjustmentRepository adjustmentRepository;
  private final AdjustmentSuggestionRepository suggestionRepository;

  public DemoAdjustmentSeeder(
      BusinessApi businessApi,
      ForecastApi forecastApi,
      AdjustmentRepository adjustmentRepository,
      AdjustmentSuggestionRepository suggestionRepository) {
    this.businessApi = businessApi;
    this.forecastApi = forecastApi;
    this.adjustmentRepository = adjustmentRepository;
    this.suggestionRepository = suggestionRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Long businessId = businessApi.findBusinessIdByRegNo("QA-RISK").orElse(null);
    if (businessId == null
        || !adjustmentRepository
            .findByBusinessIdOrderByExpectedDateAscIdAsc(businessId)
            .isEmpty()) {
      return;
    }
    Long runId = forecastApi.findLatestForecastRunId(businessId).orElse(null);

    adjustmentRepository.save(
        adjustment(
            businessId,
            "CASH_SALES",
            "I",
            650_000L,
            LocalDate.of(2025, 7, 20),
            "매주 토요일 현금 매출",
            "SAVED",
            runId));
    adjustmentRepository.save(
        adjustment(
            businessId,
            "EXPECTED_EXPENSE",
            "O",
            1_200_000L,
            LocalDate.of(2025, 7, 22),
            "인테리어 대금",
            "SAVED",
            runId));

    suggestionRepository.save(
        suggestion(businessId, "CASH_SALES", 1_200_000L, "매월 15일", "최근 3개월 동일 패턴", "0.82"));
    suggestionRepository.save(
        suggestion(businessId, "EXTERNAL_FUND", 850_000L, "매월 말일", "최근 2개월 유사 패턴", "0.70"));

    log.info("[demo] QA-RISK 보정값 2건 + 추정 후보 2건 시딩");
  }

  private static Adjustment adjustment(
      long businessId,
      String type,
      String direction,
      long amount,
      LocalDate expectedDate,
      String memo,
      String status,
      Long appliedRunId) {
    Adjustment a = new Adjustment();
    a.setBusinessId(businessId);
    a.setAdjustmentType(type);
    a.setDirection(direction);
    a.setAmount(amount);
    a.setExpectedDate(expectedDate);
    a.setCertainty("EXPECTED_EXPENSE".equals(type) ? "CONFIRMED" : "ESTIMATED");
    a.setMemo(memo);
    a.setStatus(status);
    a.setAppliedRunId("SAVED".equals(status) ? appliedRunId : null);
    a.setCreatedAt(TS);
    a.setUpdatedAt(TS);
    return a;
  }

  private static AdjustmentSuggestion suggestion(
      long businessId, String type, long amount, String rule, String evidence, String confidence) {
    AdjustmentSuggestion s = new AdjustmentSuggestion();
    s.setBusinessId(businessId);
    s.setAdjustmentType(type);
    s.setSuggestedAmount(amount);
    s.setSuggestedRule(rule);
    s.setEvidenceText(evidence);
    s.setConfidence(new BigDecimal(confidence));
    s.setStatus("PROPOSED");
    s.setCreatedAt(TS);
    return s;
  }
}
