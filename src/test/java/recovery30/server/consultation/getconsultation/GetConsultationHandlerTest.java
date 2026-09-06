package recovery30.server.consultation.getconsultation;

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
import recovery30.server.consultation.domain.Consultation;
import recovery30.server.consultation.domain.ConsultationOption;
import recovery30.server.consultation.domain.Counselor;
import recovery30.server.consultation.internal.ConsultationOptionRepository;
import recovery30.server.consultation.internal.ConsultationRepository;
import recovery30.server.consultation.internal.CounselorRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetConsultationHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CounselorRepository counselorRepository;
  @Autowired private ConsultationRepository consultationRepository;
  @Autowired private ConsultationOptionRepository consultationOptionRepository;

  @Test
  void 예약_정보와_회복안_목록_상담자명을_반환한다() throws Exception {
    Counselor counselor = counselorRepository.save(ConsultationFixtures.counselor("김상담"));
    Consultation c =
        consultationRepository.save(
            ConsultationFixtures.consultation(1L, counselor.getId(), "PHONE"));
    ConsultationOption o1 = new ConsultationOption();
    o1.setConsultationId(c.getId());
    o1.setRecoveryOptionId(3L);
    consultationOptionRepository.save(o1);

    mockMvc
        .perform(get("/api/consultations/{consultationId}", c.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.channel").value("PHONE"))
        .andExpect(jsonPath("$.data.status").value("REQUESTED"))
        .andExpect(jsonPath("$.data.counselorName").value("김상담"))
        .andExpect(jsonPath("$.data.recoveryOptionIds[0]").value(3));
  }

  @Test
  void 존재하지_않으면_404_CONSULTATION_404_1을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/consultations/{consultationId}", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("CONSULTATION_404_1"));
  }
}
