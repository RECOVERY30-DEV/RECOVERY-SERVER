package recovery30.server.forecast.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.forecast.domain.RiskDriverEvidence;

/** 위험 신호별 근거 거래 저장소. */
public interface RiskDriverEvidenceRepository extends JpaRepository<RiskDriverEvidence, Long> {

  List<RiskDriverEvidence> findByRiskDriverIdInOrderByRiskDriverIdAscIdAsc(
      List<Long> riskDriverIds);
}
