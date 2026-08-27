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

/** 회복안 마스터 (recovery_options). 예: 상환일 변경, 지원제도 이용. */
@Entity
@Table(name = "recovery_options")
@Getter
@Setter
@NoArgsConstructor
public class RecoveryOption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String optionCode;

  /** FINANCIAL_CONSULT / SELF_ACTION / SUPPORT_PROGRAM */
  @Column(nullable = false)
  private String category;

  @Column(columnDefinition = "TEXT")
  private String expectedEffectText;

  @Column(columnDefinition = "TEXT")
  private String monthlyBurdenChangeText;

  @Column(columnDefinition = "TEXT")
  private String preconditionText;

  /** LOW / MID / HIGH */
  private String difficulty;

  @Column(nullable = false)
  private boolean requiresReview = false;

  @Column(columnDefinition = "TEXT")
  private String disclaimer;
}
