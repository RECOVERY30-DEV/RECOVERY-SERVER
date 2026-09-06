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
  void QA_RISK_회복안_비교_API가_시더_데이터로_조회된다() throws Exception {
    long businessId = businessApi.findBusinessIdByRegNo("QA-RISK").orElseThrow();
    String latest =
        mockMvc
            .perform(get("/api/businesses/{businessId}/forecasts/latest", businessId))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long runId = objectMapper.readTree(latest).path("data").path("forecastRunId").asLong();

    mockMvc
        .perform(get("/api/forecasts/{runId}/recovery-options", runId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(5))
        .andExpect(jsonPath("$.data[0].selected").value(false));
    mockMvc
        .perform(get("/api/forecasts/{runId}/scenarios", runId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].scenarioType").value("BASELINE"))
        .andExpect(jsonPath("$.data[1].scenarioType").value("SIMULATED"))
        .andExpect(jsonPath("$.data[1].appliedOptionIds.length()").value(1));
  }

  @Test
  void QA_RISK_Recovery_Packet_v1이_시더로_조회된다() throws Exception {
    long businessId = businessApi.findBusinessIdByRegNo("QA-RISK").orElseThrow();

    mockMvc
        .perform(get("/api/businesses/{businessId}/packets/latest", businessId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").value(1))
        .andExpect(jsonPath("$.data.status").value("DRAFT"))
        .andExpect(jsonPath("$.data.snapshot.causes.length()").value(3))
        .andExpect(jsonPath("$.data.snapshot.selectedOptions[0].nextAction").exists());
  }

  @Test
  void 동의_항목_마스터와_사업자_동의_상태가_조회된다() throws Exception {
    long businessId = businessApi.findBusinessIdByRegNo("QA-RISK").orElseThrow();

    mockMvc
        .perform(get("/api/consent-types"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].code").value("ANALYSIS"))
        .andExpect(jsonPath("$.data[0].required").value(true));

    mockMvc
        .perform(get("/api/businesses/{businessId}/consents", businessId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].typeCode").value("ANALYSIS"))
        .andExpect(jsonPath("$.data[0].status").value("GRANTED"));
  }

  @Test
  void 상담자와_슬롯이_시더로_조회되고_잔여석이_계산된다() throws Exception {
    String body =
        mockMvc
            .perform(get("/api/counselors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long counselorId = objectMapper.readTree(body).path("data").get(0).path("counselorId").asLong();

    mockMvc
        .perform(get("/api/counselors/{counselorId}/slots", counselorId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].remainingSeats").value(2))
        .andExpect(jsonPath("$.data[1].remainingSeats").value(3))
        .andExpect(jsonPath("$.data[2].remainingSeats").value(1));
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
