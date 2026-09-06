package recovery30.server.forecast.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.forecast.domain.ForecastDaily;

/** 30일 일자별 현금흐름 저장소. */
public interface ForecastDailyRepository extends JpaRepository<ForecastDaily, Long> {

  List<ForecastDaily> findByForecastRunIdOrderByTargetDateAsc(Long forecastRunId);

  Optional<ForecastDaily> findByForecastRunIdAndTargetDate(
      Long forecastRunId, LocalDate targetDate);
}
