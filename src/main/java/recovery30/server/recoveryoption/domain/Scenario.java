package recovery30.server.recoveryoption.domain;

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

/** baseline 또는 회복안 적용 시뮬레이션 결과 (recovery_scenarios). */
@Entity
@Table(name = "recovery_scenarios")
@Getter
@Setter
@NoArgsConstructor
public class Scenario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long forecastRunId;

  /** BASELINE / SIMULATED */
  @Column(nullable = false)
  private String scenarioType;

  private LocalDate firstShortfallDate;

  private Long minBalance;

  private Integer deltaDays;

  private Long deltaMinBalance;

  private Long monthlyPaymentDelta;

  @Column(columnDefinition = "TEXT")
  private String note;
}
