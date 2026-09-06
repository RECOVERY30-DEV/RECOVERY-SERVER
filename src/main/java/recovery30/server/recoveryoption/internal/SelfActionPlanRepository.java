package recovery30.server.recoveryoption.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.recoveryoption.domain.SelfActionPlan;

/** 자체 실행 계획 저장소. */
public interface SelfActionPlanRepository extends JpaRepository<SelfActionPlan, Long> {

  List<SelfActionPlan> findByForecastRunIdOrderByIdAsc(Long forecastRunId);
}
