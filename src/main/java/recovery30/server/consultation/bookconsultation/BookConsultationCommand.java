package recovery30.server.consultation.bookconsultation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** 상담 예약 생성 요청. slotId 를 주면 예약 일시는 슬롯에서 가져오고 잔여석을 1 줄인다. */
public record BookConsultationCommand(
    @Schema(description = "상담자 ID. nullable", example = "1") Long counselorId,
    @Schema(description = "예약할 슬롯 ID. nullable (직접 일시 지정 시 생략)", example = "31") Long slotId,
    @Schema(description = "상담 채널", example = "PHONE", requiredMode = Schema.RequiredMode.REQUIRED)
        String channel,
    @Schema(description = "예약 일시(UTC). slotId 없을 때 필수", example = "2025-07-14T01:00:00Z")
        Instant scheduledAt,
    @Schema(description = "상담 목적", example = "30일 현금흐름 위험 대응 — 부족일 이전 회복 지원") String purposeText,
    @Schema(description = "사전 질문 (선택)") String preQuestion,
    @Schema(description = "상담원 정보 전송 동의 여부. 생략 시 false", example = "true")
        Boolean transferConsentGranted,
    @Schema(description = "연결할 Recovery Packet ID (선택)", example = "42") Long packetId,
    @Schema(description = "상담에서 다룰 회복안 ID 목록", example = "[1, 3]") List<Long> recoveryOptionIds) {}
