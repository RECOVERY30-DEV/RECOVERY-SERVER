package recovery30.server.source.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 보정값 (source_adjustments). 현금매출/타행·외부자금/예정수입/예정지출 입력 4개 화면을 {@link #adjustmentType} 하나로 통합한다.
 *
 * <p>시나리오 반영 규칙: {@code certainty = CONFIRMED} 는 예상·낙관 시나리오 모두 반영, {@code certainty = ESTIMATED} 는
 * 낙관 시나리오에만 반영하고 보수적 시나리오에서는 제외한다.
 */
@Entity
@Table(name = "source_adjustments")
@Getter
@Setter
@NoArgsConstructor
public class Adjustment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  /** CASH_SALES / EXTERNAL_FUND / EXPECTED_INCOME / EXPECTED_EXPENSE */
  @Column(nullable = false)
  private String adjustmentType;

  /** I / O */
  @Column(nullable = false)
  private String direction;

  @Column(nullable = false)
  private Long amount;

  @Column(nullable = false)
  private LocalDate expectedDate;

  /** CONFIRMED / ESTIMATED */
  @Column(nullable = false)
  private String certainty;

  private String recurrenceRule;

  /** 예정지출의 '지출 항목' */
  private String expenseCategory;

  /** 타행·외부자금의 '자금 출처' */
  private String fundSource;

  private String memo;

  /** DRAFT / SAVED / DISCARDED */
  @Column(nullable = false)
  private String status = "DRAFT";

  private Long appliedRunId;

  @Column(updatable = false)
  private Instant createdAt;

  private Instant updatedAt;
}
