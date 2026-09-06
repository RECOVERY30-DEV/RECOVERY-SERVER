package recovery30.server.packet.transfers;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.packet.domain.PacketTransfer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Recovery Packet 화면 "전송 상태" + 상담 예약 "정보 전송" 이력 한 건. */
public record PacketTransferView(
    @Schema(description = "전송 이력 ID", example = "7") Long transferId,
    @Schema(description = "상담자 ID. nullable", example = "2") Long counselorId,
    @Schema(description = "전송 채널. nullable", example = "PHONE") String channel,
    @Schema(description = "전송 범위 (JSON). 위험 Snapshot·보정값·선택안·사전 질문 포함 여부") JsonNode scope,
    @Schema(description = "전송 근거가 된 상담원 전송 동의 ID", example = "3") Long consentId,
    @Schema(description = "전송 시각(UTC)", example = "2025-07-14T00:35:00Z") Instant sentAt) {

  static PacketTransferView of(PacketTransfer t, ObjectMapper objectMapper) {
    return new PacketTransferView(
        t.getId(),
        t.getCounselorId(),
        t.getChannel(),
        t.getScopeJson() == null ? null : objectMapper.readTree(t.getScopeJson()),
        t.getConsentId(),
        t.getSentAt());
  }
}
