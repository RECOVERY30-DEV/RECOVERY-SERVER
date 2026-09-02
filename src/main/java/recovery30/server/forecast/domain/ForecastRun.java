package recovery30.server.forecast.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 예측 1회 실행 (forecast_runs). 30일 현금흐름 캘린더/원인/시나리오 화면 헤더 수치의 원천. */
@Entity
@Table(name = "forecast_runs")
@Getter
@Setter
@NoArgsConstructor
public class ForecastRun {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private Long consentId;

  @Column(nullable = false)
  private LocalDate baseDate;

  @Column(nullable = false)
  private Integer horizonDays = 30;

  /** RISK / STABLE / HOLD */
  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private String confidenceLevel = "MEDIUM";

  private BigDecimal coverageOverall;

  private LocalDate firstShortfallDate;

  private Integer daysToShortfall;

  private Long minBalanceConservative;

  private Long minBalanceExpected;

  private Long minBalanceOptimistic;

  private Long shortfallAmountMin;

  private Long shortfallAmountMax;

  @Column(name = "is_buffer_met", nullable = false)
  private boolean bufferMet = false;

  @Column(nullable = false)
  private String modelVersion;

  @Column(nullable = false)
  private String rulesetVersion;

  @Column(nullable = false)
  private String triggeredBy;

  @Column(updatable = false)
  private Instant createdAt;
}
