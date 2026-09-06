package recovery30.server.recoveryoption.internal;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.recoveryoption.domain.SelfActionItem;

/** 자체 실행 준비 항목 저장소. */
public interface SelfActionItemRepository extends JpaRepository<SelfActionItem, Long> {

  List<SelfActionItem> findBySelfActionPlanIdOrderByIdAsc(Long selfActionPlanId);

  List<SelfActionItem> findBySelfActionPlanIdInOrderByIdAsc(Collection<Long> planIds);
}
