package recovery30.server.packet;

import java.time.Instant;
import recovery30.server.packet.domain.RecoveryPacket;

/** packet 슬라이스 통합테스트용 픽스처. */
public final class PacketFixtures {

  private PacketFixtures() {}

  public static RecoveryPacket packet(long businessId, long forecastRunId, int version) {
    RecoveryPacket p = new RecoveryPacket();
    p.setBusinessId(businessId);
    p.setForecastRunId(forecastRunId);
    p.setVersion(version);
    p.setSnapshotJson("{\"riskSnapshot\":{\"status\":\"위험\"}}");
    p.setStatus("DRAFT");
    p.setGeneratedAt(Instant.parse("2025-07-14T00:32:00Z"));
    return p;
  }
}
