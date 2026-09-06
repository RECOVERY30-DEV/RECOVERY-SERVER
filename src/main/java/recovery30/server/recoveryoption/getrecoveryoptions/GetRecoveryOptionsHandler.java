package recovery30.server.recoveryoption.getrecoveryoptions;

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
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.recoveryoption.domain.RecoveryOption;
import recovery30.server.recoveryoption.domain.RecoveryOptionCategory;
import recovery30.server.recoveryoption.domain.RecoveryOptionDifficulty;
import recovery30.server.recoveryoption.internal.RecoveryOptionRepository;
import recovery30.server.recoveryoption.internal.UserOptionSelectionRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '회복안 목록 조회' 슬라이스. 회복안 비교 화면 "회복 옵션 선택 (최대 2개)". */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "RecoveryOption", description = "회복안 비교 (회복안·시나리오·선택)")
public class GetRecoveryOptionsHandler {

  private final ForecastApi forecastApi;
  private final RecoveryOptionRepository recoveryOptionRepository;
  private final UserOptionSelectionRepository userOptionSelectionRepository;

  public GetRecoveryOptionsHandler(
      ForecastApi forecastApi,
      RecoveryOptionRepository recoveryOptionRepository,
      UserOptionSelectionRepository userOptionSelectionRepository) {
    this.forecastApi = forecastApi;
    this.recoveryOptionRepository = recoveryOptionRepository;
    this.userOptionSelectionRepository = userOptionSelectionRepository;
  }

  @Operation(
      summary = "회복안 목록 조회",
      description = "회복안 카탈로그 전체를 반환하고, 해당 예측 실행에서 사용자가 선택한 항목은 selected=true 로 표시한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/recovery-options")
  public ResponseEntity<ApiResponse<List<RecoveryOptionView>>> handle(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId) {
    if (!forecastApi.forecastRunExists(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }

    Set<Long> selectedIds =
        userOptionSelectionRepository.findByForecastRunIdOrderByIdAsc(forecastRunId).stream()
            .map(s -> s.getRecoveryOptionId())
            .collect(Collectors.toSet());

    List<RecoveryOptionView> views =
        recoveryOptionRepository.findAllByOrderByIdAsc().stream()
            .map(o -> toView(o, selectedIds.contains(o.getId())))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }

  private static RecoveryOptionView toView(RecoveryOption o, boolean selected) {
    return new RecoveryOptionView(
        o.getId(),
        o.getOptionCode(),
        RecoveryOptionCategory.valueOf(o.getCategory()),
        o.getExpectedEffectText(),
        o.getMonthlyBurdenChangeText(),
        o.getPreconditionText(),
        o.getDifficulty() == null ? null : RecoveryOptionDifficulty.valueOf(o.getDifficulty()),
        o.isRequiresReview(),
        o.getDisclaimer(),
        selected);
  }
}
