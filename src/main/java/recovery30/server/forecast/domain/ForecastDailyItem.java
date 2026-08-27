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

/**
 * 하루치 예측의 근거 라인 (forecast_daily_items). Dashboard "일자별 현금흐름"과 안정 상태 "일자별 근거 바텀시트"의 각 줄. 모든 금액은
 * {@link #refType}/{@link #refId}로 원천 레코드까지 역추적 가능해야 한다 (근거 공개 원칙).
 */
@Entity
@Table(name = "forecast_daily_items")
@Getter
@Setter
@NoArgsConstructor
public class ForecastDailyItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long forecastDailyId;

  /** CONFIRMED / EXPECTED / ADJUSTMENT */
  @Column(nullable = false)
  private String itemKind;

  @Column(nullable = false)
  private String label;

  private String subLabel;

  /** I / O */
  @Column(nullable = false)
  private String direction;

  @Column(nullable = false)
  private Long amountMin;

  @Column(nullable = false)
  private Long amountMax;

  /** TRANSACTION / CARD_SETTLEMENT / LOAN_SCHEDULE / RECURRING / ADJUSTMENT */
  private String refType;

  private Long refId;
}
