package recovery30.server.source.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.source.domain.AdjustmentSuggestion;

/** 반복 패턴 추정 후보 저장소 (source_adjustment_suggestions). */
public interface AdjustmentSuggestionRepository extends JpaRepository<AdjustmentSuggestion, Long> {

  List<AdjustmentSuggestion> findByBusinessIdOrderByIdAsc(Long businessId);

  Optional<AdjustmentSuggestion> findByIdAndBusinessId(Long id, Long businessId);
}
