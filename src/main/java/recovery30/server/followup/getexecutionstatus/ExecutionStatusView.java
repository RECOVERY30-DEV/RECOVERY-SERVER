package recovery30.server.followup.getexecutionstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.followup.domain.ExecutionStatusValue;
import recovery30.server.followup.domain.RecoveryExecutionStatus;

/** 사후점검 화면 "회복안 실행 상태" 한 줄 (회복안별 진행 상황 + 장애요인). */
public record ExecutionStatusView(
    @Schema(description = "실행 상태 행 ID", example = "1") Long id,
    @Schema(description = "대상 회복안 ID", example = "2") Long recoveryOptionId,
    @Schema(description = "실행 상태") ExecutionStatusValue status,
    @Schema(description = "장애요인 설명 (status=BLOCKED 일 때). nullable", example = "담당자 확인 필요")
        String blockerText,
    @Schema(description = "연결된 예측 실행 ID. nullable", example = "1") Long forecastRunId,
    @Schema(description = "상태 갱신 시각(UTC)", example = "2025-08-10T05:00:00Z") Instant updatedAt) {

  public static ExecutionStatusView of(RecoveryExecutionStatus e) {
    return new ExecutionStatusView(
        e.getId(),
        e.getRecoveryOptionId(),
        ExecutionStatusValue.valueOf(e.getStatus()),
        e.getBlockerText(),
        e.getForecastRunId(),
        e.getUpdatedAt());
  }
}
