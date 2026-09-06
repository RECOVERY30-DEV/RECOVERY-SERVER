package recovery30.server.packet.getpacket;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.packet.domain.PacketStatus;
import recovery30.server.packet.domain.RecoveryPacket;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Recovery Packet 화면 전체. {@code snapshot}은 생성 시점에 동결된 JSON(위험 Snapshot·보정값·원인·선택한 회복안)이며 클라이언트가 구조를
 * 파싱해 렌더한다.
 */
public record PacketView(
    @Schema(description = "Packet ID", example = "42") Long packetId,
    @Schema(description = "사업자 ID", example = "1") Long businessId,
    @Schema(description = "예측 실행 ID", example = "4821") Long forecastRunId,
    @Schema(description = "버전 (1부터). 수정 시 새 버전 생성", example = "2") Integer version,
    @Schema(description = "이전 버전 Packet ID. v1이면 null", example = "41") Long supersedesPacketId,
    @Schema(description = "상태") PacketStatus status,
    @Schema(description = "생성 시각(UTC)", example = "2025-07-14T00:32:00Z") Instant generatedAt,
    @Schema(description = "고객 확인 시각(UTC). nullable", example = "2025-07-14T01:00:00Z")
        Instant customerConfirmedAt,
    @Schema(description = "상담자 전송 시각(UTC). nullable", example = "2025-07-14T00:35:00Z")
        Instant sentAt,
    @Schema(description = "PDF URL. nullable") String pdfUrl,
    @Schema(description = "동결된 Packet 내용 (JSON)") JsonNode snapshot) {

  static PacketView of(RecoveryPacket p, ObjectMapper objectMapper) {
    return new PacketView(
        p.getId(),
        p.getBusinessId(),
        p.getForecastRunId(),
        p.getVersion(),
        p.getSupersedesPacketId(),
        PacketStatus.valueOf(p.getStatus()),
        p.getGeneratedAt(),
        p.getCustomerConfirmedAt(),
        p.getSentAt(),
        p.getPdfUrl(),
        p.getSnapshotJson() == null ? null : objectMapper.readTree(p.getSnapshotJson()));
  }
}
