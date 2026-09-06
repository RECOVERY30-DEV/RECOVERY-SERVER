package recovery30.server.source.adjustments;

import io.swagger.v3.oas.annotations.media.Schema;

/** 보정값 적용(재계산 트리거) 결과. */
public record AppliedAdjustmentsView(
    @Schema(description = "SAVED 로 전환된 보정값 개수", example = "2") int appliedCount,
    @Schema(description = "반영 대상 예측 실행 ID. 최신 예측이 없으면 null", example = "1") Long appliedRunId) {}
