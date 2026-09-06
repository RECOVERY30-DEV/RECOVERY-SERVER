package recovery30.server.packet.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.packet.domain.PacketTransfer;

/** Packet 전송 이력 저장소. */
public interface PacketTransferRepository extends JpaRepository<PacketTransfer, Long> {

  List<PacketTransfer> findByPacketIdOrderByIdAsc(Long packetId);
}
