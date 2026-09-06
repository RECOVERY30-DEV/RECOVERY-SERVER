package recovery30.server.source.getdatasources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import recovery30.server.source.domain.SourceType;
import recovery30.server.source.domain.SyncStatus;

/**
 * 데이터 범위 확인 화면 "출처별 데이터 현황" 한 줄. 홈/Dashboard "분석 데이터 범위"에서도 재사용. 한글 라벨·"최근 6개월 거래내역" 같은 문구는 클라이언트가
 * 만든다.
 */
public record DataSourceView(
    @Schema(description = "소스 유형") SourceType sourceType,
    @Schema(description = "연동 기관/설명. nullable", example = "KB국민은행 · 신한은행") String institutionName,
    @Schema(description = "커버리지율(%). nullable", example = "61.00") BigDecimal coverageRate,
    @Schema(description = "반영 기간(개월)", example = "6") Integer periodMonths,
    @Schema(description = "마지막 동기화 시각(UTC). nullable", example = "2025-07-14T21:14:00Z")
        Instant lastSyncedAt,
    @Schema(description = "동기화 상태") SyncStatus syncStatus,
    @Schema(description = "커버리지 임계(70%) 미만 여부. 판단보류 전환 근거", example = "true")
        boolean belowThreshold) {}
