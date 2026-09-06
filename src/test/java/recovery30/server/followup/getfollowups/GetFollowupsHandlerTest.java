package recovery30.server.followup.getfollowups;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.business.api.BusinessApi;
import recovery30.server.followup.FollowupFixtures;
import recovery30.server.followup.domain.FollowupSchedule;
import recovery30.server.followup.internal.FollowupResultRepository;
import recovery30.server.followup.internal.FollowupScheduleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetFollowupsHandlerTest {

  private static final long BIZ = 1L;

  @Autowired private MockMvc mockMvc;
  @Autowired private FollowupScheduleRepository scheduleRepository;
  @Autowired private FollowupResultRepository resultRepository;
  @MockitoBean private BusinessApi businessApi;

  @BeforeEach
  void setUp() {
    when(businessApi.businessExists(anyLong())).thenReturn(true);
  }

  @Test
  void 점검_일정을_예정일_순으로_반환하고_결과_유무를_표시한다() throws Exception {
    FollowupSchedule d30 =
        scheduleRepository.save(
            FollowupFixtures.schedule(BIZ, "D30", LocalDate.of(2025, 8, 14), "DONE"));
    scheduleRepository.save(
        FollowupFixtures.schedule(BIZ, "D60", LocalDate.of(2025, 9, 13), "SCHEDULED"));
    resultRepository.save(FollowupFixtures.result(d30.getId(), "PARTIAL", "STABLE"));

    mockMvc
        .perform(get("/api/businesses/{businessId}/followups", BIZ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].checkpoint").value("D30"))
        .andExpect(jsonPath("$.data[0].hasResult").value(true))
        .andExpect(jsonPath("$.data[1].checkpoint").value("D60"))
        .andExpect(jsonPath("$.data[1].hasResult").value(false));
  }

  @Test
  void 일정이_없으면_빈_배열을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/businesses/{businessId}/followups", BIZ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void 존재하지_않는_사업자면_404_BUSINESS_404_1을_반환한다() throws Exception {
    when(businessApi.businessExists(anyLong())).thenReturn(false);

    mockMvc
        .perform(get("/api/businesses/{businessId}/followups", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("BUSINESS_404_1"));
  }
}
