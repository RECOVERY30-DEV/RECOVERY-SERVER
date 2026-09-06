package recovery30.server.forecast.getforecast;

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
import recovery30.server.forecast.domain.ForecastStatus;
import recovery30.server.forecast.internal.ForecastRunRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '특정 예측 실행 조회' 슬라이스. 원인 상세 화면 헤더 + Recovery Packet 위험 Snapshot 공용. */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")
public class GetForecastHandler {

  private final ForecastRunRepository forecastRunRepository;

  public GetForecastHandler(ForecastRunRepository forecastRunRepository) {
    this.forecastRunRepository = forecastRunRepository;
  }

  @Operation(
      summary = "특정 예측 실행 조회",
      description =
          "forecastRunId 로 예측 실행 1건의 상태·부족 시점·최저잔액 밴드를 한 번에 반환한다. 홈은 businesses/{id}/forecasts/latest"
              + " 로 최신 run 을 받고, 원인 상세·Packet 등 특정 run 을 다룰 때 이 엔드포인트를 쓴다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}")
  public ResponseEntity<ApiResponse<ForecastDetailView>> handle(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId) {
    ForecastRun run =
        forecastRunRepository
            .findById(forecastRunId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FORECAST_NOT_FOUND));

    ForecastDetailView view =
        new ForecastDetailView(
            run.getId(),
            run.getBusinessId(),
            run.getBaseDate(),
            run.getCreatedAt(),
            ForecastStatus.valueOf(run.getStatus()),
            run.getHorizonDays(),
            run.getCoverageOverall(),
            run.getFirstShortfallDate() != null,
            run.getDaysToShortfall(),
            run.getFirstShortfallDate(),
            run.getShortfallAmountMin(),
            run.getShortfallAmountMax(),
            run.getMinBalanceExpected() != null,
            run.getMinBalanceConservative(),
            run.getMinBalanceExpected(),
            run.getMinBalanceOptimistic(),
            run.isBufferMet());
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
