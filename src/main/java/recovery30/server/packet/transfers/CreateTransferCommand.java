package recovery30.server.packet.transfers;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

/** Packet 상담자 전송 요청. 전송 동의({@code consentId}) 없이는 전송할 수 없다. */
public record CreateTransferCommand(
    @Schema(description = "상담자 ID. nullable", example = "2") Long counselorId,
    @Schema(description = "전송 채널", example = "PHONE") String channel,
    @Schema(description = "전송 범위 (JSON). 위험 Snapshot·보정값·선택안·사전 질문 포함 여부") JsonNode scope,
    @Schema(
            description = "상담원 전송 동의 ID. 필수",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "3")
        Long consentId) {}
