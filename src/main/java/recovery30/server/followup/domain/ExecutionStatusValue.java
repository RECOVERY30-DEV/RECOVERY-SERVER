package recovery30.server.followup.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 회복안별 실행 상태 ({@code recovery_execution_status.status}). */
@Schema(
    name = "ExecutionStatusValue",
    description =
        """
        회복안 실행 상태
        - NOT_STARTED: 시작 전
        - IN_PROGRESS: 진행 중
        - DONE: 완료
        - BLOCKED: 장애요인으로 중단 (blockerText 참고)
        """,
    enumAsRef = true)
public enum ExecutionStatusValue {
  NOT_STARTED,
  IN_PROGRESS,
  DONE,
  BLOCKED
}
