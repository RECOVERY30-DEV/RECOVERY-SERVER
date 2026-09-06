package recovery30.server.forecast.getdailydetail;

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
import recovery30.server.forecast.domain.ForecastDaily;
import recovery30.server.forecast.domain.ForecastRun;
import recovery30.server.forecast.internal.ForecastDailyItemRepository;
import recovery30.server.forecast.internal.ForecastDailyRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetDailyDetailHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;
  @Autowired private ForecastDailyRepository forecastDailyRepository;
  @Autowired private ForecastDailyItemRepository forecastDailyItemRepository;

  @Test
  void 하루_요약과_근거_라인을_반환한다() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK"));
    ForecastDaily d =
        forecastDailyRepository.save(
            ForecastFixtures.daily(run.getId(), LocalDate.of(2025, 7, 20), 5, 150_000L));
    forecastDailyItemRepository.save(
        ForecastFixtures.dailyItem(d.getId(), "CONFIRMED", "I", 680_000L));
    forecastDailyItemRepository.save(
        ForecastFixtures.dailyItem(d.getId(), "ADJUSTMENT", "I", 50_000L));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/daily/{date}", run.getId(), "2025-07-20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.targetDate").value("2025-07-20"))
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].itemKind").value("CONFIRMED"))
        .andExpect(jsonPath("$.data.items[1].itemKind").value("ADJUSTMENT"));
  }

  @Test
  void run은_있고_해당_날짜_데이터가_없으면_404_FORECAST_404_2() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK"));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/daily/{date}", run.getId(), "2025-08-01"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_2"));
  }

  @Test
  void 날짜_형식이_틀리면_400() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK"));
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/daily/{date}", run.getId(), "20250720"))
        .andExpect(status().isBadRequest());
  }
}
