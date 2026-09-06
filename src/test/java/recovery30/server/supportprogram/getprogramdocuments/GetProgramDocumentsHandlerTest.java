package recovery30.server.supportprogram.getprogramdocuments;

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
import recovery30.server.supportprogram.domain.SupportProgram;
import recovery30.server.supportprogram.internal.ProgramDocumentRepository;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetProgramDocumentsHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private SupportProgramRepository supportProgramRepository;
  @Autowired private ProgramDocumentRepository programDocumentRepository;

  @Test
  void 필요서류_목록을_반환한다() throws Exception {
    SupportProgram p =
        supportProgramRepository.save(
            SupportProgramFixtures.program(
                "SBIZ_STABLE_FUND", "경영안정자금", "공단", LocalDate.of(2025, 7, 31), "ACTIVE"));
    programDocumentRepository.save(SupportProgramFixtures.document(p.getId(), "사업자등록증", true));
    programDocumentRepository.save(SupportProgramFixtures.document(p.getId(), "임대차계약서", false));

    mockMvc
        .perform(get("/api/support-programs/{programCode}/documents", "SBIZ_STABLE_FUND"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].name").value("사업자등록증"))
        .andExpect(jsonPath("$.data[0].required").value(true))
        .andExpect(jsonPath("$.data[1].required").value(false));
  }

  @Test
  void 존재하지_않는_코드면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/support-programs/{programCode}/documents", "NOPE"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("SUPPORT_404_1"));
  }
}
