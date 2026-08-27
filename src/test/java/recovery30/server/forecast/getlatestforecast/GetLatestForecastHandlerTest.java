package recovery30.server.forecast.getlatestforecast;

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
class GetLatestForecastHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;

  @Test
  void 예측이_여러건이면_기준일이_가장_최신인_run을_반환한다() throws Exception {
    forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 10), "STABLE"));
    ForecastRun latest =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK"));

    mockMvc
        .perform(get("/api/businesses/{businessId}/forecasts/latest", 1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.forecastRunId").value(latest.getId().intValue()))
        .andExpect(jsonPath("$.data.baseDate").value("2025-07-15"))
        .andExpect(jsonPath("$.data.status").value("RISK"));
  }

  @Test
  void 예측_이력이_없으면_404와_FORECAST_404_1을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/businesses/{businessId}/forecasts/latest", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
