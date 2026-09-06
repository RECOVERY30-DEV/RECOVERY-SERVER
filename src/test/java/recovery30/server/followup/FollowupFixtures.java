package recovery30.server.followup;

import java.time.Instant;
import java.time.LocalDate;
import recovery30.server.followup.domain.FollowupResult;
import recovery30.server.followup.domain.FollowupSchedule;
import recovery30.server.followup.domain.RecoveryExecutionStatus;

/** followup 슬라이스 통합테스트용 엔티티 픽스처. */
public final class FollowupFixtures {

  private FollowupFixtures() {}

  public static FollowupSchedule schedule(
      long businessId, String checkpoint, LocalDate scheduledDate, String status) {
    FollowupSchedule s = new FollowupSchedule();
    s.setBusinessId(businessId);
    s.setForecastRunId(1L);
    s.setCheckpoint(checkpoint);
    s.setScheduledDate(scheduledDate);
    s.setStatus(status);
    s.setConsentId(1L);
    return s;
  }

  public static FollowupResult result(long scheduleId, String balanceRecovered, String riskStatus) {
    FollowupResult r = new FollowupResult();
    r.setFollowupScheduleId(scheduleId);
    r.setBalanceRecovered(balanceRecovered);
    r.setDelinquency(false);
    r.setBaselineBalance(540_000L);
    r.setCurrentBalance(2_180_000L);
    r.setRecoveryAmount(1_640_000L);
    r.setLatestForecastRunId(1L);
    r.setRiskStatus(riskStatus);
    r.setRecordedAt(Instant.parse("2025-08-14T02:00:00Z"));
    return r;
  }

  public static RecoveryExecutionStatus executionStatus(
      long businessId, long recoveryOptionId, String status, String blockerText) {
    RecoveryExecutionStatus e = new RecoveryExecutionStatus();
    e.setBusinessId(businessId);
    e.setRecoveryOptionId(recoveryOptionId);
    e.setForecastRunId(1L);
    e.setStatus(status);
    e.setBlockerText(blockerText);
    e.setUpdatedAt(Instant.parse("2025-08-10T05:00:00Z"));
    return e;
  }
}
