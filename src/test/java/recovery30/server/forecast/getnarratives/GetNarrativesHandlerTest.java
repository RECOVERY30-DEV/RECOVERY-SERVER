package recovery30.server.forecast.getnarratives;

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
import recovery30.server.forecast.internal.ForecastRunNarrativeRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetNarrativesHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;
  @Autowired private ForecastRunNarrativeRepository narrativeRepository;

  private ForecastRun seedWithNarratives() {
    ForecastRun run =
        forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 14), "STABLE"));
    narrativeRepository.save(ForecastFixtures.narrative(run.getId(), "STATUS_LABEL", 0, "현금흐름 안정"));
    narrativeRepository.save(ForecastFixtures.narrative(run.getId(), "STABLE_REASON", 0, "근거 1"));
    narrativeRepository.save(ForecastFixtures.narrative(run.getId(), "STABLE_REASON", 1, "근거 2"));
    narrativeRepository.save(ForecastFixtures.narrative(run.getId(), "DISCLAIMER", 0, "고지"));
    return run;
  }

  @Test
  void kind_seq_순으로_전체를_반환한다() throws Exception {
    ForecastRun run = seedWithNarratives();

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/narratives", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(4))
        .andExpect(jsonPath("$.data[0].kind").value("DISCLAIMER"))
        .andExpect(jsonPath("$.data[1].kind").value("STABLE_REASON"))
        .andExpect(jsonPath("$.data[1].seq").value(0))
        .andExpect(jsonPath("$.data[2].seq").value(1));
  }

  @Test
  void kind_필터로_해당_종류만_반환한다() throws Exception {
    ForecastRun run = seedWithNarratives();

    mockMvc
        .perform(
            get("/api/forecasts/{forecastRunId}/narratives", run.getId())
                .param("kind", "STABLE_REASON"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].kind").value("STABLE_REASON"));
  }

  @Test
  void 존재하지_않는_run이면_404() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/narratives", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
