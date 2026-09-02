package recovery30.server.forecast.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.forecast.domain.ForecastRun;

/** 예측 실행 저장소. */
public interface ForecastRunRepository extends JpaRepository<ForecastRun, Long> {

  /** 사업자의 가장 최근 예측 실행 1건 (기준일 → 생성시각 내림차순). */
  Optional<ForecastRun> findTopByBusinessIdOrderByBaseDateDescCreatedAtDesc(Long businessId);

  boolean existsByBusinessId(Long businessId);
}
