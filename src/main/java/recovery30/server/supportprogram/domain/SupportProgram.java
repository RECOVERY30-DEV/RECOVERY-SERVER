package recovery30.server.supportprogram.domain;

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

/** 지원제도 (recovery_support_programs). 예: 소상공인119Plus, 햇살론119. */
@Entity
@Table(name = "recovery_support_programs")
@Getter
@Setter
@NoArgsConstructor
public class SupportProgram {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String programCode;

  @Column(nullable = false)
  private String name;

  private String agency;

  @Column(columnDefinition = "TEXT")
  private String supportContent;

  private Long limitAmount;

  private String interestRateText;

  private String termText;

  private LocalDate applyDeadline;

  private String applyUrl;

  private String officialSourceUrl;

  private String rulesetVersion;

  /** ACTIVE / CLOSED */
  @Column(nullable = false)
  private String status = "ACTIVE";
}
