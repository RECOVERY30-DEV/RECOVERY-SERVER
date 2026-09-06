package recovery30.server.source.adjustmentsuggestions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;
import recovery30.server.source.domain.Adjustment;
import recovery30.server.source.domain.AdjustmentSuggestion;
import recovery30.server.source.domain.AdjustmentType;
import recovery30.server.source.internal.AdjustmentRepository;
import recovery30.server.source.internal.AdjustmentSuggestionRepository;

/** 반복 패턴 추정 후보 조회 + 수락. 정보 보정 화면 "반복 패턴 추정 후보". MVP는 목데이터로 시드한다. */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Source", description = "원천 데이터 (연동 상태·보정값)")
public class SuggestionsHandler {

  private final AdjustmentSuggestionRepository suggestionRepository;
  private final AdjustmentRepository adjustmentRepository;

  public SuggestionsHandler(
      AdjustmentSuggestionRepository suggestionRepository,
      AdjustmentRepository adjustmentRepository) {
    this.suggestionRepository = suggestionRepository;
    this.adjustmentRepository = adjustmentRepository;
  }

  @Operation(
      summary = "추정 후보 목록 조회",
      description = "반복 거래 패턴에서 뽑은 누락 가능 항목 후보. status 미지정 시 PROPOSED 만 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공")
  })
  @GetMapping("/{businessId}/adjustment-suggestions")
  public ResponseEntity<ApiResponse<List<SuggestionView>>> list(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @Parameter(description = "상태 필터 (PROPOSED/ACCEPTED/REJECTED)", example = "PROPOSED")
          @RequestParam(required = false)
          String status) {
    String wanted = status == null ? "PROPOSED" : status;
    List<SuggestionView> views =
        suggestionRepository.findByBusinessIdOrderByIdAsc(businessId).stream()
            .filter(s -> wanted.equals(s.getStatus()))
            .map(SuggestionView::of)
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }

  @Operation(
      summary = "추정 후보 수락",
      description =
          "후보를 보정값(status=DRAFT, certainty=ESTIMATED)으로 생성하고 후보를 ACCEPTED 로 표시한다. 예정일은"
              + " 오늘로 채워지므로 이후 PATCH 로 조정한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "수락 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "이미 처리된 후보 / 추정 금액 없음",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 후보",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{businessId}/adjustment-suggestions/{id}/accept")
  @Transactional
  public ResponseEntity<ApiResponse<SuggestionView>> accept(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @Parameter(description = "추정 후보 ID", example = "3") @PathVariable Long id) {
    AdjustmentSuggestion s =
        suggestionRepository
            .findByIdAndBusinessId(id, businessId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ADJUSTMENT_SUGGESTION_NOT_FOUND));
    if (!"PROPOSED".equals(s.getStatus())) {
      throw new BusinessException(ErrorCode.ADJUSTMENT_SUGGESTION_ALREADY_HANDLED);
    }
    if (s.getSuggestedAmount() == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }

    AdjustmentType type = AdjustmentType.valueOf(s.getAdjustmentType());
    Instant now = Instant.now();
    Adjustment a = new Adjustment();
    a.setBusinessId(businessId);
    a.setAdjustmentType(type.name());
    a.setDirection(type.direction());
    a.setAmount(s.getSuggestedAmount());
    a.setExpectedDate(LocalDate.now());
    a.setCertainty("ESTIMATED");
    a.setRecurrenceRule(s.getSuggestedRule());
    a.setMemo(s.getEvidenceText());
    a.setStatus("DRAFT");
    a.setCreatedAt(now);
    a.setUpdatedAt(now);
    a = adjustmentRepository.save(a);

    s.setStatus("ACCEPTED");
    s.setAcceptedAdjustmentId(a.getId());
    return ResponseEntity.ok(ApiResponse.success(SuggestionView.of(suggestionRepository.save(s))));
  }
}
