package recovery30.server.recoveryoption.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 시나리오에 적용된 회복안 조합 (recovery_scenario_options, N:M 조인). */
@Entity
@Table(name = "recovery_scenario_options")
@Getter
@Setter
@NoArgsConstructor
public class ScenarioOption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long scenarioId;

  @Column(nullable = false)
  private Long recoveryOptionId;
}
