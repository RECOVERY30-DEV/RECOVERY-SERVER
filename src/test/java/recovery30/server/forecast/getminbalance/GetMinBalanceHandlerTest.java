package recovery30.server.forecast.getminbalance;

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
import recovery30.server.forecast.ForecastFixtures;
import recovery30.server.forecast.domain.ForecastRun;
import recovery30.server.forecast.internal.ForecastRunRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetMinBalanceHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;

  @Test
  void RISK_run이면_보수_예상_낙관_밴드를_반환한다() throws Exception {
    ForecastRun run = ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK");
    run.setMinBalanceConservative(-1_280_000L);
    run.setMinBalanceExpected(540_000L);
    run.setMinBalanceOptimistic(830_000L);
    run = forecastRunRepository.save(run);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/min-balance", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.available").value(true))
        .andExpect(jsonPath("$.data.conservative").value(-1_280_000))
        .andExpect(jsonPath("$.data.expected").value(540_000))
        .andExpect(jsonPath("$.data.optimistic").value(830_000));
  }

  @Test
  void HOLD_run이면_available가_false이고_값이_null이다() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "HOLD"));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/min-balance", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.available").value(false))
        .andExpect(jsonPath("$.data.conservative").isEmpty())
        .andExpect(jsonPath("$.data.expected").isEmpty())
        .andExpect(jsonPath("$.data.optimistic").isEmpty());
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/min-balance", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
