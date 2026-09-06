package recovery30.server.supportprogram.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.supportprogram.domain.ProgramEligibilityRule;

/** 지원제도 자격요건 항목 저장소. */
public interface ProgramEligibilityRuleRepository
    extends JpaRepository<ProgramEligibilityRule, Long> {

  List<ProgramEligibilityRule> findByProgramIdOrderByIdAsc(Long programId);
}
