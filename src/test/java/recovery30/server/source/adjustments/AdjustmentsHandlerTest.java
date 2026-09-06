package recovery30.server.source.adjustments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import recovery30.server.source.internal.AdjustmentRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdjustmentsHandlerTest {

  private static final long BIZ = 1L;

  @Autowired private MockMvc mockMvc;
  @Autowired private AdjustmentRepository adjustmentRepository;
  @MockitoBean private ForecastApi forecastApi;

  private long create(String body) throws Exception {
    String res =
        mockMvc
            .perform(
                post("/api/businesses/{businessId}/adjustments", BIZ)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return Long.parseLong(res.replaceAll(".*\"id\":(\\d+).*", "$1"));
  }

  @Test
  void 입력하면_DRAFT로_생성되고_방향이_유형으로_결정된다() throws Exception {
    mockMvc
        .perform(
            post("/api/businesses/{businessId}/adjustments", BIZ)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"adjustmentType\":\"CASH_SALES\",\"amount\":650000,"
                        + "\"expectedDate\":\"2025-07-20\",\"certainty\":\"ESTIMATED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("DRAFT"))
        .andExpect(jsonPath("$.data.direction").value("I"));

    mockMvc
        .perform(
            post("/api/businesses/{businessId}/adjustments", BIZ)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"adjustmentType\":\"EXPECTED_EXPENSE\",\"amount\":1200000,"
                        + "\"expectedDate\":\"2025-07-22\",\"certainty\":\"CONFIRMED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.direction").value("O"));
  }

  @Test
  void 필수값이_없으면_400() throws Exception {
    mockMvc
        .perform(
            post("/api/businesses/{businessId}/adjustments", BIZ)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"adjustmentType\":\"CASH_SALES\",\"amount\":1000}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("COMMON_400"));
  }

  @Test
  void 수정_삭제_후_목록은_DISCARDED를_제외한다() throws Exception {
    long id =
        create(
            "{\"adjustmentType\":\"EXPECTED_INCOME\",\"amount\":300000,"
                + "\"expectedDate\":\"2025-07-25\",\"certainty\":\"ESTIMATED\"}");

    mockMvc
        .perform(
            patch("/api/businesses/{businessId}/adjustments/{id}", BIZ, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":450000,\"memo\":\"수정함\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.amount").value(450000))
        .andExpect(jsonPath("$.data.memo").value("수정함"));

    mockMvc
        .perform(delete("/api/businesses/{businessId}/adjustments/{id}", BIZ, id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("DISCARDED"));

    mockMvc
        .perform(get("/api/businesses/{businessId}/adjustments", BIZ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void 없는_보정값_수정은_404_ADJUSTMENT_404_1() throws Exception {
    mockMvc
        .perform(
            patch("/api/businesses/{businessId}/adjustments/{id}", BIZ, 999_999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":1}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("ADJUSTMENT_404_1"));
  }

  @Test
  void apply하면_DRAFT가_SAVED로_전환되고_appliedRunId가_기록된다() throws Exception {
    when(forecastApi.findLatestForecastRunId(anyLong())).thenReturn(Optional.of(1L));
    long id =
        create(
            "{\"adjustmentType\":\"CASH_SALES\",\"amount\":650000,"
                + "\"expectedDate\":\"2025-07-20\",\"certainty\":\"ESTIMATED\"}");

    mockMvc
        .perform(post("/api/businesses/{businessId}/adjustments/apply", BIZ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.appliedCount").value(1))
        .andExpect(jsonPath("$.data.appliedRunId").value(1));

    var saved = adjustmentRepository.findByIdAndBusinessId(id, BIZ).orElseThrow();
    assertThat(saved.getStatus()).isEqualTo("SAVED");
    assertThat(saved.getAppliedRunId()).isEqualTo(1L);
  }
}
