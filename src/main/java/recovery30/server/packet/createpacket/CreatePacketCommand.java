package recovery30.server.packet.createpacket;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

/**
 * Recovery Packet 새 버전 생성 요청. {@code snapshot}은 회복안 비교/셀프 액션 화면에서 조립한, 이 시점에 동결할 내용(위험
 * Snapshot·보정값·원인·선택안 ...) 전체를 담은 JSON 객체다.
 */
public record CreatePacketCommand(
    @Schema(
            description = "동결할 Packet 내용 (JSON 객체). 필수",
            requiredMode = Schema.RequiredMode.REQUIRED)
        JsonNode snapshot) {}
