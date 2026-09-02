package recovery30.server.source.domain;

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

/** 대출 원리금 상환 일정 (source_loan_schedules). 30일 캘린더의 확정 유출 항목. */
@Entity
@Table(name = "source_loan_schedules")
@Getter
@Setter
@NoArgsConstructor
public class LoanSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long loanId;

  @Column(nullable = false)
  private LocalDate dueDate;

  @Column(nullable = false)
  private Long principalAmount;

  @Column(nullable = false)
  private Long interestAmount = 0L;

  @Column(nullable = false)
  private Long totalAmount;

  /** SCHEDULED / PAID / OVERDUE */
  @Column(nullable = false)
  private String status = "SCHEDULED";
}
