package recovery30.server.source.getdatasources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.shared.response.ApiResponse;
import recovery30.server.source.domain.SourceDataSource;
import recovery30.server.source.domain.SourceType;
import recovery30.server.source.domain.SyncStatus;
import recovery30.server.source.internal.SourceDataSourceRepository;

/** '연동 커넥션 현재 상태 조회' 슬라이스. 데이터 범위 확인 화면 + 홈/Dashboard "분석 데이터 범위". */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Source", description = "원천 데이터 (연동 상태·보정값)")
public class GetDataSourcesHandler {

  private static final BigDecimal THRESHOLD = new BigDecimal("70");

  private final SourceDataSourceRepository sourceDataSourceRepository;

  public GetDataSourcesHandler(SourceDataSourceRepository sourceDataSourceRepository) {
    this.sourceDataSourceRepository = sourceDataSourceRepository;
  }

  @Operation(
      summary = "연동 데이터 소스 현황 조회",
      description =
          "사업자의 연동 커넥션(계좌·카드정산·대출·자동이체) 현재 상태를 반환한다. coverageRate가 70% 미만이면 belowThreshold=true.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (연동이 없으면 빈 배열)")
  })
  @GetMapping("/{businessId}/data-sources")
  public ResponseEntity<ApiResponse<List<DataSourceView>>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId) {
    List<DataSourceView> views =
        sourceDataSourceRepository.findByBusinessIdOrderByIdAsc(businessId).stream()
            .map(GetDataSourcesHandler::toView)
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }

  private static DataSourceView toView(SourceDataSource s) {
    boolean below = s.getCoverageRate() != null && s.getCoverageRate().compareTo(THRESHOLD) < 0;
    return new DataSourceView(
        SourceType.valueOf(s.getSourceType()),
        s.getInstitutionName(),
        s.getCoverageRate(),
        s.getPeriodMonths(),
        s.getLastSyncedAt(),
        SyncStatus.valueOf(s.getSyncStatus()),
        below);
  }
}
