package recovery30.server.forecast.getsafetybuffer;

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
import recovery30.server.forecast.domain.ForecastRun;
import recovery30.server.forecast.internal.ForecastRunRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '안전 잔액 조회' 슬라이스. */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")
public class GetSafetyBufferHandler {

  private final ForecastRunRepository forecastRunRepository;

  public GetSafetyBufferHandler(ForecastRunRepository forecastRunRepository) {
    this.forecastRunRepository = forecastRunRepository;
  }

  @Operation(summary = "안전 잔액 조회", description = "안전 잔액(원)과 Safety Buffer 충족 여부.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/safety-buffer")
  public ResponseEntity<ApiResponse<SafetyBufferView>> handle(
      @Parameter(description = "예측 실행 ID", example = "4821") @PathVariable Long forecastRunId) {
    ForecastRun run =
        forecastRunRepository
            .findById(forecastRunId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FORECAST_NOT_FOUND));

    SafetyBufferView view =
        new SafetyBufferView(run.getId(), run.getMinBalanceExpected(), run.isBufferMet());
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
