package recovery30.server.business.updateconsent;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.business.domain.ConsentStatus;

/** grant/withdraw 결과. 화면은 이 응답 후 GET /consents 로 목록을 다시 그린다. */
public record UpdatedConsentView(
    @Schema(description = "동의 항목 코드", example = "PACKET_TRANSFER") String typeCode,
    @Schema(description = "적용된 상태") ConsentStatus status,
    @Schema(description = "동의한 약관 버전", example = "v1.0") String consentVersion,
    @Schema(description = "변경 시각(UTC)", example = "2025-07-14T00:00:00Z") Instant changedAt) {}
