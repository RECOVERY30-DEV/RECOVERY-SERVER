package recovery30.server.packet.createpacket;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.packet.internal.RecoveryPacketRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreatePacketHandlerTest {

  private static final long RUN = 4821L;

  @Autowired private MockMvc mockMvc;
  @Autowired private RecoveryPacketRepository recoveryPacketRepository;
  @MockitoBean private ForecastApi forecastApi;

  private void postSnapshot(String body, int expectedStatus) throws Exception {
    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/packets", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().is(expectedStatus));
  }

  @Test
  void 첫_생성은_v1_DRAFT이고_재생성은_v2로_이전버전을_잇는다() throws Exception {
    when(forecastApi.findBusinessId(anyLong())).thenReturn(Optional.of(1L));

    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/packets", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"snapshot\":{\"riskSnapshot\":{\"status\":\"위험\"}}}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").value(1))
        .andExpect(jsonPath("$.data.status").value("DRAFT"))
        .andExpect(jsonPath("$.data.supersedesPacketId").doesNotExist());

    long v1Id =
        recoveryPacketRepository
            .findTopByBusinessIdAndForecastRunIdOrderByVersionDesc(1L, RUN)
            .orElseThrow()
            .getId();

    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/packets", RUN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"snapshot\":{\"v\":2}}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").value(2))
        .andExpect(jsonPath("$.data.supersedesPacketId").value((int) v1Id));
  }

  @Test
  void snapshot이_없으면_400을_반환한다() throws Exception {
    when(forecastApi.findBusinessId(anyLong())).thenReturn(Optional.of(1L));
    postSnapshot("{}", 400);
  }

  @Test
  void 존재하지_않는_run이면_404를_반환한다() throws Exception {
    when(forecastApi.findBusinessId(anyLong())).thenReturn(Optional.empty());
    mockMvc
        .perform(
            post("/api/forecasts/{forecastRunId}/packets", 999_999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"snapshot\":{\"a\":1}}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("FORECAST_404_1"));
  }
}
