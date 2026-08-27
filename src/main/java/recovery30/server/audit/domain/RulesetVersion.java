package recovery30.server.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 규칙셋 버전 마스터 (audit_ruleset_versions). 자격판정/예측 규칙의 논리 버전. {@code
 * recovery_support_programs.ruleset_version} 등이 참조한다.
 */
@Entity
@Table(name = "audit_ruleset_versions")
@Getter
@Setter
@NoArgsConstructor
public class RulesetVersion {

  @Id private String version;

  /** ELIGIBILITY / FORECAST */
  @Column(nullable = false)
  private String domain;

  private String description;

  private LocalDate releasedAt;

  @Column(name = "is_active", nullable = false)
  private boolean active = false;
}
