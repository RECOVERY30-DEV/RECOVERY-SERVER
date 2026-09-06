package recovery30.server.followup.getfollowups;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import recovery30.server.followup.domain.FollowupCheckpoint;
import recovery30.server.followup.domain.FollowupSchedule;
import recovery30.server.followup.domain.FollowupStatus;

/** 사후점검 화면의 D30/D60/D90 점검 일정 한 줄. */
public record FollowupView(
    @Schema(description = "점검 일정 ID", example = "1") Long id,
    @Schema(description = "점검 시점") FollowupCheckpoint checkpoint,
    @Schema(description = "예정일", example = "2025-08-14") LocalDate scheduledDate,
    @Schema(description = "일정 상태") FollowupStatus status,
    @Schema(description = "연결된 예측 실행 ID. nullable", example = "1") Long forecastRunId,
    @Schema(description = "연결된 Recovery Packet ID. nullable", example = "1") Long packetId,
    @Schema(description = "점검 결과가 기록되어 있는지", example = "true") boolean hasResult) {

  public static FollowupView of(FollowupSchedule s, boolean hasResult) {
    return new FollowupView(
        s.getId(),
        FollowupCheckpoint.valueOf(s.getCheckpoint()),
        s.getScheduledDate(),
        FollowupStatus.valueOf(s.getStatus()),
        s.getForecastRunId(),
        s.getPacketId(),
        hasResult);
  }
}
