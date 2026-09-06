package recovery30.server.recoveryoption.internal;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.recoveryoption.domain.RecoveryOption;

/** 회복안 마스터 저장소. */
public interface RecoveryOptionRepository extends JpaRepository<RecoveryOption, Long> {

  List<RecoveryOption> findAllByOrderByIdAsc();

  long countByIdIn(Collection<Long> ids);
}
