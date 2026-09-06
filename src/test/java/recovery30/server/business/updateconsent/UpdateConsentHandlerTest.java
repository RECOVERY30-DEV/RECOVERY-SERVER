package recovery30.server.business.updateconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.business.ConsentFixtures;
import recovery30.server.business.internal.BusinessRepository;
import recovery30.server.business.internal.ConsentRepository;
import recovery30.server.business.internal.ConsentTypeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UpdateConsentHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private BusinessRepository businessRepository;
  @Autowired private ConsentTypeRepository consentTypeRepository;
  @Autowired private ConsentRepository consentRepository;
  @Autowired private JdbcTemplate jdbc;

  private long bizId;

  @BeforeEach
  void setUp() {
    bizId = businessRepository.save(ConsentFixtures.business("QA-CONSENT")).getId();
    consentTypeRepository.save(ConsentFixtures.type("PACKET_TRANSFER", "상담원 전송 동의", false));
  }

  private void putConsent(String typeCode, String body, int expectedStatus) throws Exception {
    mockMvc
        .perform(
            put("/api/businesses/{businessId}/consents/{typeCode}", bizId, typeCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().is(expectedStatus));
  }

  @Test
  void 동의_후_철회하면_상태가_바뀌고_이력이_두_건_쌓인다() throws Exception {
    mockMvc
        .perform(
            put("/api/businesses/{businessId}/consents/{typeCode}", bizId, "PACKET_TRANSFER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"granted\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("GRANTED"))
        .andExpect(jsonPath("$.data.consentVersion").value("v1.0"));

    assertThat(
            consentRepository
                .findByBusinessIdAndConsentTypeCode(bizId, "PACKET_TRANSFER")
                .orElseThrow()
                .getStatus())
        .isEqualTo("GRANTED");

    putConsent("PACKET_TRANSFER", "{\"granted\":false}", 200);
    assertThat(
            consentRepository
                .findByBusinessIdAndConsentTypeCode(bizId, "PACKET_TRANSFER")
                .orElseThrow()
                .getStatus())
        .isEqualTo("WITHDRAWN");

    Integer logs =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM audit_consent_logs WHERE business_id = ? AND consent_type_code = ?",
            Integer.class,
            bizId,
            "PACKET_TRANSFER");
    assertThat(logs).isEqualTo(2);
    String lastTo =
        jdbc.queryForObject(
            "SELECT to_status FROM audit_consent_logs WHERE business_id = ? ORDER BY id DESC LIMIT 1",
            String.class,
            bizId);
    assertThat(lastTo).isEqualTo("WITHDRAWN");
  }

  @Test
  void granted가_없으면_400을_반환한다() throws Exception {
    putConsent("PACKET_TRANSFER", "{}", 400);
  }

  @Test
  void 존재하지_않는_사업자면_404_BUSINESS_404_1() throws Exception {
    mockMvc
        .perform(
            put("/api/businesses/{businessId}/consents/{typeCode}", 999_999, "PACKET_TRANSFER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"granted\":true}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("BUSINESS_404_1"));
  }

  @Test
  void 존재하지_않는_동의항목이면_404_CONSENT_404_1() throws Exception {
    putConsent("NO_SUCH_TYPE", "{\"granted\":true}", 404);
  }
}
