package recovery30.server.recoveryoption.getscenarios;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.recoveryoption.domain.Scenario;
import recovery30.server.recoveryoption.domain.ScenarioType;
import recovery30.server.recoveryoption.internal.ScenarioOptionRepository;
import recovery30.server.recoveryoption.internal.ScenarioRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '시나리오 비교 조회' 슬라이스. 회복안 비교 화면 "시나리오 비교" (baseline + 옵션 적용 시뮬). */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "RecoveryOption", description = "회복안 비교 (회복안·시나리오·선택)")
public class GetScenariosHandler {

  private final ForecastApi forecastApi;
  private final ScenarioRepository scenarioRepository;
  private final ScenarioOptionRepository scenarioOptionRepository;

  public GetScenariosHandler(
      ForecastApi forecastApi,
      ScenarioRepository scenarioRepository,
      ScenarioOptionRepository scenarioOptionRepository) {
    this.forecastApi = forecastApi;
    this.scenarioRepository = scenarioRepository;
    this.scenarioOptionRepository = scenarioOptionRepository;
  }

  @Operation(
      summary = "시나리오 비교 조회",
      description =
          "BASELINE(아무 조치 없음) 1건 + SIMULATED(회복안 적용) N건을 반환한다. SIMULATED 행의 deltaDays/deltaMinBalance 는"
              + " BASELINE 대비 개선폭이다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (시나리오가 없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/scenarios")
  public ResponseEntity<ApiResponse<List<ScenarioView>>> handle(
      @Parameter(description = "예측 실행 ID", example = "4821") @PathVariable Long forecastRunId) {
    if (!forecastApi.forecastRunExists(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }

    List<Scenario> scenarios = scenarioRepository.findByForecastRunIdOrderByIdAsc(forecastRunId);
    if (scenarios.isEmpty()) {
      return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    List<Long> scenarioIds = scenarios.stream().map(Scenario::getId).toList();
    Map<Long, List<Long>> optionIdsByScenario =
        scenarioOptionRepository.findByScenarioIdInOrderByIdAsc(scenarioIds).stream()
            .collect(
                Collectors.groupingBy(
                    so -> so.getScenarioId(),
                    Collectors.mapping(so -> so.getRecoveryOptionId(), Collectors.toList())));

    List<ScenarioView> views =
        scenarios.stream()
            .map(
                s ->
                    new ScenarioView(
                        s.getId(),
                        ScenarioType.valueOf(s.getScenarioType()),
                        s.getFirstShortfallDate(),
                        s.getMinBalance(),
                        s.getDeltaDays(),
                        s.getDeltaMinBalance(),
                        s.getMonthlyPaymentDelta(),
                        s.getNote(),
                        optionIdsByScenario.getOrDefault(s.getId(), List.of())))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
