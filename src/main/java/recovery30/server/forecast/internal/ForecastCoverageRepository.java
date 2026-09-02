package recovery30.server.forecast.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.forecast.domain.ForecastCoverage;

/** 예측 실행 시점의 소스별 Coverage 스냅샷 저장소. */
public interface ForecastCoverageRepository extends JpaRepository<ForecastCoverage, Long> {

  List<ForecastCoverage> findByForecastRunIdOrderById(Long forecastRunId);
}
