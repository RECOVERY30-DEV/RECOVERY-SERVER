package recovery30.server.recoveryoption.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 회복안 실행 난이도 ({@code recovery_options.difficulty}). nullable. */
@Schema(
    name = "RecoveryOptionDifficulty",
    description =
        """
        회복안 실행 난이도
        - LOW: 당사자 간 협의로 가능
        - MID: 상담 후 심사 필요
        - HIGH: 서류 준비·심사 필요
        """,
    enumAsRef = true)
public enum RecoveryOptionDifficulty {
  LOW,
  MID,
  HIGH
}
