package recovery30.server.forecast.internal;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.forecast.domain.RiskDriver;

/** 부족 원인(위험 신호) 저장소. */
public interface ForecastRiskDriverRepository extends JpaRepository<RiskDriver, Long> {

  /** 예측 실행의 위험 신호를 rank 순으로. {@code Pageable.unpaged()}면 전체. */
  List<RiskDriver> findByForecastRunIdOrderByRankNo(Long forecastRunId, Pageable pageable);
}
