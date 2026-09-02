package recovery30.server.followup.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 30/60/90일 사후 점검 일정 (recovery_followup_schedules). 사후점검 화면과 Recovery Packet "사후 점검 일정". 추적
 * 동의({@link #consentId}) 없이는 행을 만들 수 없다.
 */
@Entity
@Table(name = "recovery_followup_schedules")
@Getter
@Setter
@NoArgsConstructor
public class FollowupSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  private Long packetId;

  private Long forecastRunId;

  /** D30 / D60 / D90 */
  @Column(nullable = false)
  private String checkpoint;

  @Column(nullable = false)
  private LocalDate scheduledDate;

  /** SCHEDULED / DONE / SKIPPED */
  @Column(nullable = false)
  private String status = "SCHEDULED";

  @Column(nullable = false)
  private Long consentId;
}
