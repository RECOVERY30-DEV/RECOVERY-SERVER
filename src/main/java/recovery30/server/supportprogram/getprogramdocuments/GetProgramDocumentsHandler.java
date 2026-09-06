package recovery30.server.supportprogram.getprogramdocuments;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
import recovery30.server.supportprogram.internal.ProgramDocumentRepository;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

/** '지원제도 필요서류 조회' 슬라이스. 지원사업 상세 화면 "필요서류". */
@RestController
@RequestMapping("/api/support-programs")
@Tag(name = "SupportProgram", description = "지원제도 (목록·상세·필요서류·자격판정·추천)")
public class GetProgramDocumentsHandler {

  private final SupportProgramRepository supportProgramRepository;
  private final ProgramDocumentRepository programDocumentRepository;

  public GetProgramDocumentsHandler(
      SupportProgramRepository supportProgramRepository,
      ProgramDocumentRepository programDocumentRepository) {
    this.supportProgramRepository = supportProgramRepository;
    this.programDocumentRepository = programDocumentRepository;
  }

  @Operation(summary = "지원제도 필요서류 조회", description = "지원제도 코드로 신청 시 필요한 서류 목록을 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 지원제도",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{programCode}/documents")
  public ResponseEntity<ApiResponse<List<ProgramDocumentView>>> handle(
      @Parameter(description = "지원제도 코드", example = "SBIZ_STABLE_FUND") @PathVariable
          String programCode) {
    SupportProgram program =
        supportProgramRepository
            .findByProgramCode(programCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SUPPORT_PROGRAM_NOT_FOUND));

    List<ProgramDocumentView> views =
        programDocumentRepository.findByProgramIdOrderByIdAsc(program.getId()).stream()
            .map(
                d ->
                    new ProgramDocumentView(
                        d.getId(), d.getName(), d.getDescription(), d.isRequired()))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
