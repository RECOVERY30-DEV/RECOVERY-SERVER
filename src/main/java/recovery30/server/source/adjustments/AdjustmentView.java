package recovery30.server.source.adjustments;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import recovery30.server.source.domain.Adjustment;
import recovery30.server.source.domain.AdjustmentCertainty;
import recovery30.server.source.domain.AdjustmentStatus;
import recovery30.server.source.domain.AdjustmentType;

/** 정보 보정 화면 + 입력 4화면에서 다루는 보정값 한 건. */
public record AdjustmentView(
    @Schema(description = "보정값 ID", example = "10") Long id,
    @Schema(description = "유형") AdjustmentType adjustmentType,
    @Schema(
            description = "현금흐름 방향 (I=유입, O=유출). 유형으로 결정됨",
            example = "I",
            allowableValues = {"I", "O"})
        String direction,
    @Schema(description = "금액(원, 양수)", example = "1200000") Long amount,
    @Schema(description = "예정일", example = "2025-07-20") LocalDate expectedDate,
    @Schema(description = "확실성") AdjustmentCertainty certainty,
    @Schema(description = "반복 규칙. nullable", example = "매월 15일") String recurrenceRule,
    @Schema(description = "지출 항목(예정지출). nullable", example = "인테리어 대금") String expenseCategory,
    @Schema(description = "자금 출처(타행·외부자금). nullable", example = "타행 계좌 이체") String fundSource,
    @Schema(description = "메모. nullable") String memo,
    @Schema(description = "상태") AdjustmentStatus status,
    @Schema(description = "반영된 예측 실행 ID. nullable", example = "1") Long appliedRunId,
    @Schema(description = "생성 시각(UTC)") Instant createdAt,
    @Schema(description = "수정 시각(UTC). nullable") Instant updatedAt) {

  static AdjustmentView of(Adjustment a) {
    return new AdjustmentView(
        a.getId(),
        AdjustmentType.valueOf(a.getAdjustmentType()),
        a.getDirection(),
        a.getAmount(),
        a.getExpectedDate(),
        AdjustmentCertainty.valueOf(a.getCertainty()),
        a.getRecurrenceRule(),
        a.getExpenseCategory(),
        a.getFundSource(),
        a.getMemo(),
        AdjustmentStatus.valueOf(a.getStatus()),
        a.getAppliedRunId(),
        a.getCreatedAt(),
        a.getUpdatedAt());
  }
}
