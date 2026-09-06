package recovery30.server.followup.getfollowups;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.business.api.BusinessApi;
import recovery30.server.followup.domain.FollowupResult;
import recovery30.server.followup.domain.FollowupSchedule;
import recovery30.server.followup.internal.FollowupResultRepository;
import recovery30.server.followup.internal.FollowupScheduleRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '사후 점검 일정 목록' 슬라이스. 사후점검 화면 진입점 + Recovery Packet "사후 점검 일정". */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Followup", description = "사후점검 (D30/60/90 점검 일정·결과·회복안 실행 상태)")
public class GetFollowupsHandler {

  private final BusinessApi businessApi;
  private final FollowupScheduleRepository scheduleRepository;
  private final FollowupResultRepository resultRepository;

  public GetFollowupsHandler(
      BusinessApi businessApi,
      FollowupScheduleRepository scheduleRepository,
      FollowupResultRepository resultRepository) {
    this.businessApi = businessApi;
    this.scheduleRepository = scheduleRepository;
    this.resultRepository = resultRepository;
  }

  @Operation(
      summary = "사후 점검 일정 목록 조회",
      description = "사업자의 D30/D60/D90 점검 일정을 예정일 순으로 반환한다. 아직 일정이 없으면 빈 배열.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 사업자",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{businessId}/followups")
  public ResponseEntity<ApiResponse<List<FollowupView>>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId) {
    if (!businessApi.businessExists(businessId)) {
      throw new BusinessException(ErrorCode.BUSINESS_NOT_FOUND);
    }
    List<FollowupSchedule> schedules =
        scheduleRepository.findByBusinessIdOrderByScheduledDateAsc(businessId);
    Set<Long> withResult =
        schedules.isEmpty()
            ? Set.of()
            : resultRepository
                .findByFollowupScheduleIdIn(
                    schedules.stream().map(FollowupSchedule::getId).toList())
                .stream()
                .map(FollowupResult::getFollowupScheduleId)
                .collect(Collectors.toSet());

    List<FollowupView> views =
        schedules.stream().map(s -> FollowupView.of(s, withResult.contains(s.getId()))).toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
