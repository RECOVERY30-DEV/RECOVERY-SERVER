package recovery30.server.packet.getpacket;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.packet.PacketFixtures;
import recovery30.server.packet.internal.RecoveryPacketRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetLatestPacketHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RecoveryPacketRepository recoveryPacketRepository;

  @Test
  void 가장_높은_버전을_반환한다() throws Exception {
    recoveryPacketRepository.save(PacketFixtures.packet(7L, 4821L, 1));
    recoveryPacketRepository.save(PacketFixtures.packet(7L, 4821L, 2));

    mockMvc
        .perform(get("/api/businesses/{businessId}/packets/latest", 7))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").value(2));
  }

  @Test
  void Packet이_없으면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/businesses/{businessId}/packets/latest", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("PACKET_404_1"));
  }
}
