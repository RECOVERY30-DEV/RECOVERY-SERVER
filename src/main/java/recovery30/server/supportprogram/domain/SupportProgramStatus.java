package recovery30.server.supportprogram.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 지원제도 상태 ({@code recovery_support_programs.status}). */
@Schema(
    name = "SupportProgramStatus",
    description =
        """
        지원제도 상태
        - ACTIVE: 신청 가능 (공고 진행 중)
        - CLOSED: 마감
        """,
    enumAsRef = true)
public enum SupportProgramStatus {
  ACTIVE,
  CLOSED
}
