package recovery30.server.forecast;

import java.time.Instant;
import java.time.LocalDate;
import recovery30.server.forecast.domain.ForecastCoverage;
import recovery30.server.forecast.domain.ForecastRun;
import recovery30.server.forecast.domain.RiskDriver;
import recovery30.server.forecast.domain.RiskDriverEvidence;

/** forecast 슬라이스 통합테스트용 엔티티 픽스처. */
public final class ForecastFixtures {

  private ForecastFixtures() {}

  public static ForecastRun run(long businessId, LocalDate baseDate, String status) {
    ForecastRun r = new ForecastRun();
    r.setBusinessId(businessId);
    r.setConsentId(1L);
    r.setBaseDate(baseDate);
    r.setHorizonDays(30);
    r.setStatus(status);
    r.setConfidenceLevel("MEDIUM");
    r.setModelVersion("fc-model-v0.3");
    r.setRulesetVersion("rule-2025-06");
    r.setTriggeredBy("SCHEDULED");
    r.setCreatedAt(Instant.parse("2025-07-14T23:32:00Z"));
    return r;
  }

  public static RiskDriver driver(long forecastRunId, int rank, String driverCode, String title) {
    RiskDriver d = new RiskDriver();
    d.setForecastRunId(forecastRunId);
    d.setRankNo(rank);
    d.setDriverCode(driverCode);
    d.setTitle(title);
    return d;
  }

  public static RiskDriverEvidence evidence(long riskDriverId, String label, String periodText) {
    RiskDriverEvidence e = new RiskDriverEvidence();
    e.setRiskDriverId(riskDriverId);
    e.setLabel(label);
    e.setPeriodText(periodText);
    return e;
  }

  public static ForecastCoverage coverage(long forecastRunId, String sourceType, boolean below) {
    ForecastCoverage c = new ForecastCoverage();
    c.setForecastRunId(forecastRunId);
    c.setSourceType(sourceType);
    c.setBelowThreshold(below);
    return c;
  }
}
