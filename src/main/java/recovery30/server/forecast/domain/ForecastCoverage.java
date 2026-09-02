package recovery30.server.forecast.domain;

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
 * 예측 1회 시점의 소스별 Coverage 스냅샷 (forecast_coverage). 하나라도 {@link #belowThreshold}면 {@code
 * forecast_runs.status = HOLD}(판단보류)의 근거가 된다.
 */
@Entity
@Table(name = "forecast_coverage")
@Getter
@Setter
@NoArgsConstructor
public class ForecastCoverage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long forecastRunId;

  /** BANK_ACCOUNT / CARD_SETTLEMENT / LOAN / AUTO_TRANSFER */
  @Column(nullable = false)
  private String sourceType;

  private BigDecimal coverageRate;

  private Instant lastSyncedAt;

  @Column(name = "is_below_threshold", nullable = false)
  private boolean belowThreshold = false;
}
