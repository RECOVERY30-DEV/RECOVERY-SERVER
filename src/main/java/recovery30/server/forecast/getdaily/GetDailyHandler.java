package recovery30.server.forecast.getdaily;

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
import recovery30.server.forecast.internal.ForecastDailyRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '30일 일자별 현금흐름 조회' 슬라이스. Dashboard 캘린더 + 안정 상태 차트. */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")
public class GetDailyHandler {

  private final ForecastRunRepository forecastRunRepository;
  private final ForecastDailyRepository forecastDailyRepository;

  public GetDailyHandler(
      ForecastRunRepository forecastRunRepository,
      ForecastDailyRepository forecastDailyRepository) {
    this.forecastRunRepository = forecastRunRepository;
    this.forecastDailyRepository = forecastDailyRepository;
  }

  @Operation(summary = "일자별 현금흐름 조회", description = "예측 실행의 30일 캘린더 행을 날짜순으로 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (일자 데이터가 없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/daily")
  public ResponseEntity<ApiResponse<List<DailyView>>> handle(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId) {
    if (!forecastRunRepository.existsById(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }
    List<DailyView> views =
        forecastDailyRepository.findByForecastRunIdOrderByTargetDateAsc(forecastRunId).stream()
            .map(DailyView::of)
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
