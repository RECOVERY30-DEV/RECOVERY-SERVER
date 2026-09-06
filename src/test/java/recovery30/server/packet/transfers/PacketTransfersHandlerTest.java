package recovery30.server.packet.transfers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.packet.PacketFixtures;
import recovery30.server.packet.domain.RecoveryPacket;
import recovery30.server.packet.internal.RecoveryPacketRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PacketTransfersHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RecoveryPacketRepository recoveryPacketRepository;

  private long seedPacket() {
    return recoveryPacketRepository.save(PacketFixtures.packet(1L, 4821L, 1)).getId();
  }

  @Test
  void 전송하면_이력이_생기고_Packet이_SENT가_된다() throws Exception {
    long packetId = seedPacket();

    mockMvc
        .perform(
            post("/api/packets/{packetId}/transfers", packetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"counselorId\":2,\"channel\":\"PHONE\",\"consentId\":3,"
                        + "\"scope\":{\"riskSnapshot\":true,\"adjustments\":true}}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.channel").value("PHONE"))
        .andExpect(jsonPath("$.data.consentId").value(3))
        .andExpect(jsonPath("$.data.scope.riskSnapshot").value(true));

    RecoveryPacket packet = recoveryPacketRepository.findById(packetId).orElseThrow();
    org.assertj.core.api.Assertions.assertThat(packet.getStatus()).isEqualTo("SENT");
    org.assertj.core.api.Assertions.assertThat(packet.getSentAt()).isNotNull();

    mockMvc
        .perform(get("/api/packets/{packetId}/transfers", packetId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  void consentId가_없으면_400을_반환한다() throws Exception {
    long packetId = seedPacket();
    mockMvc
        .perform(
            post("/api/packets/{packetId}/transfers", packetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channel\":\"PHONE\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("COMMON_400"));
  }

  @Test
  void 존재하지_않는_Packet이면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/packets/{packetId}/transfers", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("PACKET_404_1"));
  }
}
