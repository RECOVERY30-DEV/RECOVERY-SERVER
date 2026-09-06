package recovery30.server.business.getconsents;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.business.domain.ConsentStatus;

/** 동의 관리 화면의 항목별 현재 상태 한 개. */
public record ConsentStatusView(
    @Schema(description = "동의 항목 코드", example = "ANALYSIS") String typeCode,
    @Schema(description = "항목명", example = "서비스 분석 동의") String name,
    @Schema(description = "필수 여부", example = "true") boolean required,
    @Schema(description = "현재 상태") ConsentStatus status,
    @Schema(description = "동의 시각(UTC). nullable", example = "2025-07-14T00:00:00Z")
        Instant grantedAt,
    @Schema(description = "철회 시각(UTC). nullable") Instant withdrawnAt,
    @Schema(
            description = "가장 최근 변경 시각(UTC). 화면의 '최종 동의 변경일' 계산용. nullable",
            example = "2025-07-14T00:00:00Z")
        Instant lastChangedAt,
    @Schema(description = "동의한 약관 버전. NOT_SET이면 null", example = "v1.0") String consentVersion) {}
