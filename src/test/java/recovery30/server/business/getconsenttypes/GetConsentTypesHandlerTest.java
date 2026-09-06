package recovery30.server.business.getconsenttypes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.business.ConsentFixtures;
import recovery30.server.business.internal.ConsentTypeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetConsentTypesHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ConsentTypeRepository consentTypeRepository;

  @Test
  void 필수_항목이_먼저_오도록_전체를_반환한다() throws Exception {
    consentTypeRepository.save(ConsentFixtures.type("PACKET_TRANSFER", "상담원 전송 동의", false));
    consentTypeRepository.save(ConsentFixtures.type("ANALYSIS", "서비스 분석 동의", true));
    consentTypeRepository.save(ConsentFixtures.type("FOLLOWUP_TRACKING", "사후 점검 동의", false));

    mockMvc
        .perform(get("/api/consent-types"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].code").value("ANALYSIS"))
        .andExpect(jsonPath("$.data[0].required").value(true))
        .andExpect(jsonPath("$.data[0].purpose").exists())
        .andExpect(jsonPath("$.data[1].required").value(false));
  }
}
