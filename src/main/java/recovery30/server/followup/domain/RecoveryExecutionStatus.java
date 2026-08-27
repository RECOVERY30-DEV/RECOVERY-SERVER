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

/** 회복안별 실행 상태 (recovery_execution_status). 사후점검 화면 "회복안 실행 상태" ("진행중 · 장애요인 담당자 확인 필요"). */
@Entity
@Table(name = "recovery_execution_status")
@Getter
@Setter
@NoArgsConstructor
public class RecoveryExecutionStatus {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private Long recoveryOptionId;

  private Long forecastRunId;

  /** NOT_STARTED / IN_PROGRESS / DONE / BLOCKED */
  @Column(nullable = false)
  private String status = "NOT_STARTED";

  private String blockerText;

  private Instant updatedAt;
}
