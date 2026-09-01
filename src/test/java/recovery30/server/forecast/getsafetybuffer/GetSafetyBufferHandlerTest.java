package recovery30.server.forecast.getsafetybuffer;

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
class GetSafetyBufferHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;

  @Test
  void 안전잔액과_buffer_충족여부를_반환한다() throws Exception {
    ForecastRun run = ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "STABLE");
    run.setMinBalanceExpected(830_000L);
    run.setBufferMet(true);
    run = forecastRunRepository.save(run);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/safety-buffer", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.amount").value(830_000))
        .andExpect(jsonPath("$.data.bufferMet").value(true));
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/safety-buffer", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
