package recovery30.server.consultation.getcounselorslots;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.consultation.domain.CounselorSlotStatus;

/** 상담 예약 화면 "예약 가능 일시 선택" 한 줄. "잔여 N석" = {@code remainingSeats}. */
public record CounselorSlotView(
    @Schema(description = "슬롯 ID (예약 요청에 사용)", example = "31") Long slotId,
    @Schema(description = "시작 시각(UTC)", example = "2025-07-14T01:00:00Z") Instant startAt,
    @Schema(description = "종료 시각(UTC)", example = "2025-07-14T01:30:00Z") Instant endAt,
    @Schema(description = "정원", example = "3") int capacity,
    @Schema(description = "현재 예약 수", example = "1") int bookedCount,
    @Schema(description = "잔여석 (capacity - bookedCount)", example = "2") int remainingSeats,
    @Schema(description = "슬롯 상태") CounselorSlotStatus status,
    @Schema(description = "예약 가능 여부 (BLOCKED 아님 + 잔여석 있음)", example = "true") boolean bookable) {}
