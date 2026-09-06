package recovery30.server.consultation.getcounselorslots;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.consultation.ConsultationFixtures;
import recovery30.server.consultation.domain.Counselor;
import recovery30.server.consultation.domain.CounselorSlot;
import recovery30.server.consultation.internal.CounselorRepository;
import recovery30.server.consultation.internal.CounselorSlotRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetCounselorSlotsHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CounselorRepository counselorRepository;
  @Autowired private CounselorSlotRepository counselorSlotRepository;

  @Test
  void 슬롯을_시작시각순으로_잔여석과_예약가능여부와_함께_반환한다() throws Exception {
    Counselor c = counselorRepository.save(ConsultationFixtures.counselor("김상담"));
    counselorSlotRepository.save(
        ConsultationFixtures.slot(c.getId(), Instant.parse("2025-07-14T01:00:00Z"), 3, 1));
    CounselorSlot full =
        ConsultationFixtures.slot(c.getId(), Instant.parse("2025-07-14T05:00:00Z"), 3, 3);
    counselorSlotRepository.save(full);
    CounselorSlot blocked =
        ConsultationFixtures.slot(c.getId(), Instant.parse("2025-07-15T02:00:00Z"), 3, 0);
    blocked.setStatus("BLOCKED");
    counselorSlotRepository.save(blocked);

    mockMvc
        .perform(get("/api/counselors/{counselorId}/slots", c.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].remainingSeats").value(2))
        .andExpect(jsonPath("$.data[0].bookable").value(true))
        .andExpect(jsonPath("$.data[1].bookable").value(false))
        .andExpect(jsonPath("$.data[2].status").value("BLOCKED"))
        .andExpect(jsonPath("$.data[2].bookable").value(false));
  }

  @Test
  void 존재하지_않는_상담자면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/counselors/{counselorId}/slots", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("CONSULTATION_404_2"));
  }
}
