package recovery30.server.forecast.getriskdrivers;

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
import recovery30.server.forecast.domain.RiskDriver;
import recovery30.server.forecast.internal.ForecastRiskDriverRepository;
import recovery30.server.forecast.internal.ForecastRunRepository;
import recovery30.server.forecast.internal.RiskDriverEvidenceRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetRiskDriversHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ForecastRunRepository forecastRunRepository;
  @Autowired private ForecastRiskDriverRepository riskDriverRepository;
  @Autowired private RiskDriverEvidenceRepository evidenceRepository;

  private ForecastRun newRun() {
    return forecastRunRepository.save(ForecastFixtures.run(1L, LocalDate.of(2025, 7, 15), "RISK"));
  }

  @Test
  void limit_2면_rank_상위_2건만_반환한다() throws Exception {
    ForecastRun run = newRun();
    riskDriverRepository.save(
        ForecastFixtures.driver(run.getId(), 1, "RENT_LOAN", "월말 원리금 임차료 집중"));
    riskDriverRepository.save(
        ForecastFixtures.driver(run.getId(), 2, "SALES_DROP", "최근 4주 매출 감소 추세"));
    riskDriverRepository.save(
        ForecastFixtures.driver(run.getId(), 3, "AUTODEBIT", "자동이체 3건 납부일 겹침"));

    mockMvc
        .perform(
            get("/api/forecasts/{forecastRunId}/risk-drivers", run.getId()).param("limit", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].rank").value(1))
        .andExpect(jsonPath("$.data[1].rank").value(2))
        .andExpect(jsonPath("$.data[0].evidence").isEmpty());
  }

  @Test
  void run은_있고_원인이_없으면_빈_배열을_반환한다() throws Exception {
    ForecastRun run = newRun();

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/risk-drivers", run.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void include_evidence면_근거_거래를_포함한다() throws Exception {
    ForecastRun run = newRun();
    RiskDriver driver =
        riskDriverRepository.save(
            ForecastFixtures.driver(run.getId(), 1, "SALES_DROP", "최근 8주 매출 감소"));
    evidenceRepository.save(ForecastFixtures.evidence(driver.getId(), "신한카드 정산 5건", "6월 2일~11일"));

    mockMvc
        .perform(
            get("/api/forecasts/{forecastRunId}/risk-drivers", run.getId())
                .param("include", "evidence"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].evidence.length()").value(1))
        .andExpect(jsonPath("$.data[0].evidence[0].label").value("신한카드 정산 5건"))
        .andExpect(jsonPath("$.data[0].evidence[0].periodText").value("6월 2일~11일"));
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/risk-drivers", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
