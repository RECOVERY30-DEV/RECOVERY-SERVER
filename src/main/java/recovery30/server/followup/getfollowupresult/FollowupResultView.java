package recovery30.server.followup.getfollowupresult;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import recovery30.server.followup.domain.BalanceRecovered;
import recovery30.server.followup.domain.FollowupResult;

/** 사후점검 화면 "잔액 회복 현황" 카드. 점검 일정 1건에 대응하는 결과. */
public record FollowupResultView(
    @Schema(description = "점검 일정 ID", example = "1") Long scheduleId,
    @Schema(description = "잔액 회복 여부. nullable") BalanceRecovered balanceRecovered,
    @Schema(description = "연체 발생 여부", example = "false") boolean delinquency,
    @Schema(description = "점검 시작 시점 잔액(원). nullable", example = "540000") Long baselineBalance,
    @Schema(description = "현재 잔액(원). nullable", example = "2180000") Long currentBalance,
    @Schema(description = "회복 금액(현재-기준, 원). nullable", example = "1640000") Long recoveryAmount,
    @Schema(description = "점검 시점 최신 예측 실행 ID. nullable", example = "1") Long latestForecastRunId,
    @Schema(description = "점검 시점 위험 상태 (RISK/STABLE/HOLD). nullable", example = "STABLE")
        String riskStatus,
    @Schema(description = "결과 기록 시각(UTC)", example = "2025-08-14T02:00:00Z") Instant recordedAt) {

  public static FollowupResultView of(FollowupResult r) {
    return new FollowupResultView(
        r.getFollowupScheduleId(),
        r.getBalanceRecovered() == null ? null : BalanceRecovered.valueOf(r.getBalanceRecovered()),
        r.isDelinquency(),
        r.getBaselineBalance(),
        r.getCurrentBalance(),
        r.getRecoveryAmount(),
        r.getLatestForecastRunId(),
        r.getRiskStatus(),
        r.getRecordedAt());
  }
}
