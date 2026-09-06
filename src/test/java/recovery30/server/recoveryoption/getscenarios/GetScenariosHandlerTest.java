package recovery30.server.recoveryoption.getscenarios;

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
import recovery30.server.recoveryoption.domain.Scenario;
import recovery30.server.recoveryoption.internal.RecoveryOptionRepository;
import recovery30.server.recoveryoption.internal.ScenarioOptionRepository;
import recovery30.server.recoveryoption.internal.ScenarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetScenariosHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ScenarioRepository scenarioRepository;
  @Autowired private ScenarioOptionRepository scenarioOptionRepository;
  @Autowired private RecoveryOptionRepository recoveryOptionRepository;
  @MockitoBean private ForecastApi forecastApi;

  @Test
  void baseline과_simulated를_적용옵션과_함께_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(true);
    scenarioRepository.save(RecoveryOptionFixtures.scenario(4821L, "BASELINE"));
    Scenario simulated =
        scenarioRepository.save(RecoveryOptionFixtures.scenario(4821L, "SIMULATED"));
    RecoveryOption opt =
        recoveryOptionRepository.save(
            RecoveryOptionFixtures.option("REPAYMENT_ADJUST", "FINANCIAL_CONSULT", "MID"));
    scenarioOptionRepository.save(
        RecoveryOptionFixtures.scenarioOption(simulated.getId(), opt.getId()));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/scenarios", 4821))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].scenarioType").value("BASELINE"))
        .andExpect(jsonPath("$.data[0].appliedOptionIds.length()").value(0))
        .andExpect(jsonPath("$.data[1].scenarioType").value("SIMULATED"))
        .andExpect(jsonPath("$.data[1].appliedOptionIds[0]").value(opt.getId().intValue()));
  }

  @Test
  void 시나리오가_없으면_빈_배열을_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(true);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/scenarios", 4821))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(false);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/scenarios", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
