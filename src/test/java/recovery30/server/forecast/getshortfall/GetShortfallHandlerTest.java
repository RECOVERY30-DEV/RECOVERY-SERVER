package recovery30.server.forecast.getshortfall;

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
class GetShortfallHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;

  @Test
  void 부족일이_있으면_dDay와_예상일을_반환한다() throws Exception {
    ForecastRun run = ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK");
    run.setDaysToShortfall(11);
    run.setFirstShortfallDate(LocalDate.of(2025, 7, 26));
    run.setShortfallAmountMin(760_000L);
    run.setShortfallAmountMax(1_240_000L);
    run = forecastRunRepository.save(run);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/shortfall", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.hasShortfall").value(true))
        .andExpect(jsonPath("$.data.dDay").value(11))
        .andExpect(jsonPath("$.data.expectedDate").value("2025-07-26"))
        .andExpect(jsonPath("$.data.horizonDays").value(30));
  }

  @Test
  void STABLE_run이면_hasShortfall이_false이고_dDay가_null이다() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "STABLE"));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/shortfall", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.hasShortfall").value(false))
        .andExpect(jsonPath("$.data.dDay").isEmpty())
        .andExpect(jsonPath("$.data.expectedDate").isEmpty());
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/shortfall", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
