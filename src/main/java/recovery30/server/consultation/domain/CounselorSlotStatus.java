package recovery30.server.consultation.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 상담 슬롯 상태 ({@code recovery_counselor_slots.status}). 예약 가능 여부는 status와 잔여석(capacity -
 * bookedCount)을 함께 본다.
 */
@Schema(
    name = "CounselorSlotStatus",
    description =
        """
        상담 슬롯 상태
        - OPEN: 예약 가능 (잔여석이 있을 때)
        - BOOKED: 정원 마감
        - BLOCKED: 운영자가 막음
        """,
    enumAsRef = true)
public enum CounselorSlotStatus {
  OPEN,
  BOOKED,
  BLOCKED
}
