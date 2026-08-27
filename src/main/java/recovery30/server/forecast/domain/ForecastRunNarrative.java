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
 * 예측 실행별 서술 문구 (forecast_run_narratives). {@code forecast_risk_drivers}가 RISK 전용이라 담을 수 없는 텍스트를
 * 보관한다.
 *
 * <ul>
 *   <li>STATUS_LABEL — 홈 상태 배지 문구 ("안전상태" 등)
 *   <li>STABLE_REASON — 안정 상태 화면 "판단 근거"
 *   <li>RISK_NOTE — 위험 상태 부가 설명
 *   <li>STATE_CHANGE_HINT — "이런 경우 상태가 바뀔 수 있어요"
 *   <li>DISCLAIMER — 공통 고지문
 * </ul>
 */
@Entity
@Table(name = "forecast_run_narratives")
@Getter
@Setter
@NoArgsConstructor
public class ForecastRunNarrative {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long forecastRunId;

  /** STATUS_LABEL / STABLE_REASON / RISK_NOTE / STATE_CHANGE_HINT / DISCLAIMER */
  @Column(nullable = false)
  private String kind;

  @Column(nullable = false)
  private Integer seq = 0;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String text;
}
