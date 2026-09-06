package recovery30.server.recoveryoption.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 자체 실행 준비 항목 상태 ({@code recovery_self_action_items.status}). */
@Schema(
    name = "SelfActionItemStatus",
    description =
        """
        준비 항목 상태
        - PENDING: 미완료
        - DONE: 완료
        """,
    enumAsRef = true)
public enum SelfActionItemStatus {
  PENDING,
  DONE
}
