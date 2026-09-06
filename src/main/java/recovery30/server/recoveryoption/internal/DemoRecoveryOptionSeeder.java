package recovery30.server.recoveryoption.internal;

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
import recovery30.server.recoveryoption.domain.RecoveryOption;
import recovery30.server.recoveryoption.domain.Scenario;
import recovery30.server.recoveryoption.domain.ScenarioOption;

/**
 * demo 프로파일에서 회복안 비교 화면 데이터를 주입한다.
 *
 * <ul>
 *   <li>회복안 카탈로그({@code recovery_options}) : 운영/QA 는 Flyway V16 가 적재. Flyway 가 없는 테스트 환경을 위해 비어 있으면
 *       여기서 동일 5건을 채운다(멱등).
 *   <li>QA-RISK 예측 실행의 시나리오({@code recovery_scenarios} + {@code recovery_scenario_options}) :
 *       BASELINE 1 + SIMULATED 2.
 * </ul>
 *
 * 사업자·예측은 @Order 1·2 시더가 먼저 만들고 여기서는 BusinessApi / ForecastApi 로 id 만 조회한다.
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(3)
public class DemoRecoveryOptionSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoRecoveryOptionSeeder.class);

  private final BusinessApi businessApi;
  private final ForecastApi forecastApi;
  private final RecoveryOptionRepository optionRepository;
  private final ScenarioRepository scenarioRepository;
  private final ScenarioOptionRepository scenarioOptionRepository;

  public DemoRecoveryOptionSeeder(
      BusinessApi businessApi,
      ForecastApi forecastApi,
      RecoveryOptionRepository optionRepository,
      ScenarioRepository scenarioRepository,
      ScenarioOptionRepository scenarioOptionRepository) {
    this.businessApi = businessApi;
    this.forecastApi = forecastApi;
    this.optionRepository = optionRepository;
    this.scenarioRepository = scenarioRepository;
    this.scenarioOptionRepository = scenarioOptionRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedCatalogIfEmpty();
    seedRiskScenarios();
  }

  private void seedCatalogIfEmpty() {
    if (optionRepository.count() > 0) {
      return;
    }
    optionRepository.save(
        option(
            "REPAYMENT_ADJUST",
            "FINANCIAL_CONSULT",
            "MID",
            true,
            "부족일 최대 16일 연장 가능",
            "월 상환액 약 15만 원 감소 예상",
            "원리금 3회 이상 정상 납부 이력",
            "승인 여부와 조건은 금융기관 심사 결과에 따릅니다."));
    optionRepository.save(
        option(
            "DUEDATE_SHIFT",
            "SELF_ACTION",
            "LOW",
            false,
            "월말 집중 부담 분산",
            "총액 변화 없음, 시기 조정",
            "임대인·기관과 납부일 협의 가능 여부",
            "납부일 변경 가능 여부는 계약 및 기관 방침에 따릅니다."));
    optionRepository.save(
        option(
            "POLICY_FUND_LINK",
            "SUPPORT_PROGRAM",
            "HIGH",
            true,
            "부족액 일부 보완 가능",
            "신규 상환 발생 (한도·금리 미확정)",
            "자격 요건 별도 확인 필요",
            "자격 요건과 한도는 공식 출처 및 상담자 확인이 필요합니다."));
    optionRepository.save(
        option(
            "RATE_CUT_REFINANCE",
            "FINANCIAL_CONSULT",
            "MID",
            true,
            "이자 부담 경감 가능",
            "금리·한도 미확정, 상담 필요",
            "신용 상태·거래 이력 기반 검토",
            "금리 인하 및 대환 승인은 금융기관 심사 결과에 따릅니다."));
    optionRepository.save(
        option(
            "BIZ_DIAGNOSIS",
            "SUPPORT_PROGRAM",
            "LOW",
            false,
            "중장기 매출 회복 가능성",
            "단기 현금흐름 직접 효과 낮음",
            "지역·업종별 지원 기관 확인 필요",
            "단기 부족 해소보다 중장기 회복을 목표로 하는 옵션입니다."));
    log.info("[demo] recovery_options 카탈로그 5건 시딩 (Flyway V16 미적용 환경)");
  }

  private void seedRiskScenarios() {
    Long businessId = businessApi.findBusinessIdByRegNo("QA-RISK").orElse(null);
    if (businessId == null) {
      log.warn("[demo] QA-RISK 사업자가 없습니다 — DemoBusinessSeeder 확인");
      return;
    }
    Long runId = forecastApi.findLatestForecastRunId(businessId).orElse(null);
    if (runId == null) {
      log.warn("[demo] QA-RISK 예측 실행이 없습니다 — DemoForecastSeeder 확인");
      return;
    }
    if (!scenarioRepository.findByForecastRunIdOrderByIdAsc(runId).isEmpty()) {
      log.info("[demo] run {} 시나리오가 이미 있어 건너뜁니다", runId);
      return;
    }

    Long repaymentAdjustId =
        optionRepository
            .findByOptionCode("REPAYMENT_ADJUST")
            .map(RecoveryOption::getId)
            .orElse(null);
    Long dueDateShiftId =
        optionRepository.findByOptionCode("DUEDATE_SHIFT").map(RecoveryOption::getId).orElse(null);

    // BASELINE — 아무 조치 없음
    Scenario baseline = new Scenario();
    baseline.setForecastRunId(runId);
    baseline.setScenarioType("BASELINE");
    baseline.setFirstShortfallDate(LocalDate.of(2025, 7, 26));
    baseline.setMinBalance(-1_240_000L);
    baseline.setNote("현재 데이터 기반 기준 시나리오입니다.");
    scenarioRepository.save(baseline);

    // SIMULATED — 상환조건 조정 상담
    Scenario s1 = new Scenario();
    s1.setForecastRunId(runId);
    s1.setScenarioType("SIMULATED");
    s1.setFirstShortfallDate(LocalDate.of(2025, 8, 11));
    s1.setMinBalance(-630_000L);
    s1.setDeltaDays(16);
    s1.setDeltaMinBalance(610_000L);
    s1.setMonthlyPaymentDelta(-150_000L);
    s1.setNote("상담 및 심사 결과에 따라 실제 효과는 달라질 수 있습니다.");
    s1 = scenarioRepository.save(s1);
    if (repaymentAdjustId != null) {
      scenarioOptionRepository.save(scenarioOption(s1.getId(), repaymentAdjustId));
    }

    // SIMULATED — 고정비 납부일 재배치
    Scenario s2 = new Scenario();
    s2.setForecastRunId(runId);
    s2.setScenarioType("SIMULATED");
    s2.setFirstShortfallDate(LocalDate.of(2025, 8, 4));
    s2.setMinBalance(-860_000L);
    s2.setDeltaDays(9);
    s2.setDeltaMinBalance(380_000L);
    s2.setMonthlyPaymentDelta(0L);
    s2.setNote("납부일 변경은 기관 협의 후 실제 반영됩니다.");
    s2 = scenarioRepository.save(s2);
    if (dueDateShiftId != null) {
      scenarioOptionRepository.save(scenarioOption(s2.getId(), dueDateShiftId));
    }

    log.info("[demo] QA-RISK run {} 시나리오 3건(BASELINE + SIMULATED 2) 시딩", runId);
  }

  private static RecoveryOption option(
      String code,
      String category,
      String difficulty,
      boolean requiresReview,
      String effect,
      String burden,
      String precondition,
      String disclaimer) {
    RecoveryOption o = new RecoveryOption();
    o.setOptionCode(code);
    o.setCategory(category);
    o.setDifficulty(difficulty);
    o.setRequiresReview(requiresReview);
    o.setExpectedEffectText(effect);
    o.setMonthlyBurdenChangeText(burden);
    o.setPreconditionText(precondition);
    o.setDisclaimer(disclaimer);
    return o;
  }

  private static ScenarioOption scenarioOption(long scenarioId, long recoveryOptionId) {
    ScenarioOption so = new ScenarioOption();
    so.setScenarioId(scenarioId);
    so.setRecoveryOptionId(recoveryOptionId);
    return so;
  }
}
