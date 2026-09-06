package recovery30.server.followup.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 사후 점검 일정 상태 ({@code recovery_followup_schedules.status}). */
@Schema(
    name = "FollowupStatus",
    description =
        """
        사후 점검 일정 상태
        - SCHEDULED: 예정
        - DONE: 점검 완료 (결과 기록됨)
        - SKIPPED: 건너뜀
        """,
    enumAsRef = true)
public enum FollowupStatus {
  SCHEDULED,
  DONE,
  SKIPPED
}
