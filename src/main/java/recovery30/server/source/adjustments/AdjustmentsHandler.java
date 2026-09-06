package recovery30.server.source.adjustments;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;
import recovery30.server.source.domain.Adjustment;
import recovery30.server.source.domain.AdjustmentType;
import recovery30.server.source.internal.AdjustmentRepository;

/**
 * 보정값 CRUD + 적용. 정보 보정 화면과 입력 4개 화면(현금매출/타행·외부자금/예정수입/예정지출)이 공용으로 쓴다. 하나의 리소스라 CRUD 를 한 핸들러에 모았다.
 */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Source", description = "원천 데이터 (연동 상태·보정값)")
public class AdjustmentsHandler {

  private final AdjustmentRepository adjustmentRepository;
  private final ForecastApi forecastApi;

  public AdjustmentsHandler(AdjustmentRepository adjustmentRepository, ForecastApi forecastApi) {
    this.adjustmentRepository = adjustmentRepository;
    this.forecastApi = forecastApi;
  }

  @Operation(
      summary = "보정값 목록 조회",
      description = "사업자의 보정값을 예정일 순으로. type / status 로 필터. 기본은 DISCARDED 제외.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공")
  })
  @GetMapping("/{businessId}/adjustments")
  public ResponseEntity<ApiResponse<List<AdjustmentView>>> list(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @Parameter(description = "유형 필터", example = "CASH_SALES") @RequestParam(required = false)
          AdjustmentType type,
      @Parameter(description = "상태 필터 (DRAFT/SAVED/DISCARDED)", example = "DRAFT")
          @RequestParam(required = false)
          String status) {
    List<AdjustmentView> views =
        adjustmentRepository.findByBusinessIdOrderByExpectedDateAscIdAsc(businessId).stream()
            .filter(a -> type == null || type.name().equals(a.getAdjustmentType()))
            .filter(
                a ->
                    status != null
                        ? status.equals(a.getStatus())
                        : !"DISCARDED".equals(a.getStatus()))
            .map(AdjustmentView::of)
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }

  @Operation(summary = "보정값 입력", description = "status=DRAFT 로 생성. 방향(I/O)은 유형으로 자동 결정된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "생성 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "필수 값 누락 / 금액 <= 0",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{businessId}/adjustments")
  @Transactional
  public ResponseEntity<ApiResponse<AdjustmentView>> create(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @RequestBody CreateAdjustmentCommand command) {
    if (command.adjustmentType() == null
        || command.certainty() == null
        || command.expectedDate() == null
        || command.amount() == null
        || command.amount() <= 0) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
    Instant now = Instant.now();
    Adjustment a = new Adjustment();
    a.setBusinessId(businessId);
    a.setAdjustmentType(command.adjustmentType().name());
    a.setDirection(command.adjustmentType().direction());
    a.setAmount(command.amount());
    a.setExpectedDate(command.expectedDate());
    a.setCertainty(command.certainty().name());
    a.setRecurrenceRule(command.recurrenceRule());
    a.setExpenseCategory(command.expenseCategory());
    a.setFundSource(command.fundSource());
    a.setMemo(command.memo());
    a.setStatus("DRAFT");
    a.setCreatedAt(now);
    a.setUpdatedAt(now);
    return ResponseEntity.ok(ApiResponse.success(AdjustmentView.of(adjustmentRepository.save(a))));
  }

  @Operation(summary = "보정값 수정", description = "null 이 아닌 필드만 반영. 유형은 변경 불가.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 보정값",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PatchMapping("/{businessId}/adjustments/{id}")
  @Transactional
  public ResponseEntity<ApiResponse<AdjustmentView>> update(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @Parameter(description = "보정값 ID", example = "10") @PathVariable Long id,
      @RequestBody UpdateAdjustmentCommand command) {
    Adjustment a =
        adjustmentRepository
            .findByIdAndBusinessId(id, businessId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ADJUSTMENT_NOT_FOUND));
    if (command.amount() != null) {
      if (command.amount() <= 0) {
        throw new BusinessException(ErrorCode.INVALID_INPUT);
      }
      a.setAmount(command.amount());
    }
    if (command.expectedDate() != null) {
      a.setExpectedDate(command.expectedDate());
    }
    if (command.certainty() != null) {
      a.setCertainty(command.certainty().name());
    }
    if (command.recurrenceRule() != null) {
      a.setRecurrenceRule(command.recurrenceRule());
    }
    if (command.expenseCategory() != null) {
      a.setExpenseCategory(command.expenseCategory());
    }
    if (command.fundSource() != null) {
      a.setFundSource(command.fundSource());
    }
    if (command.memo() != null) {
      a.setMemo(command.memo());
    }
    a.setUpdatedAt(Instant.now());
    return ResponseEntity.ok(ApiResponse.success(AdjustmentView.of(adjustmentRepository.save(a))));
  }

  @Operation(summary = "보정값 삭제", description = "덮어쓰지 않고 status=DISCARDED 로 전환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "삭제 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 보정값",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @DeleteMapping("/{businessId}/adjustments/{id}")
  @Transactional
  public ResponseEntity<ApiResponse<AdjustmentView>> discard(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @Parameter(description = "보정값 ID", example = "10") @PathVariable Long id) {
    Adjustment a =
        adjustmentRepository
            .findByIdAndBusinessId(id, businessId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ADJUSTMENT_NOT_FOUND));
    a.setStatus("DISCARDED");
    a.setUpdatedAt(Instant.now());
    return ResponseEntity.ok(ApiResponse.success(AdjustmentView.of(adjustmentRepository.save(a))));
  }

  @Operation(
      summary = "보정값 적용 (재계산 트리거)",
      description =
          "DRAFT 보정값을 모두 SAVED 로 전환하고 최신 예측 실행에 applied_run_id 를 기록한다. 실제 재예측(엔진)은 별도. 최신 예측이 없으면"
              + " appliedRunId=null.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "적용 성공")
  })
  @PostMapping("/{businessId}/adjustments/apply")
  @Transactional
  public ResponseEntity<ApiResponse<AppliedAdjustmentsView>> apply(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId) {
    Long runId = forecastApi.findLatestForecastRunId(businessId).orElse(null);
    List<Adjustment> drafts = adjustmentRepository.findByBusinessIdAndStatus(businessId, "DRAFT");
    for (Adjustment a : drafts) {
      a.setStatus("SAVED");
      a.setAppliedRunId(runId);
      a.setUpdatedAt(Instant.now());
    }
    adjustmentRepository.saveAll(drafts);
    return ResponseEntity.ok(ApiResponse.success(new AppliedAdjustmentsView(drafts.size(), runId)));
  }
}
