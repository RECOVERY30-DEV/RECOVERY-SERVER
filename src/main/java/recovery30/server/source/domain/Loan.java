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

/** 대출 (source_loans). */
@Entity
@Table(name = "source_loans")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private String institution;

  private String loanType;

  @Column(nullable = false)
  private Long outstandingBalance;

  private BigDecimal interestRate;

  private String rateType;

  private String repaymentType;

  @Column(updatable = false)
  private Instant createdAt;
}
