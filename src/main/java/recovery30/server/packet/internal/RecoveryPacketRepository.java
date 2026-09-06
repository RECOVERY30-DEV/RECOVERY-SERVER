package recovery30.server.packet.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.packet.domain.RecoveryPacket;

/** Recovery Packet 저장소. 수정은 새 버전 행 추가(덮어쓰기 금지). */
public interface RecoveryPacketRepository extends JpaRepository<RecoveryPacket, Long> {

  Optional<RecoveryPacket> findTopByBusinessIdOrderByVersionDesc(Long businessId);

  Optional<RecoveryPacket> findTopByBusinessIdAndForecastRunIdOrderByVersionDesc(
      Long businessId, Long forecastRunId);
}
