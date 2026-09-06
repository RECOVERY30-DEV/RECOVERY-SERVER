package recovery30.server.forecast.getforecastcoverage;

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
import recovery30.server.forecast.domain.CoverageSourceType;
import recovery30.server.forecast.domain.CoverageStatus;
import recovery30.server.forecast.internal.ForecastCoverageRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '분석 데이터 범위(Coverage) 조회' 슬라이스. */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")
public class GetForecastCoverageHandler {

  private final ForecastRunRepository forecastRunRepository;
  private final ForecastCoverageRepository coverageRepository;

  public GetForecastCoverageHandler(
      ForecastRunRepository forecastRunRepository, ForecastCoverageRepository coverageRepository) {
    this.forecastRunRepository = forecastRunRepository;
    this.coverageRepository = coverageRepository;
  }

  @Operation(
      summary = "분석 데이터 범위 조회",
      description = "예측 실행 시점의 소스별 Coverage 스냅샷. 임계(70%) 미만이면 status=PARTIAL.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (스냅샷이 없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/coverage")
  public ResponseEntity<ApiResponse<List<CoverageView>>> handle(
      @Parameter(description = "예측 실행 ID", example = "4821") @PathVariable Long forecastRunId) {
    if (!forecastRunRepository.existsById(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }

    List<CoverageView> views =
        coverageRepository.findByForecastRunIdOrderById(forecastRunId).stream()
            .map(
                c ->
                    new CoverageView(
                        CoverageSourceType.valueOf(c.getSourceType()),
                        CoverageStatus.of(c.isBelowThreshold()),
                        c.getCoverageRate(),
                        c.getLastSyncedAt(),
                        c.isBelowThreshold()))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
