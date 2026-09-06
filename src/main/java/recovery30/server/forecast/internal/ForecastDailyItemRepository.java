package recovery30.server.forecast.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.forecast.domain.ForecastDailyItem;

/** 일자별 근거 라인 저장소. */
public interface ForecastDailyItemRepository extends JpaRepository<ForecastDailyItem, Long> {

  List<ForecastDailyItem> findByForecastDailyIdOrderByIdAsc(Long forecastDailyId);
}
