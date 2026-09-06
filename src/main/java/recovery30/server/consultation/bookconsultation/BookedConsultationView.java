package recovery30.server.consultation.bookconsultation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.consultation.domain.ConsultationChannel;
import recovery30.server.consultation.domain.ConsultationStatus;

/** 상담 예약 생성 결과. 화면은 이 consultationId 로 상세를 다시 조회한다. */
public record BookedConsultationView(
    @Schema(description = "생성된 상담 예약 ID", example = "8") Long consultationId,
    @Schema(description = "예약 상태 (생성 직후 REQUESTED)") ConsultationStatus status,
    @Schema(description = "상담 채널") ConsultationChannel channel,
    @Schema(description = "예약 일시(UTC)", example = "2025-07-14T01:00:00Z") Instant scheduledAt) {}
