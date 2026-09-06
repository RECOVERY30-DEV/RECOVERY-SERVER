package recovery30.server.recoveryoption.putoptionselections;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.recoveryoption.domain.UserOptionSelection;
import recovery30.server.recoveryoption.internal.RecoveryOptionRepository;
import recovery30.server.recoveryoption.internal.UserOptionSelectionRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '회복안 선택 저장' 슬라이스. 회복안 비교 화면 "회복 옵션 선택 (최대 2개)". 선택 집합 전체를 교체한다(PUT). */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "RecoveryOption", description = "회복안 비교 (회복안·시나리오·선택)")
public class PutOptionSelectionsHandler {

  private static final int MAX_SELECTIONS = 2;

  private final ForecastApi forecastApi;
  private final RecoveryOptionRepository recoveryOptionRepository;
  private final UserOptionSelectionRepository userOptionSelectionRepository;

  public PutOptionSelectionsHandler(
      ForecastApi forecastApi,
      RecoveryOptionRepository recoveryOptionRepository,
      UserOptionSelectionRepository userOptionSelectionRepository) {
    this.forecastApi = forecastApi;
    this.recoveryOptionRepository = recoveryOptionRepository;
    this.userOptionSelectionRepository = userOptionSelectionRepository;
  }

  @Operation(
      summary = "회복안 선택 저장",
      description = "해당 예측 실행의 회복안 선택을 요청한 목록으로 통째로 교체한다. 최대 2개(앱 레벨 제한). 빈 배열이면 전체 해제.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "저장 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "선택 개수 초과(최대 2개)",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행 또는 회복안",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping("/{forecastRunId}/option-selections")
  @Transactional
  public ResponseEntity<ApiResponse<OptionSelectionsView>> handle(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId,
      @RequestBody PutOptionSelectionsCommand command) {
    List<Long> optionIds =
        command.optionIds() == null
            ? List.of()
            : command.optionIds().stream().filter(java.util.Objects::nonNull).distinct().toList();

    if (optionIds.size() > MAX_SELECTIONS) {
      throw new BusinessException(ErrorCode.RECOVERY_OPTION_SELECTION_LIMIT);
    }
    if (!forecastApi.forecastRunExists(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }
    if (!optionIds.isEmpty()
        && recoveryOptionRepository.countByIdIn(optionIds) != optionIds.size()) {
      throw new BusinessException(ErrorCode.RECOVERY_OPTION_NOT_FOUND);
    }

    userOptionSelectionRepository.deleteByForecastRunId(forecastRunId);
    for (Long optionId : optionIds) {
      UserOptionSelection selection = new UserOptionSelection();
      selection.setForecastRunId(forecastRunId);
      selection.setRecoveryOptionId(optionId);
      selection.setSelectedAt(Instant.now());
      userOptionSelectionRepository.save(selection);
    }

    return ResponseEntity.ok(ApiResponse.success(new OptionSelectionsView(optionIds)));
  }
}
