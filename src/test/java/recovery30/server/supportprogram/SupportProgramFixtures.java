package recovery30.server.supportprogram;

import java.time.LocalDate;
import recovery30.server.supportprogram.domain.ProgramDocument;
import recovery30.server.supportprogram.domain.ProgramEligibilityRule;
import recovery30.server.supportprogram.domain.ProgramRecommendation;
import recovery30.server.supportprogram.domain.SupportProgram;

/** supportprogram 슬라이스 통합테스트용 엔티티 픽스처. */
public final class SupportProgramFixtures {

  private SupportProgramFixtures() {}

  public static SupportProgram program(
      String code, String name, String agency, LocalDate deadline, String status) {
    SupportProgram p = new SupportProgram();
    p.setProgramCode(code);
    p.setName(name);
    p.setAgency(agency);
    p.setSupportContent("운전자금 최대 2,000만 원 융자");
    p.setLimitAmount(20_000_000L);
    p.setInterestRateText("연 3.4% (고정, 상담자 확인 필요)");
    p.setTermText("3년 거치 5년 분할상환");
    p.setApplyDeadline(deadline);
    p.setApplyUrl("https://www.sbiz.or.kr");
    p.setOfficialSourceUrl("https://www.sbiz.or.kr/notice/2025-1");
    p.setRulesetVersion("rule-2025-06");
    p.setStatus(status);
    return p;
  }

  public static ProgramDocument document(long programId, String name, boolean required) {
    ProgramDocument d = new ProgramDocument();
    d.setProgramId(programId);
    d.setName(name);
    d.setDescription("최근 발급본");
    d.setRequired(required);
    return d;
  }

  public static ProgramEligibilityRule rule(
      long programId, String ruleCode, String label, String evaluationType) {
    ProgramEligibilityRule r = new ProgramEligibilityRule();
    r.setProgramId(programId);
    r.setRuleCode(ruleCode);
    r.setLabel(label);
    r.setEvaluationType(evaluationType);
    return r;
  }

  public static ProgramRecommendation recommendation(
      long forecastRunId, long programId, int rank, String matchReason) {
    ProgramRecommendation r = new ProgramRecommendation();
    r.setForecastRunId(forecastRunId);
    r.setProgramId(programId);
    r.setRankNo(rank);
    r.setMatchReason(matchReason);
    r.setCreatedAt(java.time.Instant.parse("2025-07-15T00:00:00Z"));
    return r;
  }
}
