package recovery30.server.recoveryoption.domain;

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

/** 자체 실행 계획 (recovery_self_action_plans). 셀프 액션 저장 화면에서 선택한 회복안을 실행 계획으로 저장한 것. */
@Entity
@Table(name = "recovery_self_action_plans")
@Getter
@Setter
@NoArgsConstructor
public class SelfActionPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private Long forecastRunId;

  @Column(nullable = false)
  private Long recoveryOptionId;

  @Column(columnDefinition = "TEXT")
  private String expectedEffectText;

  /** ACTIVE / ARCHIVED */
  @Column(nullable = false)
  private String status = "ACTIVE";

  @Column(updatable = false)
  private Instant savedAt;
}
