package recovery30.server.recoveryoption.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.recoveryoption.domain.UserOptionSelection;

/** 고객이 선택한 회복안 저장소. */
public interface UserOptionSelectionRepository extends JpaRepository<UserOptionSelection, Long> {

  List<UserOptionSelection> findByForecastRunIdOrderByIdAsc(Long forecastRunId);

  void deleteByForecastRunId(Long forecastRunId);
}
