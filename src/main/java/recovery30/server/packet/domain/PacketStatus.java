package recovery30.server.packet.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** Recovery Packet 상태 ({@code recovery_packets.status}). */
@Schema(
    name = "PacketStatus",
    description =
        """
        Recovery Packet 상태
        - DRAFT: 생성됨, 고객 확인 전
        - CONFIRMED: 고객이 내용 확인함
        - SENT: 상담자에게 전송됨 (transfers 1건 이상)
        """,
    enumAsRef = true)
public enum PacketStatus {
  DRAFT,
  CONFIRMED,
  SENT
}
