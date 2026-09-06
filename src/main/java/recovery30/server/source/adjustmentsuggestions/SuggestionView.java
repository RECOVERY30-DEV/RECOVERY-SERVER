package recovery30.server.source.adjustmentsuggestions;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import recovery30.server.source.domain.AdjustmentSuggestion;
import recovery30.server.source.domain.AdjustmentType;
import recovery30.server.source.domain.SuggestionStatus;

/** 정보 보정 화면 "반복 패턴 추정 후보" 한 건. */
public record SuggestionView(
    @Schema(description = "추정 후보 ID", example = "3") Long id,
    @Schema(description = "유형") AdjustmentType adjustmentType,
    @Schema(description = "추정 금액(원). nullable", example = "1200000") Long suggestedAmount,
    @Schema(description = "추정 반복 규칙. nullable", example = "매월 15일") String suggestedRule,
    @Schema(description = "근거 문구", example = "최근 3개월 동일 패턴") String evidenceText,
    @Schema(description = "신뢰도(0~1). nullable", example = "0.82") BigDecimal confidence,
    @Schema(description = "상태") SuggestionStatus status,
    @Schema(description = "수락 시 생성된 보정값 ID. nullable", example = "11") Long acceptedAdjustmentId) {

  static SuggestionView of(AdjustmentSuggestion s) {
    return new SuggestionView(
        s.getId(),
        AdjustmentType.valueOf(s.getAdjustmentType()),
        s.getSuggestedAmount(),
        s.getSuggestedRule(),
        s.getEvidenceText(),
        s.getConfidence(),
        SuggestionStatus.valueOf(s.getStatus()),
        s.getAcceptedAdjustmentId());
  }
}
