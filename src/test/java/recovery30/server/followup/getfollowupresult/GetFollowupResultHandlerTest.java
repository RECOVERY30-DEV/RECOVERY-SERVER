package recovery30.server.followup.getfollowupresult;

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
import recovery30.server.followup.FollowupFixtures;
import recovery30.server.followup.domain.FollowupSchedule;
import recovery30.server.followup.internal.FollowupResultRepository;
import recovery30.server.followup.internal.FollowupScheduleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetFollowupResultHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private FollowupScheduleRepository scheduleRepository;
  @Autowired private FollowupResultRepository resultRepository;

  @Test
  void 점검_결과를_회복_금액과_위험_상태와_함께_반환한다() throws Exception {
    FollowupSchedule d30 =
        scheduleRepository.save(
            FollowupFixtures.schedule(1L, "D30", LocalDate.of(2025, 8, 14), "DONE"));
    resultRepository.save(FollowupFixtures.result(d30.getId(), "PARTIAL", "STABLE"));

    mockMvc
        .perform(get("/api/followups/{scheduleId}/result", d30.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.scheduleId").value(d30.getId().intValue()))
        .andExpect(jsonPath("$.data.balanceRecovered").value("PARTIAL"))
        .andExpect(jsonPath("$.data.recoveryAmount").value(1_640_000))
        .andExpect(jsonPath("$.data.riskStatus").value("STABLE"));
  }

  @Test
  void 없는_점검_일정이면_404_FOLLOWUP_404_1을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/followups/{scheduleId}/result", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FOLLOWUP_404_1"));
  }

  @Test
  void 결과가_아직_없으면_404_FOLLOWUP_404_2를_반환한다() throws Exception {
    FollowupSchedule d60 =
        scheduleRepository.save(
            FollowupFixtures.schedule(1L, "D60", LocalDate.of(2025, 9, 13), "SCHEDULED"));

    mockMvc
        .perform(get("/api/followups/{scheduleId}/result", d60.getId()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FOLLOWUP_404_2"));
  }
}
