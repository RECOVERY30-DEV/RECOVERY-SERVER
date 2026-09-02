package recovery30.server.forecast.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 소스별 커버리지 반영 상태. DB에 저장되는 값이 아니라 {@code forecast_coverage.is_below_threshold}를 표시용으로 매핑한 것. */
@Schema(
    name = "CoverageStatus",
    description =
        """
        소스별 반영 상태 (is_below_threshold 매핑)
        - COMPLETE: 커버리지 임계(70%) 이상, 충분히 반영됨
        - PARTIAL: 커버리지 임계 미만, 일부 누락 가능 (판단보류 전환 근거)
        """,
    enumAsRef = true)
public enum CoverageStatus {
  COMPLETE,
  PARTIAL;

  public static CoverageStatus of(boolean belowThreshold) {
    return belowThreshold ? PARTIAL : COMPLETE;
  }
}
