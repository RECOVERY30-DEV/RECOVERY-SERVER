package recovery30.server.recoveryoption.getrecoveryoptions;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.recoveryoption.RecoveryOptionFixtures;
import recovery30.server.recoveryoption.domain.RecoveryOption;
import recovery30.server.recoveryoption.domain.UserOptionSelection;
import recovery30.server.recoveryoption.internal.RecoveryOptionRepository;
import recovery30.server.recoveryoption.internal.UserOptionSelectionRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetRecoveryOptionsHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RecoveryOptionRepository recoveryOptionRepository;
  @Autowired private UserOptionSelectionRepository userOptionSelectionRepository;
  @MockitoBean private ForecastApi forecastApi;

  @Test
  void 카탈로그를_반환하고_선택한_것은_selected_true다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(true);
    RecoveryOption picked =
        recoveryOptionRepository.save(
            RecoveryOptionFixtures.option("REPAYMENT_ADJUST", "FINANCIAL_CONSULT", "MID"));
    recoveryOptionRepository.save(
        RecoveryOptionFixtures.option("DUEDATE_SHIFT", "SELF_ACTION", "LOW"));
    UserOptionSelection sel = new UserOptionSelection();
    sel.setForecastRunId(4821L);
    sel.setRecoveryOptionId(picked.getId());
    sel.setSelectedAt(java.time.Instant.parse("2025-07-14T00:00:00Z"));
    userOptionSelectionRepository.save(sel);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/recovery-options", 4821))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].optionCode").value("REPAYMENT_ADJUST"))
        .andExpect(jsonPath("$.data[0].category").value("FINANCIAL_CONSULT"))
        .andExpect(jsonPath("$.data[0].difficulty").value("MID"))
        .andExpect(jsonPath("$.data[0].selected").value(true))
        .andExpect(jsonPath("$.data[1].selected").value(false));
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(false);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/recovery-options", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
