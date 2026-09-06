package recovery30.server.consultation.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 상담 예약 상태 ({@code recovery_consultations.status}). */
@Schema(
    name = "ConsultationStatus",
    description =
        """
        상담 예약 상태
        - REQUESTED: 예약 요청됨 (기본)
        - CONFIRMED: 상담자/기관이 확정함
        - COMPLETED: 상담 완료
        - CANCELED: 취소됨
        """,
    enumAsRef = true)
public enum ConsultationStatus {
  REQUESTED,
  CONFIRMED,
  COMPLETED,
  CANCELED
}
