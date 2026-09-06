package recovery30.server.source.adjustments;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import recovery30.server.source.domain.AdjustmentCertainty;
import recovery30.server.source.domain.AdjustmentType;

/** 보정값 입력 (현금매출/타행·외부자금/예정수입/예정지출 4화면 공용). status=DRAFT 로 생성된다. */
public record CreateAdjustmentCommand(
    @Schema(description = "유형", requiredMode = Schema.RequiredMode.REQUIRED, example = "CASH_SALES")
        AdjustmentType adjustmentType,
    @Schema(
            description = "금액(원, 양수)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1200000")
        Long amount,
    @Schema(
            description = "예정일",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2025-07-20")
        LocalDate expectedDate,
    @Schema(description = "확실성", requiredMode = Schema.RequiredMode.REQUIRED, example = "ESTIMATED")
        AdjustmentCertainty certainty,
    @Schema(description = "반복 규칙 (선택)", example = "매월 15일") String recurrenceRule,
    @Schema(description = "지출 항목 (예정지출, 선택)", example = "인테리어 대금") String expenseCategory,
    @Schema(description = "자금 출처 (타행·외부자금, 선택)", example = "타행 계좌 이체") String fundSource,
    @Schema(description = "메모 (선택)") String memo) {}
