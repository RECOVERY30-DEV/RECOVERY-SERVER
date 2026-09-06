package recovery30.server.supportprogram.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.supportprogram.domain.ProgramEligibilityCheck;

/** 지원제도 자격 판정 결과 저장소. */
public interface ProgramEligibilityCheckRepository
    extends JpaRepository<ProgramEligibilityCheck, Long> {

  Optional<ProgramEligibilityCheck> findByBusinessIdAndProgramId(Long businessId, Long programId);
}
