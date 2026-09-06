package recovery30.server.followup.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 사후 점검 시점 ({@code recovery_followup_schedules.checkpoint}). */
@Schema(
    name = "FollowupCheckpoint",
    description =
        """
        사후 점검 시점
        - D30: 실행 후 30일
        - D60: 실행 후 60일
        - D90: 실행 후 90일
        """,
    enumAsRef = true)
public enum FollowupCheckpoint {
  D30,
  D60,
  D90
}
