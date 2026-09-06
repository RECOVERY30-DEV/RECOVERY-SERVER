package recovery30.server.forecast.getdaily;

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
import recovery30.server.forecast.internal.ForecastDailyRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetDailyHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;
  @Autowired private ForecastDailyRepository forecastDailyRepository;

  @Test
  void 일자별_행을_날짜순으로_반환한다() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK"));
    forecastDailyRepository.save(
        ForecastFixtures.daily(run.getId(), LocalDate.of(2025, 7, 17), 2, 400_000L));
    forecastDailyRepository.save(
        ForecastFixtures.daily(run.getId(), LocalDate.of(2025, 7, 15), 0, 800_000L));
    forecastDailyRepository.save(
        ForecastFixtures.daily(run.getId(), LocalDate.of(2025, 7, 26), 11, -200_000L));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/daily", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].targetDate").value("2025-07-15"))
        .andExpect(jsonPath("$.data[2].targetDate").value("2025-07-26"))
        .andExpect(jsonPath("$.data[2].shortfall").value(true));
  }

  @Test
  void 존재하지_않는_run이면_404() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/daily", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
