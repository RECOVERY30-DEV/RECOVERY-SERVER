package recovery30.server.followup.getexecutionstatus;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import recovery30.server.followup.internal.RecoveryExecutionStatusRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetExecutionStatusHandlerTest {

  private static final long BIZ = 1L;

  @Autowired private MockMvc mockMvc;
  @Autowired private RecoveryExecutionStatusRepository executionStatusRepository;
  @MockitoBean private BusinessApi businessApi;

  @BeforeEach
  void setUp() {
    when(businessApi.businessExists(anyLong())).thenReturn(true);
  }

  @Test
  void 회복안별_실행_상태를_반환한다() throws Exception {
    executionStatusRepository.save(FollowupFixtures.executionStatus(BIZ, 2L, "IN_PROGRESS", null));
    executionStatusRepository.save(
        FollowupFixtures.executionStatus(BIZ, 3L, "BLOCKED", "담당자 확인 필요"));

    mockMvc
        .perform(get("/api/businesses/{businessId}/recovery-execution-status", BIZ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"))
        .andExpect(jsonPath("$.data[1].status").value("BLOCKED"))
        .andExpect(jsonPath("$.data[1].blockerText").value("담당자 확인 필요"));
  }

  @Test
  void 실행_상태가_없으면_빈_배열을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/businesses/{businessId}/recovery-execution-status", BIZ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void 존재하지_않는_사업자면_404_BUSINESS_404_1을_반환한다() throws Exception {
    when(businessApi.businessExists(anyLong())).thenReturn(false);

    mockMvc
        .perform(get("/api/businesses/{businessId}/recovery-execution-status", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("BUSINESS_404_1"));
  }
}
