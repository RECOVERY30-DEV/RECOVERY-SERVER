package recovery30.server.source.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 자동이체/반복 고정비 (source_recurring_expenses). 임차료·급여·공과금·세금·보험 등. */
@Entity
@Table(name = "source_recurring_expenses")
@Getter
@Setter
@NoArgsConstructor
public class RecurringExpense {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  /** RENT / UTILITY / PAYROLL / INSURANCE / TAX / SUBSCRIPTION */
  @Column(nullable = false)
  private String expenseType;

  @Column(nullable = false)
  private Long amount;

  private String recurrenceRule;

  private LocalDate nextDueDate;

  @Column(name = "is_auto_debit", nullable = false)
  private boolean autoDebit = true;

  private BigDecimal confidence;
}
