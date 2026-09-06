package recovery30.server.supportprogram.getprogrameligibility;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.supportprogram.SupportProgramFixtures;
import recovery30.server.supportprogram.domain.ProgramEligibilityCheck;
import recovery30.server.supportprogram.domain.ProgramEligibilityCheckItem;
import recovery30.server.supportprogram.domain.ProgramEligibilityRule;
import recovery30.server.supportprogram.domain.SupportProgram;
import recovery30.server.supportprogram.internal.ProgramEligibilityCheckItemRepository;
import recovery30.server.supportprogram.internal.ProgramEligibilityCheckRepository;
import recovery30.server.supportprogram.internal.ProgramEligibilityRuleRepository;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetProgramEligibilityHandlerTest {

  private static final long BIZ = 1L;

  @Autowired private MockMvc mockMvc;
  @Autowired private SupportProgramRepository supportProgramRepository;
  @Autowired private ProgramEligibilityRuleRepository ruleRepository;
  @Autowired private ProgramEligibilityCheckRepository checkRepository;
  @Autowired private ProgramEligibilityCheckItemRepository checkItemRepository;

  private SupportProgram seedProgramWithRules() {
    SupportProgram p =
        supportProgramRepository.save(
            SupportProgramFixtures.program(
                "SBIZ_STABLE_FUND", "경영안정자금", "공단", LocalDate.of(2025, 7, 31), "ACTIVE"));
    ruleRepository.save(
        SupportProgramFixtures.rule(p.getId(), "BIZ_AGE_1Y", "사업자등록 1년 이상", "AUTO"));
    ruleRepository.save(
        SupportProgramFixtures.rule(p.getId(), "NO_DELINQUENCY", "금융기관 연체 없음", "COUNSELOR_ONLY"));
    return p;
  }

  @Test
  void 판정_이력이_있으면_규칙별과_종합결과를_반환한다() throws Exception {
    SupportProgram p = seedProgramWithRules();
    ProgramEligibilityRule bizAge = ruleRepository.findByProgramIdOrderByIdAsc(p.getId()).get(0);

    ProgramEligibilityCheck check = new ProgramEligibilityCheck();
    check.setBusinessId(BIZ);
    check.setProgramId(p.getId());
    check.setResult("NEEDS_REVIEW");
    check.setReasonText("연체 여부는 상담자 확인이 필요합니다.");
    check.setAdvisory(true);
    check.setRulesetVersion("rule-2025-06");
    check.setCheckedAt(Instant.parse("2025-07-15T00:00:00Z"));
    check = checkRepository.save(check);

    ProgramEligibilityCheckItem item = new ProgramEligibilityCheckItem();
    item.setCheckId(check.getId());
    item.setRuleId(bizAge.getId());
    item.setResult("LIKELY_PASS");
    item.setNoteText("등록일 기준 충족 가능성 높음");
    item.setAdvisory(true);
    checkItemRepository.save(item);

    mockMvc
        .perform(
            get(
                "/api/businesses/{businessId}/support-programs/{programCode}/eligibility",
                BIZ,
                "SBIZ_STABLE_FUND"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.result").value("NEEDS_REVIEW"))
        .andExpect(jsonPath("$.data.advisory").value(true))
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].ruleCode").value("BIZ_AGE_1Y"))
        .andExpect(jsonPath("$.data.items[0].result").value("LIKELY_PASS"))
        .andExpect(jsonPath("$.data.items[0].noteText").value("등록일 기준 충족 가능성 높음"))
        .andExpect(jsonPath("$.data.items[1].result").value("UNKNOWN"));
  }

  @Test
  void 판정_이력이_없으면_규칙은_UNKNOWN이고_종합도_UNKNOWN이다() throws Exception {
    seedProgramWithRules();

    mockMvc
        .perform(
            get(
                "/api/businesses/{businessId}/support-programs/{programCode}/eligibility",
                BIZ,
                "SBIZ_STABLE_FUND"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.result").value("UNKNOWN"))
        .andExpect(jsonPath("$.data.checkedAt").doesNotExist())
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].result").value("UNKNOWN"))
        .andExpect(jsonPath("$.data.items[1].evaluationType").value("COUNSELOR_ONLY"));
  }

  @Test
  void 존재하지_않는_코드면_404를_반환한다() throws Exception {
    mockMvc
        .perform(
            get(
                "/api/businesses/{businessId}/support-programs/{programCode}/eligibility",
                BIZ,
                "NOPE"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("SUPPORT_404_1"));
  }
}
