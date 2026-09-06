package recovery30.server.recoveryoption.internal;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.recoveryoption.domain.ScenarioOption;

/** 시나리오 ↔ 회복안 조인 저장소. */
public interface ScenarioOptionRepository extends JpaRepository<ScenarioOption, Long> {

  List<ScenarioOption> findByScenarioIdInOrderByIdAsc(Collection<Long> scenarioIds);
}
