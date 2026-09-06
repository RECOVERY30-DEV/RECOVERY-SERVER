package recovery30.server.followup.getexecutionstatus;

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
import recovery30.server.business.api.BusinessApi;
import recovery30.server.followup.internal.RecoveryExecutionStatusRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '회복안 실행 상태 목록' 슬라이스. 사후점검 화면 "회복안 실행 상태". */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Followup", description = "사후점검 (D30/60/90 점검 일정·결과·회복안 실행 상태)")
public class GetExecutionStatusHandler {

  private final BusinessApi businessApi;
  private final RecoveryExecutionStatusRepository executionStatusRepository;

  public GetExecutionStatusHandler(
      BusinessApi businessApi, RecoveryExecutionStatusRepository executionStatusRepository) {
    this.businessApi = businessApi;
    this.executionStatusRepository = executionStatusRepository;
  }

  @Operation(
      summary = "회복안 실행 상태 목록 조회",
      description = "사업자가 실행하기로 한 회복안별 진행 상태(진행중·완료·장애요인 등)를 반환한다. 없으면 빈 배열.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 사업자",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{businessId}/recovery-execution-status")
  public ResponseEntity<ApiResponse<List<ExecutionStatusView>>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId) {
    if (!businessApi.businessExists(businessId)) {
      throw new BusinessException(ErrorCode.BUSINESS_NOT_FOUND);
    }
    List<ExecutionStatusView> views =
        executionStatusRepository.findByBusinessIdOrderByIdAsc(businessId).stream()
            .map(ExecutionStatusView::of)
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
