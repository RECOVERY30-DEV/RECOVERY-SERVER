package recovery30.server.recoveryoption.selfaction;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import recovery30.server.recoveryoption.domain.SelfActionItemStatus;

/** 준비 항목 부분 수정. null 이 아닌 필드만 반영. */
public record UpdateSelfActionItemCommand(
    @Schema(description = "할 일", example = "거래 은행에 원리금 납부일 변경 신청") String title,
    @Schema(description = "예정일", example = "2025-07-19") LocalDate targetDate,
    @Schema(description = "상태 (PENDING/DONE)", example = "DONE") SelfActionItemStatus status,
    @Schema(description = "메모") String memo) {}
