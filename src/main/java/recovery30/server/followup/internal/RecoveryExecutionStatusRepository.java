package recovery30.server.followup.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.followup.domain.RecoveryExecutionStatus;

/** 회복안별 실행 상태 저장소. */
public interface RecoveryExecutionStatusRepository
    extends JpaRepository<RecoveryExecutionStatus, Long> {

  List<RecoveryExecutionStatus> findByBusinessIdOrderByIdAsc(Long businessId);
}
