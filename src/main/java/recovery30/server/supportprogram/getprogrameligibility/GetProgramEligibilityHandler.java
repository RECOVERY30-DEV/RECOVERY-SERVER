package recovery30.server.supportprogram.getprogrameligibility;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;
import recovery30.server.supportprogram.domain.EligibilityEvaluationType;
import recovery30.server.supportprogram.domain.EligibilityResult;
import recovery30.server.supportprogram.domain.ProgramEligibilityCheck;
import recovery30.server.supportprogram.domain.ProgramEligibilityCheckItem;
import recovery30.server.supportprogram.domain.ProgramEligibilityRule;
import recovery30.server.supportprogram.domain.SupportProgram;
import recovery30.server.supportprogram.getprogrameligibility.EligibilityView.RuleResultView;
import recovery30.server.supportprogram.internal.ProgramEligibilityCheckItemRepository;
import recovery30.server.supportprogram.internal.ProgramEligibilityCheckRepository;
import recovery30.server.supportprogram.internal.ProgramEligibilityRuleRepository;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

/** '지원제도 자격 판정 조회' 슬라이스. 지원사업 상세 화면 "자격요건 확인". 항상 참고용(advisory). */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "SupportProgram", description = "지원제도 (목록·상세·필요서류·자격판정·추천)")
public class GetProgramEligibilityHandler {

  private final SupportProgramRepository supportProgramRepository;
  private final ProgramEligibilityRuleRepository ruleRepository;
  private final ProgramEligibilityCheckRepository checkRepository;
  private final ProgramEligibilityCheckItemRepository checkItemRepository;

  public GetProgramEligibilityHandler(
      SupportProgramRepository supportProgramRepository,
      ProgramEligibilityRuleRepository ruleRepository,
      ProgramEligibilityCheckRepository checkRepository,
      ProgramEligibilityCheckItemRepository checkItemRepository) {
    this.supportProgramRepository = supportProgramRepository;
    this.ruleRepository = ruleRepository;
    this.checkRepository = checkRepository;
    this.checkItemRepository = checkItemRepository;
  }

  @Operation(
      summary = "지원제도 자격 판정 조회",
      description =
          "규칙 항목별 + 종합 자격 판정을 반환한다. 예측/프로필 데이터 기반 추정이며 자동 자격판정이 아니다(advisory=true). 판정 이력이 없으면"
              + " 규칙 목록만 UNKNOWN 으로 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 지원제도",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{businessId}/support-programs/{programCode}/eligibility")
  public ResponseEntity<ApiResponse<EligibilityView>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @Parameter(description = "지원제도 코드", example = "SBIZ_STABLE_FUND") @PathVariable
          String programCode) {
    SupportProgram program =
        supportProgramRepository
            .findByProgramCode(programCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SUPPORT_PROGRAM_NOT_FOUND));

    List<ProgramEligibilityRule> rules =
        ruleRepository.findByProgramIdOrderByIdAsc(program.getId());
    ProgramEligibilityCheck check =
        checkRepository.findByBusinessIdAndProgramId(businessId, program.getId()).orElse(null);

    Map<Long, ProgramEligibilityCheckItem> itemByRuleId =
        check == null
            ? Map.of()
            : checkItemRepository.findByCheckIdOrderByIdAsc(check.getId()).stream()
                .collect(
                    Collectors.toMap(
                        ProgramEligibilityCheckItem::getRuleId, Function.identity(), (a, b) -> a));

    List<RuleResultView> items =
        rules.stream()
            .map(
                r -> {
                  ProgramEligibilityCheckItem item = itemByRuleId.get(r.getId());
                  return new RuleResultView(
                      r.getRuleCode(),
                      r.getLabel(),
                      EligibilityEvaluationType.valueOf(r.getEvaluationType()),
                      item == null
                          ? EligibilityResult.UNKNOWN
                          : EligibilityResult.valueOf(item.getResult()),
                      item == null ? null : item.getNoteText());
                })
            .toList();

    EligibilityView view =
        new EligibilityView(
            programCode,
            check == null
                ? EligibilityResult.UNKNOWN
                : EligibilityResult.valueOf(check.getResult()),
            check == null ? null : check.getReasonText(),
            true,
            check == null ? program.getRulesetVersion() : check.getRulesetVersion(),
            check == null ? null : check.getCheckedAt(),
            items);
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
