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

/** 카드매출 정산 (source_card_settlements). 정산 예정액은 근시일 확정 항목으로 캘린더에 반영된다. */
@Entity
@Table(name = "source_card_settlements")
@Getter
@Setter
@NoArgsConstructor
public class CardSettlement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private String cardCompany;

  @Column(nullable = false)
  private LocalDate salesDate;

  @Column(nullable = false)
  private LocalDate settlementDate;

  @Column(nullable = false)
  private Long salesAmount;

  @Column(nullable = false)
  private Long feeAmount = 0L;

  @Column(nullable = false)
  private Long settlementAmount;

  /** CONFIRMED / EXPECTED */
  @Column(nullable = false)
  private String status;
}
