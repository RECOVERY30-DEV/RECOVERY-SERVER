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
import recovery30.server.packet.domain.RecoveryPacket;
import recovery30.server.packet.internal.RecoveryPacketRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetPacketHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RecoveryPacketRepository recoveryPacketRepository;

  @Test
  void 아이디로_snapshot을_파싱해_반환한다() throws Exception {
    RecoveryPacket p = recoveryPacketRepository.save(PacketFixtures.packet(1L, 4821L, 1));

    mockMvc
        .perform(get("/api/packets/{packetId}", p.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.packetId").value(p.getId().intValue()))
        .andExpect(jsonPath("$.data.version").value(1))
        .andExpect(jsonPath("$.data.status").value("DRAFT"))
        .andExpect(jsonPath("$.data.snapshot.riskSnapshot.status").value("위험"));
  }

  @Test
  void 존재하지_않으면_404_PACKET_404_1을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/packets/{packetId}", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("PACKET_404_1"));
  }
}
