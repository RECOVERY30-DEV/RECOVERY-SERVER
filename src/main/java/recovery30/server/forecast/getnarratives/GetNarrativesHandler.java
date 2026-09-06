package recovery30.server.forecast.getnarratives;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.domain.NarrativeKind;
import recovery30.server.forecast.internal.ForecastRunNarrativeRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/**
 * '예측 실행 서술 문구 조회' 슬라이스. 안정 상태 안내 / 판단보류 안내 화면이 공용으로 쓴다. 상태별 수치는 latest / coverage / min-balance /
 * daily 를 재사용하고, 이 엔드포인트는 문구만 담당한다.
 */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")
public class GetNarrativesHandler {

  private final ForecastRunRepository forecastRunRepository;
  private final ForecastRunNarrativeRepository narrativeRepository;

  public GetNarrativesHandler(
      ForecastRunRepository forecastRunRepository,
      ForecastRunNarrativeRepository narrativeRepository) {
    this.forecastRunRepository = forecastRunRepository;
    this.narrativeRepository = narrativeRepository;
  }

  @Operation(
      summary = "예측 서술 문구 조회",
      description = "예측 실행의 상태 라벨·판단 근거·상태 변경 힌트·고지 문구를 kind/seq 순으로 반환한다. kind 로 필터 가능.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (문구가 없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/narratives")
  public ResponseEntity<ApiResponse<List<NarrativeView>>> handle(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId,
      @Parameter(description = "종류 필터", example = "STABLE_REASON") @RequestParam(required = false)
          NarrativeKind kind) {
    if (!forecastRunRepository.existsById(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }
    List<NarrativeView> views =
        narrativeRepository.findByForecastRunIdOrderByKindAscSeqAsc(forecastRunId).stream()
            .filter(n -> kind == null || kind.name().equals(n.getKind()))
            .map(NarrativeView::of)
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
