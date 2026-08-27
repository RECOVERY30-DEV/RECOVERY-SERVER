package recovery30.server.forecast.domain;

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

/** 30일 현금흐름 캘린더의 하루치 행 (forecast_daily). */
@Entity
@Table(name = "forecast_daily")
@Getter
@Setter
@NoArgsConstructor
public class ForecastDaily {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long forecastRunId;

  @Column(nullable = false)
  private LocalDate targetDate;

  @Column(nullable = false)
  private Integer dDay;

  @Column(nullable = false)
  private Long openingBalance;

  @Column(nullable = false)
  private Long confirmedInflow = 0L;

  @Column(nullable = false)
  private Long confirmedOutflow = 0L;

  @Column(nullable = false)
  private Long expectedInflowMin = 0L;

  @Column(nullable = false)
  private Long expectedInflowMax = 0L;

  @Column(nullable = false)
  private Long expectedOutflowMin = 0L;

  @Column(nullable = false)
  private Long expectedOutflowMax = 0L;

  @Column(nullable = false)
  private Long adjustmentNet = 0L;

  @Column(nullable = false)
  private Long closingBalanceConservative;

  @Column(nullable = false)
  private Long closingBalanceExpected;

  @Column(nullable = false)
  private Long closingBalanceOptimistic;

  @Column(name = "is_shortfall", nullable = false)
  private boolean shortfall = false;

  @Column(name = "is_holiday", nullable = false)
  private boolean holiday = false;

  private String holidayShiftNote;
}
