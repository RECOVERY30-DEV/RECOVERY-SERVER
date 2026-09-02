package recovery30.server.forecast.domain;

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

/** 부족 원인 TOP N (forecast_risk_drivers). */
@Entity
@Table(name = "forecast_risk_drivers")
@Getter
@Setter
@NoArgsConstructor
public class RiskDriver {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long forecastRunId;

  @Column(name = "rank_no", nullable = false)
  private Integer rankNo;

  @Column(nullable = false)
  private String driverCode;

  @Column(nullable = false)
  private String title;

  private Long contributionAmount;

  /** 금액이 아닌 지표 표시용 (예: "-18%", "약 32% 감소"). */
  private String metricText;

  @Column(name = "is_estimating", nullable = false)
  private boolean estimating = false;

  private LocalDate occurrenceDate;

  /** 복수 발생일 표시용 (예: "11월 20일·25일 발생"). 정렬은 {@link #occurrenceDate} 사용. */
  private String occurrenceText;

  /** 영향 기간 표시용 (예: "6월 15일~28일 영향"). */
  private String impactPeriodText;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String assumptionText;

  private BigDecimal shapValue;
}
