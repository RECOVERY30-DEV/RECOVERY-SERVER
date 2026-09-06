package recovery30.server.recoveryoption.selfaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.recoveryoption.RecoveryOptionFixtures;
import recovery30.server.recoveryoption.domain.SelfActionItem;
import recovery30.server.recoveryoption.domain.SelfActionPlan;
import recovery30.server.recoveryoption.internal.RecoveryOptionRepository;
import recovery30.server.recoveryoption.internal.SelfActionItemRepository;
import recovery30.server.recoveryoption.internal.SelfActionPlanRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SelfActionHandlerTest {

  private static final long RUN = 4821L;
  private static final long BIZ = 7L;

  @Autowired private MockMvc mockMvc;
  @Autowired private RecoveryOptionRepository recoveryOptionRepository;
  @Autowired private SelfActionPlanRepository planRepository;
  @Autowired private SelfActionItemRepository itemRepository;
  @MockitoBean private ForecastApi forecastApi;

  private long optionId;

  @BeforeEach
  void setUp() {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(true);
    when(forecastApi.findBusinessId(anyLong())).thenReturn(Optional.of(BIZ));
    optionId =
        recoveryOptionRepository
            .save(RecoveryOptionFixtures.option("REPAYMENT_ADJUST", "FINANCIAL_CONSULT", "MID"))
            .getId();
  }

  private SelfActionPlan savedPlan() {
    SelfActionPlan p = new SelfActionPlan();
    p.setBusinessId(BIZ);
    p.setForecastRunId(RUN);
    p.setRecoveryOptionId(optionId);
    p.setExpectedEffectText("첫 부족일 +12일");
    p.setStatus("ACTIVE");
    p.setSavedAt(java.time.Instant.parse("2025-07-14T00:00:00Z"));
    return planRepository.save(p);
  }

  private SelfActionItem savedItem(long planId, String title, String status) {
    SelfActionItem i = new SelfActionItem();
    i.setSelfActionPlanId(planId);
    i.setTitle(title);
    i.setStatus(status);
    return itemRepository.save(i);
  }

  @Test
  void 계획이_없으면_빈_배열을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/self-action-plans", RUN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void 계획과_준비항목을_함께_반환한다() throws Exception {
    SelfActionPlan plan = savedPlan();
    savedItem(plan.getId(), "임차인에게 납부일 조정 요청", "PENDING");
    savedItem(plan.getId(), "은행에 원리금 납부일 변경 신청", "DONE");

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/self-action-plans", RUN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].recoveryOptionId").value((int) optionId))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$.data[0].items.length()").value(2))
        .andExpect(jsonPath("$.data[0].items[1].status").value("DONE"));
  }

  @Test
  void 존재하지_않는_run이면_404_FORECAST_404_1을_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(false);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/self-action-plans", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }

  @Test
  void 회복안을_실행계획으로_저장한다() throws Exception {
    String body =
        "{\"recoveryOptionId\":"
            + optionId
            + ",\"expectedEffectText\":\"첫 부족일 +12일\",\"items\":["
            + "{\"title\":\"임차인에게 납부일 조정 요청\",\"targetDate\":\"2025-07-18\"},"
            + "{\"title\":\"은행에 원리금 납부일 변경 신청\"}]}";

    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/self-action-plans", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.recoveryOptionId").value((int) optionId))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"))
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].status").value("PENDING"))
        .andExpect(jsonPath("$.data.items[0].targetDate").value("2025-07-18"));

    assertThat(planRepository.findByForecastRunIdOrderByIdAsc(RUN)).hasSize(1);
  }

  @Test
  void 회복안ID가_없으면_400을_반환한다() throws Exception {
    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/self-action-plans", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedEffectText\":\"x\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("COMMON_400"));
  }

  @Test
  void 저장시_존재하지_않는_회복안이면_404_RECOVERY_404_1을_반환한다() throws Exception {
    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/self-action-plans", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recoveryOptionId\":999999}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("RECOVERY_404_1"));
  }

  @Test
  void 저장시_run의_사업자를_찾을_수_없으면_404_FORECAST_404_1을_반환한다() throws Exception {
    when(forecastApi.findBusinessId(anyLong())).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/self-action-plans", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recoveryOptionId\":" + optionId + "}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }

  @Test
  void 준비항목_제목이_비어있으면_400을_반환한다() throws Exception {
    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/self-action-plans", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recoveryOptionId\":" + optionId + ",\"items\":[{\"title\":\"  \"}]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("COMMON_400"));
  }

  @Test
  void 준비항목을_DONE으로_수정한다() throws Exception {
    SelfActionPlan plan = savedPlan();
    SelfActionItem item = savedItem(plan.getId(), "임차인에게 납부일 조정 요청", "PENDING");

    mockMvc
        .perform(
            patch(
                    "/api/forecasts/{forecastRunId}/self-action-plans/items/{itemId}",
                    RUN,
                    item.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\",\"memo\":\"완료\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("DONE"))
        .andExpect(jsonPath("$.data.memo").value("완료"));

    assertThat(itemRepository.findById(item.getId()).orElseThrow().getStatus()).isEqualTo("DONE");
  }

  @Test
  void 다른_run의_준비항목이면_404_RECOVERY_404_2를_반환한다() throws Exception {
    SelfActionPlan plan = savedPlan();
    SelfActionItem item = savedItem(plan.getId(), "임차인에게 납부일 조정 요청", "PENDING");

    mockMvc
        .perform(
            patch(
                    "/api/forecasts/{forecastRunId}/self-action-plans/items/{itemId}",
                    RUN + 1,
                    item.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("RECOVERY_404_2"));
  }

  @Test
  void 없는_준비항목이면_404_RECOVERY_404_2를_반환한다() throws Exception {
    mockMvc
        .perform(
            patch("/api/forecasts/{forecastRunId}/self-action-plans/items/{itemId}", RUN, 999_999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("RECOVERY_404_2"));
  }
}
