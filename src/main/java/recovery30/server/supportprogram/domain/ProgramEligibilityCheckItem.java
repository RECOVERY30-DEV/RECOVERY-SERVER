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

/**
 * 지원제도 자격 판정의 규칙 단위 결과 (recovery_program_eligibility_check_items). 지원사업 상세 화면에서 규칙마다 체크박스 + 개별 문구로
 * 표시한다. 부모({@link ProgramEligibilityCheck})와 마찬가지로 항상 참고용(advisory)이며 확정 판정이 아니다.
 */
@Entity
@Table(name = "recovery_program_eligibility_check_items")
@Getter
@Setter
@NoArgsConstructor
public class ProgramEligibilityCheckItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long checkId;

  @Column(nullable = false)
  private Long ruleId;

  /** LIKELY_PASS / NEEDS_REVIEW / LIKELY_FAIL / UNKNOWN */
  @Column(nullable = false)
  private String result;

  @Column(columnDefinition = "TEXT")
  private String noteText;

  @Column(name = "is_advisory", nullable = false)
  private boolean advisory = true;
}
