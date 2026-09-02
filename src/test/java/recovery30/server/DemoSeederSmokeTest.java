package recovery30.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import recovery30.server.business.api.BusinessApi;
import tools.jackson.databind.ObjectMapper;

/** demo 프로파일 시더(DemoBusinessSeeder + DemoForecastSeeder)가 홈 화면 API로 조회되는 데이터를 만드는지 확인. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class DemoSeederSmokeTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private BusinessApi businessApi;

  @Test
  void QA_RISK_페르소나가_홈_API_6종으로_조회된다() throws Exception {
    long businessId = businessApi.findBusinessIdByRegNo("QA-RISK").orElseThrow();

    String body =
        mockMvc
            .perform(get("/api/businesses/{businessId}/forecasts/latest", businessId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("RISK"))
            .andExpect(jsonPath("$.data.baseDate").value("2025-07-15"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long runId = objectMapper.readTree(body).path("data").path("forecastRunId").asLong();

    mockMvc
        .perform(get("/api/forecasts/{runId}/min-balance", runId))
        .andExpect(jsonPath("$.data.available").value(true))
        .andExpect(jsonPath("$.data.conservative").value(-1_280_000));
    mockMvc
        .perform(get("/api/forecasts/{runId}/shortfall", runId))
        .andExpect(jsonPath("$.data.hasShortfall").value(true))
        .andExpect(jsonPath("$.data.dDay").value(11));
    mockMvc
        .perform(get("/api/forecasts/{runId}/safety-buffer", runId))
        .andExpect(jsonPath("$.data.bufferMet").value(false));
    mockMvc
        .perform(get("/api/forecasts/{runId}/risk-drivers", runId).param("limit", "3"))
        .andExpect(jsonPath("$.data.length()").value(3));
    mockMvc
        .perform(get("/api/forecasts/{runId}/coverage", runId))
        .andExpect(jsonPath("$.data.length()").value(4));
  }

  @Test
  void QA_NEW_페르소나는_예측이_없어_404를_반환한다() throws Exception {
    long businessId = businessApi.findBusinessIdByRegNo("QA-NEW").orElseThrow();

    mockMvc
        .perform(get("/api/businesses/{businessId}/forecasts/latest", businessId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
