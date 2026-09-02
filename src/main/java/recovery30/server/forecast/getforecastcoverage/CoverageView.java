package recovery30.server.forecast.getforecastcoverage;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 홈 화면 "분석 데이터 범위" 행. 소스타입의 한글 라벨 매핑, {@code LOAN}+{@code AUTO_TRANSFER}를 "자동이체/대출" 한 줄로 병합하는 것,
 * "갱신 완료"/"부분 반영" 문구는 클라이언트가 만든다.
 */
public record CoverageView(
    @Schema(
            description = "소스 유형",
            example = "AUTO_TRANSFER",
            allowableValues = {"BANK_ACCOUNT", "CARD_SETTLEMENT", "LOAN", "AUTO_TRANSFER"})
        String sourceType,
    @Schema(
            description = "반영 상태",
            example = "PARTIAL",
            allowableValues = {"COMPLETE", "PARTIAL"})
        String status,
    @Schema(description = "커버리지율(%). 홈 미표시, 판단보류 화면용", example = "61.00") BigDecimal coverageRate,
    @Schema(description = "마지막 동기화 시각(UTC). 홈 미표시, Dashboard용", example = "2025-07-15T00:14:00Z")
        Instant lastSyncedAt,
    @Schema(description = "임계(70%) 미만 여부", example = "true") boolean belowThreshold) {}
