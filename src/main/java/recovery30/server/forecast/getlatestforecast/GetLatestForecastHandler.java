package recovery30.server.forecast.getlatestforecast;

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

/** '최신 예측 조회' 슬라이스. 홈 화면 진입점. */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")
public class GetLatestForecastHandler {

  private final ForecastRunRepository forecastRunRepository;

  public GetLatestForecastHandler(ForecastRunRepository forecastRunRepository) {
    this.forecastRunRepository = forecastRunRepository;
  }

  @Operation(
      summary = "최신 예측 조회",
      description =
          "사업자의 가장 최근 30일 현금흐름 예측 실행 1건의 식별자와 상태를 반환한다. 홈 화면 헤더·상태 배지에 사용하고, 반환된 forecastRunId로"
              + " 하위 리소스(min-balance, shortfall, safety-buffer, risk-drivers, coverage)를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "예측 이력이 없음",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{businessId}/forecasts/latest")
  public ResponseEntity<ApiResponse<LatestForecastView>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId) {
    ForecastRun run =
        forecastRunRepository
            .findTopByBusinessIdOrderByBaseDateDescCreatedAtDesc(businessId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FORECAST_NOT_FOUND));

    LatestForecastView view =
        new LatestForecastView(run.getId(), run.getBaseDate(), run.getCreatedAt(), run.getStatus());
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
