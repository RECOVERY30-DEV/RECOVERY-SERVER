package recovery30.server.forecast.getdailydetail;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.domain.ForecastDaily;
import recovery30.server.forecast.internal.ForecastDailyItemRepository;
import recovery30.server.forecast.internal.ForecastDailyRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '하루치 근거 상세 조회' 슬라이스. Dashboard 일자 상세 + 안정 상태 바텀시트. */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")
public class GetDailyDetailHandler {

  private final ForecastRunRepository forecastRunRepository;
  private final ForecastDailyRepository forecastDailyRepository;
  private final ForecastDailyItemRepository forecastDailyItemRepository;

  public GetDailyDetailHandler(
      ForecastRunRepository forecastRunRepository,
      ForecastDailyRepository forecastDailyRepository,
      ForecastDailyItemRepository forecastDailyItemRepository) {
    this.forecastRunRepository = forecastRunRepository;
    this.forecastDailyRepository = forecastDailyRepository;
    this.forecastDailyItemRepository = forecastDailyItemRepository;
  }

  @Operation(
      summary = "하루치 근거 상세 조회",
      description = "특정 날짜의 잔액·유입·유출 요약과 근거 라인(확정 거래·예상 거래·보정값)을 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행 또는 해당 날짜 데이터 없음",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/daily/{date}")
  public ResponseEntity<ApiResponse<DailyDetailView>> handle(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId,
      @Parameter(description = "대상 날짜 (YYYY-MM-DD)", example = "2025-07-20")
          @PathVariable
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date) {
    if (!forecastRunRepository.existsById(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }
    ForecastDaily daily =
        forecastDailyRepository
            .findByForecastRunIdAndTargetDate(forecastRunId, date)
            .orElseThrow(() -> new BusinessException(ErrorCode.FORECAST_DAILY_NOT_FOUND));

    return ResponseEntity.ok(
        ApiResponse.success(
            DailyDetailView.of(
                daily,
                forecastDailyItemRepository.findByForecastDailyIdOrderByIdAsc(daily.getId()))));
  }
}
