package recovery30.server.consultation.bookconsultation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.business.api.BusinessApi;
import recovery30.server.consultation.ConsultationFixtures;
import recovery30.server.consultation.domain.CounselorSlot;
import recovery30.server.consultation.internal.ConsultationOptionRepository;
import recovery30.server.consultation.internal.CounselorRepository;
import recovery30.server.consultation.internal.CounselorSlotRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookConsultationHandlerTest {

  private static final long BIZ = 1L;

  @Autowired private MockMvc mockMvc;
  @Autowired private CounselorRepository counselorRepository;
  @Autowired private CounselorSlotRepository counselorSlotRepository;
  @Autowired private ConsultationOptionRepository consultationOptionRepository;
  @MockitoBean private BusinessApi businessApi;

  private long counselorId;

  @BeforeEach
  void setUp() {
    when(businessApi.businessExists(anyLong())).thenReturn(true);
    counselorId = counselorRepository.save(ConsultationFixtures.counselor("김상담")).getId();
  }

  private CounselorSlot openSlot(int capacity, int booked) {
    return counselorSlotRepository.save(
        ConsultationFixtures.slot(
            counselorId, Instant.parse("2025-07-14T01:00:00Z"), capacity, booked));
  }

  @Test
  void 슬롯_예약하면_잔여석이_줄고_회복안이_연결된다() throws Exception {
    CounselorSlot slot = openSlot(3, 1);

    String body =
        "{\"counselorId\":"
            + counselorId
            + ",\"slotId\":"
            + slot.getId()
            + ",\"channel\":\"PHONE\",\"transferConsentGranted\":true,"
            + "\"recoveryOptionIds\":[1,3]}";

    String res =
        mockMvc
            .perform(
                post("/api/businesses/{businessId}/consultations", BIZ)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("REQUESTED"))
            .andExpect(jsonPath("$.data.channel").value("PHONE"))
            .andExpect(jsonPath("$.data.scheduledAt").value("2025-07-14T01:00:00Z"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(counselorSlotRepository.findById(slot.getId()).orElseThrow().getBookedCount())
        .isEqualTo(2);
    long consultationId = Long.parseLong(res.replaceAll(".*\"consultationId\":(\\d+).*", "$1"));
    assertThat(consultationOptionRepository.findByConsultationIdOrderByIdAsc(consultationId))
        .hasSize(2);
  }

  @Test
  void 정원_찬_슬롯이면_400_CONSULTATION_400_2를_반환한다() throws Exception {
    CounselorSlot full = openSlot(3, 3);
    mockMvc
        .perform(
            post("/api/businesses/{businessId}/consultations", BIZ)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"slotId\":" + full.getId() + ",\"channel\":\"PHONE\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("CONSULTATION_400_2"));
  }

  @Test
  void 잘못된_채널이면_400_CONSULTATION_400_1을_반환한다() throws Exception {
    mockMvc
        .perform(
            post("/api/businesses/{businessId}/consultations", BIZ)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channel\":\"EMAIL\",\"scheduledAt\":\"2025-07-14T01:00:00Z\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("CONSULTATION_400_1"));
  }

  @Test
  void 슬롯도_일시도_없으면_400을_반환한다() throws Exception {
    mockMvc
        .perform(
            post("/api/businesses/{businessId}/consultations", BIZ)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channel\":\"PHONE\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("COMMON_400"));
  }
}
