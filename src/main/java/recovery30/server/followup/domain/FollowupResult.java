package recovery30.server.followup.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 사후 점검 결과 (recovery_followup_results). 사후점검 화면 "잔액 회복 현황". schedule당 1건. MVP는 목데이터로 시연한다. */
@Entity
@Table(name = "recovery_followup_results")
@Getter
@Setter
@NoArgsConstructor
public class FollowupResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long followupScheduleId;

  /** YES / PARTIAL / NO */
  private String balanceRecovered;

  @Column(name = "has_delinquency", nullable = false)
  private boolean delinquency = false;

  private Long baselineBalance;

  private Long currentBalance;

  private Long recoveryAmount;

  private Long latestForecastRunId;

  /** RISK / STABLE / HOLD */
  private String riskStatus;

  @Column(updatable = false)
  private Instant recordedAt;
}
