package recovery30.server.forecast.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 부족 원인별 근거 거래 (forecast_risk_driver_evidence). 원인 상세 화면 "근거 거래" ("신한카드 정산 5건 · 6월 2일~11일"). */
@Entity
@Table(name = "forecast_risk_driver_evidence")
@Getter
@Setter
@NoArgsConstructor
public class RiskDriverEvidence {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long riskDriverId;

  /** TRANSACTION / CARD_SETTLEMENT / LOAN_SCHEDULE / RECURRING */
  private String refType;

  private Long refId;

  @Column(nullable = false)
  private String label;

  private String periodText;
}
