package recovery30.server.followup.getfollowupresult;

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
import recovery30.server.followup.domain.FollowupResult;
import recovery30.server.followup.internal.FollowupResultRepository;
import recovery30.server.followup.internal.FollowupScheduleRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '사후 점검 결과 조회' 슬라이스. 사후점검 화면 "잔액 회복 현황". */
@RestController
@RequestMapping("/api/followups")
@Tag(name = "Followup", description = "사후점검 (D30/60/90 점검 일정·결과·회복안 실행 상태)")
public class GetFollowupResultHandler {

  private final FollowupScheduleRepository scheduleRepository;
  private final FollowupResultRepository resultRepository;

  public GetFollowupResultHandler(
      FollowupScheduleRepository scheduleRepository, FollowupResultRepository resultRepository) {
    this.scheduleRepository = scheduleRepository;
    this.resultRepository = resultRepository;
  }

  @Operation(
      summary = "사후 점검 결과 조회",
      description = "점검 일정 1건의 결과(잔액 회복 여부·회복 금액·위험 상태)를 반환한다. 아직 결과가 없으면 404.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 점검 일정 또는 아직 기록된 결과가 없음",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{scheduleId}/result")
  public ResponseEntity<ApiResponse<FollowupResultView>> handle(
      @Parameter(description = "점검 일정 ID", example = "1") @PathVariable Long scheduleId) {
    if (!scheduleRepository.existsById(scheduleId)) {
      throw new BusinessException(ErrorCode.FOLLOWUP_NOT_FOUND);
    }
    FollowupResult result =
        resultRepository
            .findByFollowupScheduleId(scheduleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOWUP_RESULT_NOT_FOUND));
    return ResponseEntity.ok(ApiResponse.success(FollowupResultView.of(result)));
  }
}
