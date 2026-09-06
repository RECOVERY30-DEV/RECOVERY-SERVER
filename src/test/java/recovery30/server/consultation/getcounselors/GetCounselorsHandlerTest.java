package recovery30.server.consultation.getcounselors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.consultation.ConsultationFixtures;
import recovery30.server.consultation.internal.CounselorRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetCounselorsHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CounselorRepository counselorRepository;

  @Test
  void 상담자_목록을_반환한다() throws Exception {
    counselorRepository.save(ConsultationFixtures.counselor("김상담"));
    counselorRepository.save(ConsultationFixtures.counselor("이회복"));

    mockMvc
        .perform(get("/api/counselors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].name").value("김상담"))
        .andExpect(jsonPath("$.data[0].institution").value("소상공인시장진흥공단"));
  }
}
