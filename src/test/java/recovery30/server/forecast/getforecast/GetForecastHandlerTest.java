package recovery30.server.forecast.getforecast;

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
class GetForecastHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;

  @Test
  void RISK_run이면_부족시점과_최저잔액_밴드를_반환한다() throws Exception {
    ForecastRun run = ForecastFixtures.run(7L, LocalDate.of(2025, 6, 14), "RISK");
    run.setFirstShortfallDate(LocalDate.of(2025, 6, 28));
    run.setDaysToShortfall(14);
    run.setMinBalanceConservative(-2_300_000L);
    run.setMinBalanceExpected(-1_400_000L);
    run.setMinBalanceOptimistic(-800_000L);
    run.setShortfallAmountMin(800_000L);
    run.setShortfallAmountMax(2_300_000L);
    run.setBufferMet(false);
    run = forecastRunRepository.save(run);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.forecastRunId").value(run.getId().intValue()))
        .andExpect(jsonPath("$.data.businessId").value(7))
        .andExpect(jsonPath("$.data.status").value("RISK"))
        .andExpect(jsonPath("$.data.hasShortfall").value(true))
        .andExpect(jsonPath("$.data.daysToShortfall").value(14))
        .andExpect(jsonPath("$.data.firstShortfallDate").value("2025-06-28"))
        .andExpect(jsonPath("$.data.minBalanceAvailable").value(true))
        .andExpect(jsonPath("$.data.minBalanceConservative").value(-2_300_000))
        .andExpect(jsonPath("$.data.minBalanceOptimistic").value(-800_000))
        .andExpect(jsonPath("$.data.shortfallAmountMax").value(2_300_000))
        .andExpect(jsonPath("$.data.bufferMet").value(false));
  }

  @Test
  void HOLD_run이면_밴드가_null이고_available은_false다() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(7L, LocalDate.of(2025, 6, 14), "HOLD"));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("HOLD"))
        .andExpect(jsonPath("$.data.hasShortfall").value(false))
        .andExpect(jsonPath("$.data.minBalanceAvailable").value(false))
        .andExpect(jsonPath("$.data.minBalanceExpected").doesNotExist());
  }

  @Test
  void 존재하지_않는_run이면_404와_FORECAST_404_1을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
