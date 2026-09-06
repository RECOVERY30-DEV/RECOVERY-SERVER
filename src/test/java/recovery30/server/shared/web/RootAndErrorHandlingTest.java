package recovery30.server.shared.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RootAndErrorHandlingTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void 루트는_200으로_문서_링크를_반환한다() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.docs").value("/swagger-ui/index.html"));
  }

  @Test
  void 매핑되지_않은_경로는_500이_아니라_404를_반환한다() throws Exception {
    mockMvc
        .perform(get("/definitely/not/mapped"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_404"));
  }

  @Test
  void 경로변수_타입이_틀리면_500이_아니라_400을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/forecasts/{id}", "not-a-number"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("COMMON_400_1"));
  }

  @Test
  void 잘못된_JSON_본문이면_500이_아니라_400을_반환한다() throws Exception {
    mockMvc
        .perform(
            put("/api/forecasts/{id}/option-selections", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ this is not json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("COMMON_400_1"));
  }

  @Test
  void 허용되지_않은_메서드면_405를_반환한다() throws Exception {
    mockMvc
        .perform(
            post("/api/forecasts/{id}/option-selections", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.error.code").value("COMMON_405"));
  }
}
