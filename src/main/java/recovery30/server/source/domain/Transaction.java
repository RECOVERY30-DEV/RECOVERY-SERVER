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

/** 계좌 거래 내역 (source_transactions). 30일 현금흐름 예측의 히스토리 원천. */
@Entity
@Table(name = "source_transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private Long accountId;

  @Column(nullable = false)
  private LocalDate txnDate;

  /** I(입금) / O(출금) */
  @Column(nullable = false)
  private String direction;

  @Column(nullable = false)
  private Long amount;

  private Long balanceAfter;

  private String category;

  private String counterparty;

  @Column(name = "is_confirmed", nullable = false)
  private boolean confirmed = true;
}
