package recovery30.server.supportprogram.getsupportprograms;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.supportprogram.SupportProgramFixtures;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetSupportProgramsHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private SupportProgramRepository supportProgramRepository;

  @BeforeEach
  void setUp() {
    supportProgramRepository.save(
        SupportProgramFixtures.program(
            "A_SOON", "임박 제도", "기관A", LocalDate.now().plusDays(3), "ACTIVE"));
    supportProgramRepository.save(
        SupportProgramFixtures.program(
            "B_LATER", "여유 제도", "기관B", LocalDate.now().plusDays(60), "ACTIVE"));
    supportProgramRepository.save(
        SupportProgramFixtures.program(
            "C_PAST", "마감 지난 제도", "기관C", LocalDate.now().minusDays(1), "ACTIVE"));
    supportProgramRepository.save(
        SupportProgramFixtures.program(
            "D_CLOSED", "종료 제도", "기관D", LocalDate.now().plusDays(10), "CLOSED"));
  }

  @Test
  void 전체를_신청기한_임박순으로_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/support-programs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(4))
        .andExpect(jsonPath("$.data[0].programCode").value("C_PAST"))
        .andExpect(jsonPath("$.data[1].programCode").value("A_SOON"));
  }

  @Test
  void applicableOnly면_마감지남과_CLOSED를_제외한다() throws Exception {
    mockMvc
        .perform(get("/api/support-programs").param("applicableOnly", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].programCode").value("A_SOON"))
        .andExpect(jsonPath("$.data[1].programCode").value("B_LATER"));
  }
}
