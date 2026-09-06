package recovery30.server.supportprogram.getsupportprogram;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
class GetSupportProgramHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private SupportProgramRepository supportProgramRepository;

  @Test
  void 코드로_상세를_반환한다() throws Exception {
    supportProgramRepository.save(
        SupportProgramFixtures.program(
            "SBIZ_STABLE_FUND", "소상공인 경영안정자금", "소상공인시장진흥공단", LocalDate.of(2025, 7, 31), "ACTIVE"));

    mockMvc
        .perform(get("/api/support-programs/{programCode}", "SBIZ_STABLE_FUND"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("소상공인 경영안정자금"))
        .andExpect(jsonPath("$.data.termText").value("3년 거치 5년 분할상환"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void 존재하지_않는_코드면_404_SUPPORT_404_1을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/support-programs/{programCode}", "NOPE"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("SUPPORT_404_1"));
  }
}
