package recovery30.server.supportprogram.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import recovery30.server.supportprogram.domain.ProgramEligibilityCheck;
import recovery30.server.supportprogram.domain.ProgramEligibilityCheckItem;
import recovery30.server.supportprogram.domain.ProgramEligibilityRule;
import recovery30.server.supportprogram.domain.ProgramRecommendation;
import recovery30.server.supportprogram.domain.SupportProgram;

/**
 * demo 프로파일에서 지원사업 목록/상세 화면의 "판정 결과"와 "추천"을 주입한다.
 *
 * <p>지원제도 카탈로그·자격요건 규칙·필요서류는 Flyway V14 가 모든 환경에 적재한다. 여기서는 그 위에 QA-RISK 페르소나의 추천({@code
 * recovery_program_recommendations})과 자격 판정 결과({@code recovery_program_eligibility_checks} + {@code
 * _check_items})만 채운다. V14 가 없는(테스트) 환경이면 조용히 건너뛴다.
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(4)
public class DemoSupportProgramSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoSupportProgramSeeder.class);
  private static final Instant CHECKED_AT = Instant.parse("2025-07-15T00:00:00Z");

  private final BusinessApi businessApi;
  private final ForecastApi forecastApi;
  private final SupportProgramRepository programRepository;
  private final ProgramRecommendationRepository recommendationRepository;
  private final ProgramEligibilityRuleRepository ruleRepository;
  private final ProgramEligibilityCheckRepository checkRepository;
  private final ProgramEligibilityCheckItemRepository checkItemRepository;

  public DemoSupportProgramSeeder(
      BusinessApi businessApi,
      ForecastApi forecastApi,
      SupportProgramRepository programRepository,
      ProgramRecommendationRepository recommendationRepository,
      ProgramEligibilityRuleRepository ruleRepository,
      ProgramEligibilityCheckRepository checkRepository,
      ProgramEligibilityCheckItemRepository checkItemRepository) {
    this.businessApi = businessApi;
    this.forecastApi = forecastApi;
    this.programRepository = programRepository;
    this.recommendationRepository = recommendationRepository;
    this.ruleRepository = ruleRepository;
    this.checkRepository = checkRepository;
    this.checkItemRepository = checkItemRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (programRepository.findByProgramCode("SBIZ_STABLE_FUND").isEmpty()) {
      log.warn("[demo] 지원제도 카탈로그(V14)가 없어 추천/판정 시딩을 건너뜁니다");
      return;
    }
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

    seedRecommendations(runId);
    seedEligibility(businessId);
  }

  private void seedRecommendations(long runId) {
    if (!recommendationRepository.findByForecastRunIdOrderByRankNoAsc(runId).isEmpty()) {
      return;
    }
    recommend(runId, 1, "SBIZ_STABLE_FUND", "최근 6개월 매출 감소·사업자 2년 이상·신용등급 조건 확인 필요");
    recommend(runId, 2, "SBIZ_119PLUS", "경영애로 소상공인 저금리 대환 대상 가능 · 현금흐름 데이터 반영");
    recommend(runId, 3, "SUNSHINE_119", "연체 우려 차주 대상 · 지역·소득 요건 별도 확인 필요");
    log.info("[demo] QA-RISK run {} 지원제도 추천 3건 시딩", runId);
  }

  private void recommend(long runId, int rank, String programCode, String matchReason) {
    programRepository
        .findByProgramCode(programCode)
        .ifPresent(
            p -> {
              ProgramRecommendation r = new ProgramRecommendation();
              r.setForecastRunId(runId);
              r.setProgramId(p.getId());
              r.setRankNo(rank);
              r.setMatchReason(matchReason);
              r.setCreatedAt(CHECKED_AT);
              recommendationRepository.save(r);
            });
  }

  private void seedEligibility(long businessId) {
    SupportProgram program = programRepository.findByProgramCode("SBIZ_STABLE_FUND").orElseThrow();
    if (checkRepository.findByBusinessIdAndProgramId(businessId, program.getId()).isPresent()) {
      return;
    }

    ProgramEligibilityCheck check = new ProgramEligibilityCheck();
    check.setBusinessId(businessId);
    check.setProgramId(program.getId());
    check.setResult("NEEDS_REVIEW");
    check.setReasonText("최근 8주 매출 감소 패턴이 지원 대상 조건과 유사하나, 금융기관 연체 여부는 상담자 확인이 필요합니다.");
    check.setAdvisory(true);
    check.setRulesetVersion("rule-2025-06");
    check.setCheckedAt(CHECKED_AT);
    check = checkRepository.save(check);

    Map<String, String[]> byRuleCode =
        Map.of(
            "BIZ_AGE_1Y", new String[] {"LIKELY_PASS", "등록일 기준 충족 가능성 높음"},
            "REVENUE_1B", new String[] {"LIKELY_PASS", "최근 매출 데이터 기준 해당 가능"},
            "NO_DELINQUENCY", new String[] {"NEEDS_REVIEW", "확인 필요 — 상담자가 최종 판단합니다"},
            "INDUSTRY_ALLOWED", new String[] {"LIKELY_PASS", "현재 업종 코드 기준 해당 없음"});

    List<ProgramEligibilityRule> rules =
        ruleRepository.findByProgramIdOrderByIdAsc(program.getId());
    for (ProgramEligibilityRule rule : rules) {
      String[] rc = byRuleCode.get(rule.getRuleCode());
      if (rc == null) {
        continue;
      }
      ProgramEligibilityCheckItem item = new ProgramEligibilityCheckItem();
      item.setCheckId(check.getId());
      item.setRuleId(rule.getId());
      item.setResult(rc[0]);
      item.setNoteText(rc[1]);
      item.setAdvisory(true);
      checkItemRepository.save(item);
    }
    log.info("[demo] business {} × SBIZ_STABLE_FUND 자격 판정 + 항목 {}건 시딩", businessId, rules.size());
  }
}
