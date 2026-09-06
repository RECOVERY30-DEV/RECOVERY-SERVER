package recovery30.server.source.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 보정값 상태 ({@code source_adjustments.status}). */
@Schema(
    name = "AdjustmentStatus",
    description =
        """
        보정값 상태
        - DRAFT: 입력만 됨, 예측 미반영
        - SAVED: 저장됨, 재계산 시 예측에 반영 (applied_run_id 기록)
        - DISCARDED: 삭제됨
        """,
    enumAsRef = true)
public enum AdjustmentStatus {
  DRAFT,
  SAVED,
  DISCARDED
}
