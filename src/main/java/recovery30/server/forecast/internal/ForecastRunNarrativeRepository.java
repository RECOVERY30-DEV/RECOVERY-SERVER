package recovery30.server.forecast.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.forecast.domain.ForecastRunNarrative;

/** 예측 실행별 서술 문구 저장소. */
public interface ForecastRunNarrativeRepository extends JpaRepository<ForecastRunNarrative, Long> {

  List<ForecastRunNarrative> findByForecastRunIdOrderByKindAscSeqAsc(Long forecastRunId);
}
