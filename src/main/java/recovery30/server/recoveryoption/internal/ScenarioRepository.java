package recovery30.server.recoveryoption.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.recoveryoption.domain.Scenario;

/** 시나리오(baseline + 시뮬) 저장소. */
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

  List<Scenario> findByForecastRunIdOrderByIdAsc(Long forecastRunId);
}
