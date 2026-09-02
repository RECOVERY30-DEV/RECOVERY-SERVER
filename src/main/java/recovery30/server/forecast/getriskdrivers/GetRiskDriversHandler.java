package recovery30.server.forecast.getriskdrivers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.domain.RiskDriver;
import recovery30.server.forecast.getriskdrivers.RiskDriverView.EvidenceView;
import recovery30.server.forecast.internal.ForecastRiskDriverRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;
import recovery30.server.forecast.internal.RiskDriverEvidenceRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '부족 원인(위험 신호) 목록 조회' 슬라이스. */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "Forecast", description = "30일 현금흐름 예측 조회")
public class GetRiskDriversHandler {

  private final ForecastRunRepository forecastRunRepository;
  private final ForecastRiskDriverRepository riskDriverRepository;
  private final RiskDriverEvidenceRepository evidenceRepository;

  public GetRiskDriversHandler(
      ForecastRunRepository forecastRunRepository,
      ForecastRiskDriverRepository riskDriverRepository,
      RiskDriverEvidenceRepository evidenceRepository) {
    this.forecastRunRepository = forecastRunRepository;
    this.riskDriverRepository = riskDriverRepository;
    this.evidenceRepository = evidenceRepository;
  }

  @Operation(
      summary = "부족 원인 목록 조회",
      description = "예측 실행의 위험 신호를 rank 순으로. limit으로 상위 N건만, include=evidence로 근거 거래를 함께 받는다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (원인이 없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/risk-drivers")
  public ResponseEntity<ApiResponse<List<RiskDriverView>>> handle(
      @Parameter(description = "예측 실행 ID", example = "4821") @PathVariable Long forecastRunId,
      @Parameter(description = "상위 N건만 반환 (미지정 시 전체)", example = "3")
          @RequestParam(required = false)
          Integer limit,
      @Parameter(description = "evidence 지정 시 근거 거래 포함") @RequestParam(required = false)
          String include) {
    if (!forecastRunRepository.existsById(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }

    Pageable pageable = (limit == null) ? Pageable.unpaged() : PageRequest.of(0, limit);
    List<RiskDriver> drivers =
        riskDriverRepository.findByForecastRunIdOrderByRankNo(forecastRunId, pageable);

    boolean withEvidence = "evidence".equals(include);
    Map<Long, List<EvidenceView>> evidenceByDriver =
        (withEvidence && !drivers.isEmpty()) ? loadEvidence(drivers) : Map.of();

    List<RiskDriverView> views =
        drivers.stream()
            .map(
                d ->
                    new RiskDriverView(
                        d.getRankNo(),
                        d.getDriverCode(),
                        d.getTitle(),
                        d.getOccurrenceDate(),
                        d.getOccurrenceText(),
                        d.getImpactPeriodText(),
                        d.getMetricText(),
                        d.getContributionAmount(),
                        d.isEstimating(),
                        withEvidence ? evidenceByDriver.getOrDefault(d.getId(), List.of()) : null))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }

  private Map<Long, List<EvidenceView>> loadEvidence(List<RiskDriver> drivers) {
    List<Long> ids = drivers.stream().map(RiskDriver::getId).toList();
    return evidenceRepository.findByRiskDriverIdInOrderByRiskDriverIdAscIdAsc(ids).stream()
        .collect(
            Collectors.groupingBy(
                e -> e.getRiskDriverId(),
                Collectors.mapping(
                    e ->
                        new EvidenceView(
                            e.getRefType(), e.getRefId(), e.getLabel(), e.getPeriodText()),
                    Collectors.toList())));
  }
}
