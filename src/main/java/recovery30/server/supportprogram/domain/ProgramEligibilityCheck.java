package recovery30.server.supportprogram.domain;

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

/** 지원제도 자격 판정 결과 (recovery_program_eligibility_checks). 항상 참고용(advisory)이며 확정 판정이 아니다. */
@Entity
@Table(name = "recovery_program_eligibility_checks")
@Getter
@Setter
@NoArgsConstructor
public class ProgramEligibilityCheck {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private Long programId;

  private Long forecastRunId;

  /** LIKELY_PASS / NEEDS_REVIEW / LIKELY_FAIL / UNKNOWN */
  @Column(nullable = false)
  private String result;

  @Column(columnDefinition = "TEXT")
  private String reasonText;

  @Column(name = "is_advisory", nullable = false)
  private boolean advisory = true;

  private String rulesetVersion;

  @Column(updatable = false)
  private Instant checkedAt;
}
