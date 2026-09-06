package recovery30.server.source.adjustmentsuggestions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.source.domain.AdjustmentSuggestion;
import recovery30.server.source.internal.AdjustmentRepository;
import recovery30.server.source.internal.AdjustmentSuggestionRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SuggestionsHandlerTest {

  private static final long BIZ = 1L;

  @Autowired private MockMvc mockMvc;
  @Autowired private AdjustmentSuggestionRepository suggestionRepository;
  @Autowired private AdjustmentRepository adjustmentRepository;

  private AdjustmentSuggestion seed(String type, long amount) {
    AdjustmentSuggestion s = new AdjustmentSuggestion();
    s.setBusinessId(BIZ);
    s.setAdjustmentType(type);
    s.setSuggestedAmount(amount);
    s.setSuggestedRule("매월 15일");
    s.setEvidenceText("최근 3개월 동일 패턴");
    s.setConfidence(new BigDecimal("0.82"));
    s.setStatus("PROPOSED");
    return suggestionRepository.save(s);
  }

  @Test
  void 기본은_PROPOSED만_반환한다() throws Exception {
    seed("CASH_SALES", 1_200_000L);
    AdjustmentSuggestion done = seed("EXTERNAL_FUND", 850_000L);
    done.setStatus("REJECTED");
    suggestionRepository.save(done);

    mockMvc
        .perform(get("/api/businesses/{businessId}/adjustment-suggestions", BIZ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].adjustmentType").value("CASH_SALES"));
  }

  @Test
  void 수락하면_DRAFT_보정값이_생기고_후보는_ACCEPTED가_된다() throws Exception {
    long sid = seed("CASH_SALES", 1_200_000L).getId();

    mockMvc
        .perform(post("/api/businesses/{businessId}/adjustment-suggestions/{id}/accept", BIZ, sid))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
        .andExpect(jsonPath("$.data.acceptedAdjustmentId").exists());

    long adjId = suggestionRepository.findById(sid).orElseThrow().getAcceptedAdjustmentId();
    var adj = adjustmentRepository.findByIdAndBusinessId(adjId, BIZ).orElseThrow();
    org.assertj.core.api.Assertions.assertThat(adj.getStatus()).isEqualTo("DRAFT");
    org.assertj.core.api.Assertions.assertThat(adj.getCertainty()).isEqualTo("ESTIMATED");
    org.assertj.core.api.Assertions.assertThat(adj.getAmount()).isEqualTo(1_200_000L);
  }

  @Test
  void 이미_처리된_후보를_수락하면_400_ADJUSTMENT_400_1() throws Exception {
    AdjustmentSuggestion s = seed("CASH_SALES", 1_000L);
    s.setStatus("ACCEPTED");
    suggestionRepository.save(s);

    mockMvc
        .perform(
            post("/api/businesses/{businessId}/adjustment-suggestions/{id}/accept", BIZ, s.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("ADJUSTMENT_400_1"));
  }

  @Test
  void 없는_후보를_수락하면_404_ADJUSTMENT_404_2() throws Exception {
    mockMvc
        .perform(
            post("/api/businesses/{businessId}/adjustment-suggestions/{id}/accept", BIZ, 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("ADJUSTMENT_404_2"));
  }
}
