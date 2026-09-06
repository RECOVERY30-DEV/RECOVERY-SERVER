package recovery30.server.source.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.source.domain.Adjustment;

/** 사용자 보정값 저장소 (source_adjustments). */
public interface AdjustmentRepository extends JpaRepository<Adjustment, Long> {

  List<Adjustment> findByBusinessIdOrderByExpectedDateAscIdAsc(Long businessId);

  Optional<Adjustment> findByIdAndBusinessId(Long id, Long businessId);

  List<Adjustment> findByBusinessIdAndStatus(Long businessId, String status);
}
