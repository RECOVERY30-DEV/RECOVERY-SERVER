package recovery30.server.supportprogram.getsupportprogram;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;
import recovery30.server.supportprogram.domain.SupportProgram;
import recovery30.server.supportprogram.domain.SupportProgramStatus;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

/** '지원제도 상세 조회' 슬라이스. 지원사업 상세 화면 "지원 개요". */
@RestController
@RequestMapping("/api/support-programs")
@Tag(name = "SupportProgram", description = "지원제도 (목록·상세·필요서류·자격판정·추천)")
public class GetSupportProgramHandler {

  private final SupportProgramRepository supportProgramRepository;

  public GetSupportProgramHandler(SupportProgramRepository supportProgramRepository) {
    this.supportProgramRepository = supportProgramRepository;
  }

  @Operation(summary = "지원제도 상세 조회", description = "지원제도 코드로 개요·금리·기간·신청기한·공식 출처를 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 지원제도",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{programCode}")
  public ResponseEntity<ApiResponse<SupportProgramDetailView>> handle(
      @Parameter(description = "지원제도 코드", example = "SBIZ_STABLE_FUND") @PathVariable
          String programCode) {
    SupportProgram p =
        supportProgramRepository
            .findByProgramCode(programCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SUPPORT_PROGRAM_NOT_FOUND));

    SupportProgramDetailView view =
        new SupportProgramDetailView(
            p.getId(),
            p.getProgramCode(),
            p.getName(),
            p.getAgency(),
            p.getSupportContent(),
            p.getLimitAmount(),
            p.getInterestRateText(),
            p.getTermText(),
            p.getApplyDeadline(),
            p.getApplyUrl(),
            p.getOfficialSourceUrl(),
            p.getRulesetVersion(),
            SupportProgramStatus.valueOf(p.getStatus()));
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
