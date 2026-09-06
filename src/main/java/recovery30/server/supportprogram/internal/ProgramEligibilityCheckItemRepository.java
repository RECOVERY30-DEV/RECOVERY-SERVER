package recovery30.server.supportprogram.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.supportprogram.domain.ProgramEligibilityCheckItem;

/** 지원제도 규칙별 판정 결과 저장소. */
public interface ProgramEligibilityCheckItemRepository
    extends JpaRepository<ProgramEligibilityCheckItem, Long> {

  List<ProgramEligibilityCheckItem> findByCheckIdOrderByIdAsc(Long checkId);
}
