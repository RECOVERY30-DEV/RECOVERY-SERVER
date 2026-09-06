package recovery30.server.business.getconsents;

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
import recovery30.server.business.domain.Business;
import recovery30.server.business.internal.BusinessRepository;
import recovery30.server.business.internal.ConsentRepository;
import recovery30.server.business.internal.ConsentTypeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetConsentsHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private BusinessRepository businessRepository;
  @Autowired private ConsentTypeRepository consentTypeRepository;
  @Autowired private ConsentRepository consentRepository;

  @Test
  void 응답한_항목은_상태를_없으면_NOT_SET을_반환한다() throws Exception {
    Business b = businessRepository.save(ConsentFixtures.business("QA-CONSENT"));
    consentTypeRepository.save(ConsentFixtures.type("ANALYSIS", "서비스 분석 동의", true));
    consentTypeRepository.save(ConsentFixtures.type("PACKET_TRANSFER", "상담원 전송 동의", false));
    consentRepository.save(ConsentFixtures.consent(b.getId(), "ANALYSIS", "GRANTED"));

    mockMvc
        .perform(get("/api/businesses/{businessId}/consents", b.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].typeCode").value("ANALYSIS"))
        .andExpect(jsonPath("$.data[0].status").value("GRANTED"))
        .andExpect(jsonPath("$.data[0].lastChangedAt").value("2025-07-14T00:00:00Z"))
        .andExpect(jsonPath("$.data[0].consentVersion").value("v1.0"))
        .andExpect(jsonPath("$.data[1].status").value("NOT_SET"))
        .andExpect(jsonPath("$.data[1].consentVersion").doesNotExist());
  }

  @Test
  void 존재하지_않는_사업자면_404_BUSINESS_404_1을_반환한다() throws Exception {
    consentTypeRepository.save(ConsentFixtures.type("ANALYSIS", "서비스 분석 동의", true));
    mockMvc
        .perform(get("/api/businesses/{businessId}/consents", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("BUSINESS_404_1"));
  }
}
