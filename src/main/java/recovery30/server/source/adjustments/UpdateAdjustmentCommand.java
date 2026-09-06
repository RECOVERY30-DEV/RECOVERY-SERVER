package recovery30.server.source.adjustments;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import recovery30.server.source.domain.AdjustmentCertainty;

/** 보정값 부분 수정. null 이 아닌 필드만 반영된다. 유형은 바꿀 수 없다. */
public record UpdateAdjustmentCommand(
    @Schema(description = "금액(원, 양수)", example = "1500000") Long amount,
    @Schema(description = "예정일", example = "2025-07-22") LocalDate expectedDate,
    @Schema(description = "확실성", example = "CONFIRMED") AdjustmentCertainty certainty,
    @Schema(description = "반복 규칙", example = "매월 말일") String recurrenceRule,
    @Schema(description = "지출 항목") String expenseCategory,
    @Schema(description = "자금 출처") String fundSource,
    @Schema(description = "메모") String memo) {}
