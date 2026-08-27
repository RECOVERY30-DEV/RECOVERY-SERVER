package recovery30.server.forecast.getforecastcoverage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.forecast.ForecastFixtures;
import recovery30.server.forecast.domain.ForecastCoverage;
import recovery30.server.forecast.domain.ForecastRun;
import recovery30.server.forecast.internal.ForecastCoverageRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetForecastCoverageHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;
  @Autowired private ForecastCoverageRepository coverageRepository;

  @Test
  void 소스별_커버리지를_반환하고_임계미만이면_PARTIAL이다() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK"));
    ForecastCoverage bank = ForecastFixtures.coverage(run.getId(), "BANK_ACCOUNT", false);
    bank.setCoverageRate(new BigDecimal("95.00"));
    ForecastCoverage auto = ForecastFixtures.coverage(run.getId(), "AUTO_TRANSFER", true);
    auto.setCoverageRate(new BigDecimal("61.00"));
    coverageRepository.save(bank);
    coverageRepository.save(auto);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/coverage", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].sourceType").value("BANK_ACCOUNT"))
        .andExpect(jsonPath("$.data[0].status").value("COMPLETE"))
        .andExpect(jsonPath("$.data[1].sourceType").value("AUTO_TRANSFER"))
        .andExpect(jsonPath("$.data[1].status").value("PARTIAL"))
        .andExpect(jsonPath("$.data[1].belowThreshold").value(true));
  }

  @Test
  void coverage_스냅샷이_없으면_빈_배열을_반환한다() throws Exception {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "STABLE"));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/coverage", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/coverage", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
