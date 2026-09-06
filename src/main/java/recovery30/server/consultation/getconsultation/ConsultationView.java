package recovery30.server.consultation.getconsultation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import recovery30.server.consultation.domain.ConsultationChannel;
import recovery30.server.consultation.domain.ConsultationStatus;

/** 상담 예약 확인 화면 "예약 정보". */
public record ConsultationView(
    @Schema(description = "상담 예약 ID", example = "8") Long consultationId,
    @Schema(description = "사업자 ID", example = "1") Long businessId,
    @Schema(description = "연결된 Packet ID. nullable", example = "42") Long packetId,
    @Schema(description = "상담자 ID. nullable", example = "1") Long counselorId,
    @Schema(description = "상담자 이름. nullable", example = "김상담") String counselorName,
    @Schema(description = "상담 채널") ConsultationChannel channel,
    @Schema(description = "예약 일시(UTC)", example = "2025-07-14T01:00:00Z") Instant scheduledAt,
    @Schema(description = "상담 목적", example = "30일 현금흐름 위험 대응 — 부족일 이전 회복 지원") String purposeText,
    @Schema(description = "사전 질문. nullable") String preQuestion,
    @Schema(description = "상담원 정보 전송 동의 여부", example = "true") boolean transferConsentGranted,
    @Schema(description = "예약 상태") ConsultationStatus status,
    @Schema(description = "상담에서 다룰 회복안 ID 목록", example = "[1, 3]") List<Long> recoveryOptionIds,
    @Schema(description = "상담 최종 판단. 상담자만 기록 가능. nullable") String finalDecision,
    @Schema(description = "상담 결과 메모. nullable") String resultNote) {}
