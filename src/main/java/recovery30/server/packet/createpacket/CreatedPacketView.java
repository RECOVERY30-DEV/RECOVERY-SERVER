package recovery30.server.packet.createpacket;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.packet.domain.PacketStatus;

/** 새 Packet 버전 생성 결과. 화면은 이 packetId 로 상세를 다시 조회한다. */
public record CreatedPacketView(
    @Schema(description = "생성된 Packet ID", example = "1") Long packetId,
    @Schema(description = "버전", example = "2") Integer version,
    @Schema(description = "이전 버전 Packet ID. v1이면 null", example = "41") Long supersedesPacketId,
    @Schema(description = "상태 (생성 직후 DRAFT)") PacketStatus status,
    @Schema(description = "생성 시각(UTC)", example = "2025-07-14T00:32:00Z") Instant generatedAt) {}
