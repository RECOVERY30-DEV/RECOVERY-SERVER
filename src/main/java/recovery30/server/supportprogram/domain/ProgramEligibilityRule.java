package recovery30.server.supportprogram.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 지원제도 자격요건 항목 (recovery_program_eligibility_rules). */
@Entity
@Table(name = "recovery_program_eligibility_rules")
@Getter
@Setter
@NoArgsConstructor
public class ProgramEligibilityRule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long programId;

  @Column(nullable = false)
  private String ruleCode;

  @Column(nullable = false)
  private String label;

  /** 규칙엔진 입력값 (JSON 원문). */
  @JdbcTypeCode(SqlTypes.JSON)
  private String ruleExpression;

  /** AUTO / COUNSELOR_ONLY */
  @Column(nullable = false)
  private String evaluationType;
}
