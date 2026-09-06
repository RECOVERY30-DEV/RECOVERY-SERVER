package recovery30.server.forecast.internal;

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
import recovery30.server.forecast.domain.ForecastCoverage;
import recovery30.server.forecast.domain.ForecastDaily;
import recovery30.server.forecast.domain.ForecastDailyItem;
import recovery30.server.forecast.domain.ForecastRun;
import recovery30.server.forecast.domain.ForecastRunNarrative;
import recovery30.server.forecast.domain.RiskDriver;
import recovery30.server.forecast.domain.RiskDriverEvidence;

/**
 * demo 프로파일에서 QA 페르소나의 예측 데이터를 주입한다. 사업자·동의는 business 모듈의 DemoBusinessSeeder(@Order 1)가 먼저 만들고,
 * 여기서는 {@link BusinessApi}로 business_id / consent_id 만 조회한다. 이미 있으면 페르소나별로 건너뛴다(멱등).
 *
 * <p>QA-NEW는 의도적으로 예측을 만들지 않는다 ({@code GET /forecasts/latest} → 404 검증용).
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(2)
public class DemoForecastSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoForecastSeeder.class);
  private static final LocalDate BASE_DATE = LocalDate.of(2025, 7, 15);
  private static final Instant UPDATED_AT = Instant.parse("2025-07-14T23:32:00Z");

  private final BusinessApi businessApi;
  private final ForecastRunRepository runRepository;
  private final ForecastRiskDriverRepository driverRepository;
  private final RiskDriverEvidenceRepository evidenceRepository;
  private final ForecastCoverageRepository coverageRepository;
  private final ForecastRunNarrativeRepository narrativeRepository;
  private final ForecastDailyRepository dailyRepository;
  private final ForecastDailyItemRepository dailyItemRepository;

  public DemoForecastSeeder(
      BusinessApi businessApi,
      ForecastRunRepository runRepository,
      ForecastRiskDriverRepository driverRepository,
      RiskDriverEvidenceRepository evidenceRepository,
      ForecastCoverageRepository coverageRepository,
      ForecastRunNarrativeRepository narrativeRepository,
      ForecastDailyRepository dailyRepository,
      ForecastDailyItemRepository dailyItemRepository) {
    this.businessApi = businessApi;
    this.runRepository = runRepository;
    this.driverRepository = driverRepository;
    this.evidenceRepository = evidenceRepository;
    this.coverageRepository = coverageRepository;
    this.narrativeRepository = narrativeRepository;
    this.dailyRepository = dailyRepository;
    this.dailyItemRepository = dailyItemRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedRisk();
    seedStable();
    seedHold();
  }

  private void seedRisk() {
    Long businessId = resolve("QA-RISK");
    if (businessId == null || alreadySeeded(businessId)) {
      return;
    }
    long consentId = grantedConsent(businessId);

    ForecastRun run = baseRun(businessId, consentId, "RISK");
    run.setConfidenceLevel("MEDIUM");
    run.setCoverageOverall(new BigDecimal("84.00"));
    run.setFirstShortfallDate(LocalDate.of(2025, 7, 26));
    run.setDaysToShortfall(11);
    run.setMinBalanceConservative(-1_280_000L);
    run.setMinBalanceExpected(540_000L);
    run.setMinBalanceOptimistic(830_000L);
    run.setShortfallAmountMin(760_000L);
    run.setShortfallAmountMax(1_240_000L);
    run.setBufferMet(false);
    run = runRepository.save(run);

    RiskDriver d1 = driver(run.getId(), 1, "RENT_LOAN_CONCENTRATION", "월말 원리금 임차료 집중");
    d1.setOccurrenceDate(LocalDate.of(2025, 7, 31));
    d1.setContributionAmount(-1_850_000L);
    d1.setDescription("7월 말 임차료 150만 원과 대출 원리금 170만 원이 같은 주에 출금 예정입니다.");
    d1.setAssumptionText("최근 3개월 출금 이력 기반 반영");
    d1 = driverRepository.save(d1);

    RiskDriver d2 = driver(run.getId(), 2, "SALES_DECLINE_4W", "최근 4주 매출 감소 추세");
    d2.setMetricText("-18%");
    d2.setEstimating(true);
    d2.setDescription("최근 4주 평균 대비 카드 정산 수입이 감소 추세입니다.");
    d2.setAssumptionText("직전 4주 평균 입금 패턴 반영");
    d2 = driverRepository.save(d2);

    RiskDriver d3 = driver(run.getId(), 3, "AUTODEBIT_OVERLAP", "자동이체 3건 납부일 겹침");
    d3.setOccurrenceDate(LocalDate.of(2025, 7, 28));
    d3 = driverRepository.save(d3);

    evidence(d1.getId(), "RECURRING", "자동이체 2건 확정 · 매월 말일 반복", "매월 말일");
    evidence(d2.getId(), "CARD_SETTLEMENT", "신한카드 정산 5건", "7월 2일~11일");

    coverage(run.getId(), "BANK_ACCOUNT", "95.00", false);
    coverage(run.getId(), "CARD_SETTLEMENT", "92.00", false);
    coverage(run.getId(), "LOAN", "88.00", false);
    coverage(run.getId(), "AUTO_TRANSFER", "61.00", true);

    narrative(run.getId(), "STATUS_LABEL", 0, "주의 필요");
    narrative(run.getId(), "RISK_NOTE", 0, "부족일까지 11일 남았습니다.");

    seedDaily(run.getId());

    log.info("[demo] QA-RISK forecastRunId={}", run.getId());
  }

  /** QA-RISK 30일 일자별 캘린더. 잔액이 D-11(2025-07-26)에 0 밑으로 내려간다. 07-20에 근거 라인 4건. */
  private void seedDaily(long runId) {
    LocalDate base = LocalDate.of(2025, 7, 15);
    long opening = 2_000_000L;
    for (int i = 0; i < 30; i++) {
      LocalDate date = base.plusDays(i);
      long confirmedInflow = 0L;
      long confirmedOutflow = 0L;
      long adjustmentNet = 0L;
      long expectedInflowMax = 40_000L; // 잔잔한 일 매출 추정
      if (i == 5) {
        confirmedInflow = 680_000L;
        adjustmentNet = 50_000L;
      } else if (i == 7) {
        confirmedOutflow = 950_000L; // 임차료
      } else if (i == 10) {
        confirmedOutflow = 380_000L; // 원리금
      }
      long expected =
          opening
              + confirmedInflow
              - confirmedOutflow
              + adjustmentNet
              + 20_000L
              - 200_000L; // 하루 순감소 추세
      ForecastDaily d = new ForecastDaily();
      d.setForecastRunId(runId);
      d.setTargetDate(date);
      d.setDDay(i);
      d.setOpeningBalance(opening);
      d.setConfirmedInflow(confirmedInflow);
      d.setConfirmedOutflow(confirmedOutflow);
      d.setExpectedInflowMin(0L);
      d.setExpectedInflowMax(expectedInflowMax);
      d.setExpectedOutflowMin(0L);
      d.setExpectedOutflowMax(i == 5 ? 120_000L : 0L);
      d.setAdjustmentNet(adjustmentNet);
      d.setClosingBalanceConservative(expected - 300_000L);
      d.setClosingBalanceExpected(expected);
      d.setClosingBalanceOptimistic(expected + 200_000L);
      d.setShortfall(expected < 0);
      d.setHoliday(i == 3);
      d.setHolidayShiftNote(i == 3 ? "7월 19일(토) 주말로 원리금 상환 기준일이 7월 18일(금)로 앞당겨졌습니다." : null);
      d = dailyRepository.save(d);

      if (i == 5) {
        dailyItem(
            d.getId(),
            "CONFIRMED",
            "카드 매출 정산",
            "신한카드 · 전일 매출 확정",
            "I",
            680_000L,
            680_000L,
            "CARD_SETTLEMENT");
        dailyItem(d.getId(), "EXPECTED", "현금 매출 추정", "최근 8주 평균 기반", "I", 420_000L, 710_000L, null);
        dailyItem(
            d.getId(), "EXPECTED", "공과금 예정", "반복 패턴 추정 · 격월 납부", "O", 120_000L, 120_000L, null);
        dailyItem(
            d.getId(), "ADJUSTMENT", "현금매출 추가 입력", "사용자 직접 입력 · 확정", "I", 50_000L, 50_000L, null);
      }
      opening = expected;
    }
  }

  private void dailyItem(
      long dailyId,
      String kind,
      String label,
      String subLabel,
      String direction,
      long min,
      long max,
      String refType) {
    ForecastDailyItem item = new ForecastDailyItem();
    item.setForecastDailyId(dailyId);
    item.setItemKind(kind);
    item.setLabel(label);
    item.setSubLabel(subLabel);
    item.setDirection(direction);
    item.setAmountMin(min);
    item.setAmountMax(max);
    item.setRefType(refType);
    dailyItemRepository.save(item);
  }

  private void seedStable() {
    Long businessId = resolve("QA-STABLE");
    if (businessId == null || alreadySeeded(businessId)) {
      return;
    }
    long consentId = grantedConsent(businessId);

    ForecastRun run = baseRun(businessId, consentId, "STABLE");
    run.setConfidenceLevel("HIGH");
    run.setCoverageOverall(new BigDecimal("96.00"));
    run.setMinBalanceConservative(3_120_000L);
    run.setMinBalanceExpected(3_800_000L);
    run.setMinBalanceOptimistic(4_480_000L);
    run.setBufferMet(true);
    run = runRepository.save(run);

    coverage(run.getId(), "BANK_ACCOUNT", "96.00", false);
    coverage(run.getId(), "CARD_SETTLEMENT", "94.00", false);
    coverage(run.getId(), "LOAN", "97.00", false);
    coverage(run.getId(), "AUTO_TRANSFER", "90.00", false);

    narrative(run.getId(), "STATUS_LABEL", 0, "안전");
    narrative(run.getId(), "STABLE_REASON", 0, "최근 8주 매출이 전월 대비 안정적으로 유지되고 있습니다.");
    narrative(run.getId(), "STABLE_REASON", 1, "월말 임차료·원리금 납부 일정이 잔액 대비 감당 가능한 수준입니다.");
    narrative(
        run.getId(), "STATE_CHANGE_HINT", 0, "현금매출·타행자금을 보정하지 않았거나 예정 지출이 갑자기 늘면 상태가 바뀔 수 있습니다.");
    narrative(run.getId(), "DISCLAIMER", 0, "지원 자격·금융 승인·금리·한도는 상담자 및 공식 출처의 최종 확인이 필요합니다.");

    log.info("[demo] QA-STABLE forecastRunId={}", run.getId());
  }

  private void seedHold() {
    Long businessId = resolve("QA-HOLD");
    if (businessId == null || alreadySeeded(businessId)) {
      return;
    }
    long consentId = grantedConsent(businessId);

    ForecastRun run = baseRun(businessId, consentId, "HOLD");
    run.setConfidenceLevel("LOW");
    run.setCoverageOverall(new BigDecimal("62.00"));
    run.setBufferMet(false);
    // HOLD는 최저잔액 밴드를 산출하지 못한다 (min_balance_* = null).
    run = runRepository.save(run);

    coverage(run.getId(), "BANK_ACCOUNT", "72.00", false);
    coverage(run.getId(), "CARD_SETTLEMENT", "55.00", true);
    coverage(run.getId(), "LOAN", "40.00", true);
    coverage(run.getId(), "AUTO_TRANSFER", "61.00", true);

    narrative(run.getId(), "STATUS_LABEL", 0, "판단 보류");
    narrative(run.getId(), "DISCLAIMER", 0, "연결 데이터만으로 산출한 수치는 실제와 크게 다를 수 있습니다.");

    log.info("[demo] QA-HOLD forecastRunId={}", run.getId());
  }

  private Long resolve(String bizRegNo) {
    Long businessId = businessApi.findBusinessIdByRegNo(bizRegNo).orElse(null);
    if (businessId == null) {
      log.warn("[demo] {} 사업자가 없습니다 — DemoBusinessSeeder 실행 여부를 확인하세요", bizRegNo);
    }
    return businessId;
  }

  private boolean alreadySeeded(long businessId) {
    if (runRepository.existsByBusinessId(businessId)) {
      log.info("[demo] business {} 예측이 이미 있어 건너뜁니다", businessId);
      return true;
    }
    return false;
  }

  private long grantedConsent(long businessId) {
    return businessApi
        .findGrantedConsentId(businessId, "ANALYSIS")
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "business " + businessId + " 의 ANALYSIS 동의가 없습니다 — DemoBusinessSeeder 확인"));
  }

  private ForecastRun baseRun(long businessId, long consentId, String status) {
    ForecastRun run = new ForecastRun();
    run.setBusinessId(businessId);
    run.setConsentId(consentId);
    run.setBaseDate(BASE_DATE);
    run.setHorizonDays(30);
    run.setStatus(status);
    run.setConfidenceLevel("MEDIUM");
    run.setModelVersion("fc-model-v0.3");
    run.setRulesetVersion("rule-2025-06");
    run.setTriggeredBy("SCHEDULED");
    run.setCreatedAt(UPDATED_AT);
    return run;
  }

  private RiskDriver driver(long runId, int rank, String driverCode, String title) {
    RiskDriver d = new RiskDriver();
    d.setForecastRunId(runId);
    d.setRankNo(rank);
    d.setDriverCode(driverCode);
    d.setTitle(title);
    return d;
  }

  private void evidence(long riskDriverId, String refType, String label, String periodText) {
    RiskDriverEvidence e = new RiskDriverEvidence();
    e.setRiskDriverId(riskDriverId);
    e.setRefType(refType);
    e.setLabel(label);
    e.setPeriodText(periodText);
    evidenceRepository.save(e);
  }

  private void coverage(long runId, String sourceType, String rate, boolean below) {
    ForecastCoverage c = new ForecastCoverage();
    c.setForecastRunId(runId);
    c.setSourceType(sourceType);
    c.setCoverageRate(new BigDecimal(rate));
    c.setLastSyncedAt(UPDATED_AT);
    c.setBelowThreshold(below);
    coverageRepository.save(c);
  }

  private void narrative(long runId, String kind, int seq, String text) {
    ForecastRunNarrative n = new ForecastRunNarrative();
    n.setForecastRunId(runId);
    n.setKind(kind);
    n.setSeq(seq);
    n.setText(text);
    narrativeRepository.save(n);
  }
}
