package recovery30.server.recoveryoption.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 자체 실행 계획 상태 ({@code recovery_self_action_plans.status}). */
@Schema(
    name = "SelfActionPlanStatus",
    description =
        """
        자체 실행 계획 상태
        - ACTIVE: 진행 중
        - ARCHIVED: 보관됨 (새 계획으로 대체 등)
        """,
    enumAsRef = true)
public enum SelfActionPlanStatus {
  ACTIVE,
  ARCHIVED
}
