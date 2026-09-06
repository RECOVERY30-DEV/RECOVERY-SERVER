package recovery30.server.supportprogram.getprogramrecommendations;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.supportprogram.SupportProgramFixtures;
import recovery30.server.supportprogram.domain.SupportProgram;
import recovery30.server.supportprogram.internal.ProgramRecommendationRepository;
import recovery30.server.supportprogram.internal.SupportProgramRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetProgramRecommendationsHandlerTest {

  private static final long RUN = 4821L;

  @Autowired private MockMvc mockMvc;
  @Autowired private SupportProgramRepository supportProgramRepository;
  @Autowired private ProgramRecommendationRepository recommendationRepository;
  @MockitoBean private ForecastApi forecastApi;

  @Test
  void 추천을_rank순으로_제도명과_함께_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(true);
    SupportProgram p1 =
        supportProgramRepository.save(
            SupportProgramFixtures.program(
                "SBIZ_STABLE_FUND", "경영안정자금", "공단", LocalDate.of(2025, 7, 31), "ACTIVE"));
    SupportProgram p2 =
        supportProgramRepository.save(
            SupportProgramFixtures.program(
                "SUNSHINE_119", "햇살론119", "서금원", LocalDate.of(2025, 12, 31), "ACTIVE"));
    recommendationRepository.save(
        SupportProgramFixtures.recommendation(RUN, p2.getId(), 2, "연체 우려 차주 대상"));
    recommendationRepository.save(
        SupportProgramFixtures.recommendation(RUN, p1.getId(), 1, "매출 감소·사업자 2년 이상"));

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/program-recommendations", RUN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].rankNo").value(1))
        .andExpect(jsonPath("$.data[0].programCode").value("SBIZ_STABLE_FUND"))
        .andExpect(jsonPath("$.data[0].name").value("경영안정자금"))
        .andExpect(jsonPath("$.data[1].rankNo").value(2));
  }

  @Test
  void 추천이_없으면_빈_배열을_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(true);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/program-recommendations", RUN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    when(forecastApi.forecastRunExists(anyLong())).thenReturn(false);

    mockMvc
        .perform(get("/api/forecasts/{forecastRunId}/program-recommendations", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
