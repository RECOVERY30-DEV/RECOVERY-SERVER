package recovery30.server.supportprogram.getsupportprograms;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.shared.response.ApiResponse;
import recovery30.server.supportprogram.domain.SupportProgram;
import recovery30.server.supportprogram.domain.SupportProgramStatus;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

/** '지원제도 목록 조회' 슬라이스. 지원사업 목록 화면. */
@RestController
@RequestMapping("/api/support-programs")
@Tag(name = "SupportProgram", description = "지원제도 (목록·상세·필요서류·자격판정·추천)")
public class GetSupportProgramsHandler {

  private final SupportProgramRepository supportProgramRepository;

  public GetSupportProgramsHandler(SupportProgramRepository supportProgramRepository) {
    this.supportProgramRepository = supportProgramRepository;
  }

  @Operation(
      summary = "지원제도 목록 조회",
      description =
          "신청기한 임박순으로 정렬된 지원제도 목록. applicableOnly=true 면 ACTIVE + 마감일 미도래 항목만 반환한다. Match 근거는"
              + " forecasts/{runId}/program-recommendations 로 별도 조회해 병합한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (없으면 빈 배열)")
  })
  @GetMapping
  public ResponseEntity<ApiResponse<List<SupportProgramSummaryView>>> handle(
      @Parameter(description = "신청 가능한 제도만", example = "true") @RequestParam(defaultValue = "false")
          boolean applicableOnly) {
    List<SupportProgram> programs =
        applicableOnly
            ? supportProgramRepository.findApplicable(LocalDate.now())
            : supportProgramRepository.findAllByOrderByApplyDeadlineAscIdAsc();

    List<SupportProgramSummaryView> views =
        programs.stream()
            .map(
                p ->
                    new SupportProgramSummaryView(
                        p.getId(),
                        p.getProgramCode(),
                        p.getName(),
                        p.getAgency(),
                        p.getSupportContent(),
                        p.getLimitAmount(),
                        p.getInterestRateText(),
                        p.getApplyDeadline(),
                        SupportProgramStatus.valueOf(p.getStatus())))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
