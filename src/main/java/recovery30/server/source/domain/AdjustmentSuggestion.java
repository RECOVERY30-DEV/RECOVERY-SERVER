package recovery30.server.source.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 반복 패턴 추정 후보 (source_adjustment_suggestions). 정보 보정 화면 "반복 패턴 추정 후보" ("매월 15일 현금 매출 약 120만원" 등).
 * MVP는 목데이터로 시드한다.
 */
@Entity
@Table(name = "source_adjustment_suggestions")
@Getter
@Setter
@NoArgsConstructor
public class AdjustmentSuggestion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  /** CASH_SALES / EXTERNAL_FUND / EXPECTED_INCOME / EXPECTED_EXPENSE */
  @Column(nullable = false)
  private String adjustmentType;

  private Long suggestedAmount;

  private String suggestedRule;

  private String evidenceText;

  private BigDecimal confidence;

  /** PROPOSED / ACCEPTED / REJECTED */
  @Column(nullable = false)
  private String status = "PROPOSED";

  private Long acceptedAdjustmentId;

  @Column(updatable = false)
  private Instant createdAt;
}
