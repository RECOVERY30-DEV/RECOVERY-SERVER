package recovery30.server.recoveryoption.putoptionselections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import recovery30.server.recoveryoption.domain.UserOptionSelection;
import recovery30.server.recoveryoption.internal.RecoveryOptionRepository;
import recovery30.server.recoveryoption.internal.UserOptionSelectionRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PutOptionSelectionsHandlerTest {

  private static final long RUN = 4821L;

  @Autowired private MockMvc mockMvc;
  @Autowired private RecoveryOptionRepository recoveryOptionRepository;
  @Autowired private UserOptionSelectionRepository userOptionSelectionRepository;
  @MockitoBean private ForecastApi forecastApi;

  private long o1;
  private long o2;
  private long o3;

  @BeforeEach
  void setUp() {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(true);
    o1 =
        recoveryOptionRepository
            .save(RecoveryOptionFixtures.option("A", "FINANCIAL_CONSULT", "MID"))
            .getId();
    o2 =
        recoveryOptionRepository
            .save(RecoveryOptionFixtures.option("B", "SELF_ACTION", "LOW"))
            .getId();
    o3 =
        recoveryOptionRepository
            .save(RecoveryOptionFixtures.option("C", "SUPPORT_PROGRAM", "HIGH"))
            .getId();
  }

  private void putSelections(String body) throws Exception {
    mockMvc
        .perform(
            put("/api/forecasts/{forecastRunId}/option-selections", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());
  }

  @Test
  void 선택_2개를_저장한다() throws Exception {
    mockMvc
        .perform(
            put("/api/forecasts/{forecastRunId}/option-selections", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIds\":[" + o1 + "," + o2 + "]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.selectedOptionIds.length()").value(2));

    assertThat(userOptionSelectionRepository.findByForecastRunIdOrderByIdAsc(RUN)).hasSize(2);
  }

  @Test
  void 기존_선택을_통째로_교체한다() throws Exception {
    UserOptionSelection pre = new UserOptionSelection();
    pre.setForecastRunId(RUN);
    pre.setRecoveryOptionId(o1);
    pre.setSelectedAt(java.time.Instant.parse("2025-07-14T00:00:00Z"));
    userOptionSelectionRepository.save(pre);

    putSelections("{\"optionIds\":[" + o2 + "]}");

    var remaining = userOptionSelectionRepository.findByForecastRunIdOrderByIdAsc(RUN);
    assertThat(remaining).hasSize(1);
    assertThat(remaining.get(0).getRecoveryOptionId()).isEqualTo(o2);
  }

  @Test
  void 빈_배열이면_전체_해제한다() throws Exception {
    UserOptionSelection pre = new UserOptionSelection();
    pre.setForecastRunId(RUN);
    pre.setRecoveryOptionId(o1);
    pre.setSelectedAt(java.time.Instant.parse("2025-07-14T00:00:00Z"));
    userOptionSelectionRepository.save(pre);

    mockMvc
        .perform(
            put("/api/forecasts/{forecastRunId}/option-selections", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIds\":[]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.selectedOptionIds.length()").value(0));

    assertThat(userOptionSelectionRepository.findByForecastRunIdOrderByIdAsc(RUN)).isEmpty();
  }

  @Test
  void 세개_이상이면_400_RECOVERY_400_1을_반환한다() throws Exception {
    mockMvc
        .perform(
            put("/api/forecasts/{forecastRunId}/option-selections", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIds\":[" + o1 + "," + o2 + "," + o3 + "]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("RECOVERY_400_1"));
  }

  @Test
  void 존재하지_않는_옵션이면_404_RECOVERY_404_1을_반환한다() throws Exception {
    mockMvc
        .perform(
            put("/api/forecasts/{forecastRunId}/option-selections", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIds\":[999999]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("RECOVERY_404_1"));
  }

  @Test
  void 존재하지_않는_run이면_404_FORECAST_404_1을_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(false);

    mockMvc
        .perform(
            put("/api/forecasts/{forecastRunId}/option-selections", 999_999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIds\":[" + o1 + "]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
