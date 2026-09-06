package recovery30.server.consultation.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 상담 채널 ({@code recovery_consultations.channel}). */
@Schema(
    name = "ConsultationChannel",
    description =
        """
        상담 채널
        - PHONE: 전화 상담
        - VIDEO: 화상 상담
        - VISIT: 방문 상담
        - CHAT: 채팅 상담
        """,
    enumAsRef = true)
public enum ConsultationChannel {
  PHONE,
  VISIT,
  VIDEO,
  CHAT
}
