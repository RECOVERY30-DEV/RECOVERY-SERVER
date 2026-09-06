package recovery30.server.supportprogram.getprogramrecommendations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;
import recovery30.server.supportprogram.domain.ProgramRecommendation;
import recovery30.server.supportprogram.domain.SupportProgram;
import recovery30.server.supportprogram.internal.ProgramRecommendationRepository;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

/** '지원제도 추천 조회' 슬라이스. 지원사업 목록 화면 추천 정렬 + 회복안 비교 화면에서 재사용. MVP는 목데이터 시드. */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "SupportProgram", description = "지원제도 (목록·상세·필요서류·자격판정·추천)")
public class GetProgramRecommendationsHandler {

  private final ForecastApi forecastApi;
  private final ProgramRecommendationRepository recommendationRepository;
  private final SupportProgramRepository supportProgramRepository;

  public GetProgramRecommendationsHandler(
      ForecastApi forecastApi,
      ProgramRecommendationRepository recommendationRepository,
      SupportProgramRepository supportProgramRepository) {
    this.forecastApi = forecastApi;
    this.recommendationRepository = recommendationRepository;
    this.supportProgramRepository = supportProgramRepository;
  }

  @Operation(
      summary = "지원제도 추천 조회",
      description = "예측 실행별 추천 지원제도를 rank 순으로 반환한다. 각 항목의 matchReason 을 목록 화면 카드의 'Match 근거'로 쓴다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (추천이 없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/program-recommendations")
  public ResponseEntity<ApiResponse<List<ProgramRecommendationView>>> handle(
      @Parameter(description = "예측 실행 ID", example = "4821") @PathVariable Long forecastRunId) {
    if (!forecastApi.forecastRunExists(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }

    List<ProgramRecommendation> recs =
        recommendationRepository.findByForecastRunIdOrderByRankNoAsc(forecastRunId);
    if (recs.isEmpty()) {
      return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    Map<Long, SupportProgram> programById =
        supportProgramRepository
            .findAllById(recs.stream().map(ProgramRecommendation::getProgramId).toList())
            .stream()
            .collect(Collectors.toMap(SupportProgram::getId, Function.identity()));

    List<ProgramRecommendationView> views =
        recs.stream()
            .map(
                r -> {
                  SupportProgram p = programById.get(r.getProgramId());
                  return new ProgramRecommendationView(
                      r.getRankNo(),
                      p == null ? null : p.getProgramCode(),
                      p == null ? null : p.getName(),
                      p == null ? null : p.getAgency(),
                      p == null ? null : p.getApplyDeadline(),
                      r.getMatchReason());
                })
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
