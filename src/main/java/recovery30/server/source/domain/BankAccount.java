package recovery30.server.source.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동된 은행 계좌 (source_bank_accounts). */
@Entity
@Table(name = "source_bank_accounts")
@Getter
@Setter
@NoArgsConstructor
public class BankAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private String institutionName;

  private String accountNoMasked;

  @Column(updatable = false)
  private Instant createdAt;
}
